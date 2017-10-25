package org.springframework.bsu;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.crypto.Cipher;

import pj.lib.edu.rit.util.Packing;

public class Server {

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

 class ConnectedClient extends Thread implements Runnable, SBox, Table{
    private Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private int[] prekeys = new int[140];
   
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
    	String text = "text what should be encrypted and decrypted correctly, yes";
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
            System.out.println(e.toString());
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            System.out.println("user disconnected from " + clientSocket.getInetAddress().toString());
        }
    }
    
    public void setKey(byte[] key) {
        //prekey initialization from K
        for(int i = 0; i < 8; i++) {
            prekeys[i] = Packing.packIntBigEndian(new byte[]{this.key[4*i],this.key[4*i+1],this.key[4*i+2],this.key[4*i+3]}, 0);
        }
        //Build out prekey array
		//There's a shift of 8 positions here because I build the intermediate keys in the same
		//array as the other prekeys.
        for( int i = 8; i < prekeys.length; i++ ) {
            byte[] prnt = new byte[4];
			//Phi is the fractional part of the golden ratio
            int phi = 0x9e3779b9;
            int tmp;
            tmp = prekeys[i-8] ^ prekeys[i-5] ^ prekeys[i-3] ^ prekeys[i-1] ^ 
                i-8 ^ phi;
            prekeys[i] = (tmp << 11) | (tmp >>> (21));
            prnt = new byte[4];
            Packing.unpackIntBigEndian(prekeys[i], prnt, 0);
         }
    }
    
    private String generateSessionKey() {
    	UUID uuid = UUID.randomUUID();
    	return complementSessionKey(uuid.toString());
    }
    
    private String complementSessionKey(String uuidKey) {
    	byte[] keyBytes = uuidKey.getBytes();
    	StringBuilder builder = new StringBuilder();
    	for(int i = 0; i < keyBytes.length; i ++) {
    		builder.append(keyBytes[i]);
    	}
    	String binKey = builder.toString();

    	for (int i = binKey.length(); i < 256; i ++) {
    		if (binKey.length() == i) {
    			builder.append(1);
    		} else if (keyBytes.length < i) {
    			builder.append(0);
    		}
    	}
    	
    	return builder.toString();
    }
    
    private byte[] permutations(byte[] data, int round) {
        byte[] permutationRow = straightBoxes[round % 8]; 
        byte[] output = new byte[16];
        for(int i = 0; i < 16; i++) {
            //Break signed-ness
            int curr = data[i] & 0xFF;
            byte low4 = (byte)(curr >>> 4);
            byte high4 = (byte)(curr & 0x0F);
            output[i] = (byte) ((permutationRow[low4] << 4) ^ (permutationRow[high4]));
        }
        return output;
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
    
    private byte[] getComplementedText(byte[] text, int neededLength) {
    	byte[] complementedText = new byte[neededLength];
    	byte JUST_FOR_COMPLEMENT_VALUE = 100;
    	
    	for (int i = 0; i < neededLength; i ++) {
			if (i < text.length) {
				complementedText[i] = text[i];
			} else {
				complementedText[i] = JUST_FOR_COMPLEMENT_VALUE;
			}
		}
    	return complementedText;
    }
    
    private byte[] encrypt(String text, String key) {
    	byte[] binaryText = text.getBytes();
 		
		int complementedTextLength = this.getComplementedBytesLength(binaryText.length);
		byte[] texts = this.getComplementedText(binaryText, complementedTextLength);
		
		byte[] encryptedText = new byte[complementedTextLength];
		
		for (int i = 0; i < complementedTextLength; i += 16) {
			byte[] partsToCrypt = new byte[16];
			for (int j = i; j - i < 16; j ++) {
				partsToCrypt[j - i] = texts[j];
			}
			
			// TODO: Need to implement cipher feed back mode HERE
			
			partsToCrypt = this.initPermutation(partsToCrypt);
			for (int k = 0; k < 32; k ++) {
				this.cryptRound(partsToCrypt, k);
			}
			partsToCrypt = this.finalPermutation(partsToCrypt);
			
			for (int j = i; j - i < 16; j ++) {
				encryptedText[j] = partsToCrypt[j - i];
			}
		}
		
		return encryptedText;
    }
    
    private int getComplementedBytesLength(int length) {
    	return length + (16 - length % 16);
    }
    
    // Zorstka (((
    private byte[] getRoundKeys(int round) {
        int k0 = prekeys[4*round+8];
        int k1 = prekeys[4*round+9];
        int k2 = prekeys[4*round+10];
        int k3 = prekeys[4*round+11];
        int box = (((3-round)%8)+8)%8;
        byte[] in = new byte[16];
        for (int j = 0; j < 32; j+=2) {
            in[j/2] = (byte) (((k0 >>> j) & 0x01)     |
            ((k1 >>> j) & 0x01) << 1 |
            ((k2 >>> j) & 0x01) << 2 |
            ((k3 >>> j) & 0x01) << 3 |
            ((k0 >>> j+1) & 0x01) << 4 |
            ((k1 >>> j+1) & 0x01) << 5 |
            ((k2 >>> j+1) & 0x01) << 6 |
            ((k3 >>> j+1) & 0x01) << 7 );
        }
        byte[] out = permutations(in, box);
        byte[] key = new byte[16];
        for (int i = 3; i >= 0; i--) {
            for(int j = 0; j < 4; j++) {
                key[3-i] |= (out[i*4+j] & 0x01) << (j*2) | ((out[i*4+j] >>> 4) & 0x01) << (j*2+1) ;
                key[7-i] |= ((out[i*4+j] >>> 1) & 0x01) << (j*2) | ((out[i*4+j] >>> 5) & 0x01) << (j*2+1) ;
                key[11-i] |= ((out[i*4+j] >>> 2) & 0x01) << (j*2) | ((out[i*4+j] >>> 6) & 0x01) << (j*2+1) ;
                key[15-i] |= ((out[i*4+j] >>> 3) & 0x01) << (j*2) | ((out[i*4+j] >>> 7) & 0x01) << (j*2+1) ;
            }
        }
        return initPermutation(key);
    }
    
    /**
     * Perform initial permutation on the input
     *
     * @param data Input bit sequence
     */
    private byte[] initPermutation(byte[] data) {
        byte[] output = new byte[16];
        for (int i = 0;  i < 128; i++) {
            int bit = (data[(ipTable[i]) / 8] >>> ((ipTable[i]) % 8)) & 0x01;
            if ((bit & 0x01) == 1) {
            	output[15 - (i / 8)] |= 1 << (i % 8);
            } else {
            	output[15 - (i / 8)] &= ~ (1 << (i % 8));
            }
        }
        return output; 
    }

    /**
     * Perform final permutation on the input
     *
     * @param data Input bit sequence
     */
    private byte[] finalPermutation(byte[] data) {
        byte[] output = new byte[16];
        for (int i = 0;  i < 128; i++) {
            int bit = (data[15 - fpTable[i] / 8] >>> (fpTable[i] % 8)) & 0x01;
            if ((bit & 0x01) == 1) {
            	output[(i / 8)] |= 1 << (i % 8);
            } else {
            	output[(i / 8)] &= ~ (1 << (i % 8));
            }
        }
        return output; 
    }
    
    private byte[] cryptRound(byte[] b, int roundNumber) {
    	byte[] roundKeys = new byte[16];
    	roundKeys = getRoundKeys(roundNumber);
    	
		for (int i = 0; i < 16; i ++) {
			b[i] = (byte) (b[i] ^ roundKeys[i]);
		}
		
		b = this.permutations(b, roundNumber);

		b = this.linearTransform(b);
		return b;
	}
    
    private byte[] linearTransform(byte[] b) {
    	 byte[] output = new byte[16];
  
    	 int[] x = this.getIntegers(b);
         x[0] = ((x[0] << 13) | (x[0] >>> (32 - 13))); 
         x[2] = ((x[2] << 3) | (x[2] >>> (32 - 3)));
         x[1] = x[1] ^ x[0] ^ x[2];
         x[3] = x[3] ^ x[2] ^ (x[0] << 3);
         x[1] = (x[1] << 1) | (x[1] >>> (32 - 1));
         x[3] = (x[3] << 7) | (x[3] >>> (32 - 7));
         x[0] = x[0] ^ x[1] ^ x[3];
         x[2] = x[2] ^ x[3] ^ (x[1] << 7);
         x[0] = (x[0] << 5) | (x[0] >>> (32 - 5));
         x[2] = (x[2] << 22) | (x[2] >>> (32 - 22));
         
         output = this.getBytes(x);
         
         return output;
    }
    
    private int[] getIntegers(byte[] b) {
    	 ByteBuffer buffer = ByteBuffer.wrap(b);
    	 int[] integers = new int[b.length / 4];
    	 for (int i = 0; i < b.length; i ++) {
    		 integers[i] = buffer.getInt();
    	 }
    	 return integers;
    }
    
    private byte[] getBytes(int[] n) {
    	 ByteBuffer buffer = ByteBuffer.allocate(n.length * 4);
    	 for (int i = 0; i < n.length; i ++) {
    		 buffer.putInt(n[i]);
    	 }
    	 return buffer.array();
    }
    		
    public void send(String s) throws IOException {
        outputStream.writeUTF(s);
        outputStream.flush();
    }
}