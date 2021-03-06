package org.springframework.bsu;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.crypto.Cipher;


public class Server{

    static ServerSocket serverSocket;
    public static List<ConnectedClient> clients = new ArrayList<>();
 
  
    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(8092);
     
        System.out.println("Server start!");

        while (true) {
        	try {
	            ConnectedClient client = new ConnectedClient(serverSocket.accept(), serverSocket);
	            synchronized (clients) {
	            	clients.add(client);
				}
	            client.start();
        	} catch(SocketException e) {
        		System.out.println(e.getMessage());
        		break;
        	}
        }
    }
    
    public synchronized static void closeServer() throws IOException {
    	for(ConnectedClient client: clients) {
        	client.getInputStream().close();
        	client.getOutputStream().close();
        }
        serverSocket.close();
        System.out.println("Server close!");
    }
}

 class ConnectedClient extends Thread implements Runnable, SBox{
    private Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private CommonCryption common = new CommonCryption();

    public ConnectedClient(Socket s, ServerSocket serverSocket) throws IOException {
    	super();
        clientSocket = s;
        System.out.println("new user connected from " + s.getInetAddress().toString());
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
    }
    
    public ObjectInputStream getInputStream() {
    	return inputStream;
    }
    
    public ObjectOutputStream getOutputStream() {
    	return outputStream;
    }
    
    public void run() {
    	String text = "text that should be encrypted and decrypted correctly, yes";
    	System.out.println("Text that needs to be sent");
    	String key = "";
        try {
            while (clientSocket.isConnected()) {
            	 byte messageType = inputStream.readByte();

            	  switch(messageType) {
	            	  case 0: // Receive open RSA key (512 bits)
	            		  Message message = (Message)inputStream.readObject();
	            		  System.out.println("Open RSA KEY: " + message.getKey());
	            		  key = this.generateSessionKey(); // (128 bits)
	            	   	  System.out.println("Session key: " + key);
	            	   	  byte[] encryptedSessionKey = this.encryptSessionKey(key, message.getKey());
	            	   	  outputStream.writeByte(0); 
	            	   	  outputStream.writeUTF(Base64.getEncoder().encodeToString(encryptedSessionKey));
	            	   	  outputStream.flush();
	            	   	  break;
	            	  case 1: // Encrypt and send text and text length
	            		  int[] encryptedText = this.encrypt(text, key);
	            		  int[] encryptedLength = this.encrypt(text.length() + "", key);
	            		  Message messageToSend = new Message();
	            		  messageToSend.setText(Arrays.toString(encryptedText));
	            		  messageToSend.setTextLength(Arrays.toString(encryptedLength));
	            		  outputStream.writeByte(1); 
	            		  outputStream.writeObject(messageToSend);
	            		  outputStream.flush();
	            		  break;
            	  }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
        	 System.out.println(e.getMessage());
		} finally {
            System.out.println("user disconnected from " + clientSocket.getInetAddress().toString());
        }
    }
    
    private String generateSessionKey() {
    	UUID uuid = UUID.randomUUID();
    	return uuid.toString();
    }
    
    // Using open RSA key
    private byte[] encryptSessionKey(String sessionKey, PublicKey publicKey) {
    	 byte[] cipherText = null;
    	    try {
    	      Cipher cipher = Cipher.getInstance("RSA");
    	      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    	      cipherText = cipher.doFinal(sessionKey.getBytes());
    	    } catch (Exception e) {
    	      e.printStackTrace();
    	    }
    	    return cipherText;
    }
    
    private int[] encrypt(String text, String key) {
    	byte[] binaryText = text.getBytes();
    	byte[] binaryKeys = key.getBytes();
		int[] keys = new int[4];
		
		int bytesLength = this.getComplementedBytesLength(binaryText.length);
		int[] encryptedText = new int[bytesLength];
		int[] texts = new int[bytesLength];
		
		for (int i = 0; i < 4; i ++) {
			keys[i] = (int)binaryKeys[i];
		}
		
		for (int i = 0; i < bytesLength; i ++) {
			if (i < binaryText.length) {
				texts[i] = (int)binaryText[i];
			} else {
				texts[i] = 100;
			}
		}
		
		int randomInitVector = 109;
		for (int i = 0; i < bytesLength; i += 4) {
			int[] partsToCrypt = new int[4];
			for (int j = i; j - i < 4; j ++) {
				partsToCrypt[j - i] = texts[j];
			}
			
			// Cipher feed back mode
			for (int j = i; j - i < 4; j ++) {
				if (i == 0) {
					partsToCrypt[j - i] = partsToCrypt[j - i] ^ randomInitVector;
				} else {
					partsToCrypt[j - i] = partsToCrypt[j - i] ^ encryptedText[j - 4];
				}
			}
			
			partsToCrypt = common.getIntegers(common.initPermutation(common.getBytes(partsToCrypt)));
			for (int k = 0; k < 32; k ++) {
				partsToCrypt = common.getIntegers(sBox(common.getBytes(partsToCrypt), k));
				this.linearTransform(partsToCrypt, keys);
			}
			partsToCrypt = common.getIntegers(common.finalPermutation(common.getBytes(partsToCrypt)));
			
			for (int j = i; j - i < 4; j ++) {
				encryptedText[j] = partsToCrypt[j - i];
			}
		}
		
		return encryptedText;
    }
    
    private byte[] sBox(byte[] data, int round) {
        byte[] toUse = straightBoxes[round%8];
        byte[] output = new byte[16];
        for( int i = 0; i < 16; i++ ) {
            //Break signed-ness
            int curr = data[i]&0xFF;
            byte low4 = (byte)(curr>>>4);
            byte high4 = (byte)(curr&0x0F);
            output[i] = (byte) ((toUse[low4]<<4) ^ (toUse[high4]));
        }
        return output;
    }
    
    private int getComplementedBytesLength(int length) {
    	return length + (4 - length % 4);
    }
    
    private int[] linearTransform(int[] b, int keys[]) {
		for (int i = 0; i < 4; i ++) {
			b[i] = b[i] ^ keys[i];
		}
		b[0] = Integer.rotateLeft(b[0], 13);
		b[2] = Integer.rotateLeft(b[2], 3);
		b[1] = b[0] ^ b[1] ^ b[2];
		b[3] = b[3] ^ b[2] ^ (b[0] << 3);
		b[1] = Integer.rotateLeft(b[1], 1);
		b[3] = Integer.rotateLeft(b[3], 7);
		b[0] = b[0] ^ b[1] ^ b[3];
		b[2] = b[2] ^ b[3] ^ (b[1] << 7);
		b[0] = Integer.rotateLeft(b[0], 5);
		b[2] = Integer.rotateLeft(b[2], 22);
		return b;
	}

    public void send(String s) throws IOException {
        outputStream.writeUTF(s);
        outputStream.flush();
    }
}