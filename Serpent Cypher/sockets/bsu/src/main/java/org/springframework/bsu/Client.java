package org.springframework.bsu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Client {
	
	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, IOException {
		Thread frameThread = new Thread(new FrameAssistant("Chat"));
		frameThread.start();
	}
}

class FrameAssistant extends JFrame implements ActionListener, Runnable{
	private static final long serialVersionUID = 1L;
	DefaultListModel<String> messageListModel;
	String author;
	JList<String> messageJList;
	JTextArea textAreaInput;
	JButton buttonSend, buttonOk, buttonGenerateRSA;
	Boolean isNewMessage;
	JTextArea loginArea, passwordArea;
	JPanel cardPanel, chatPanel, loginPanel, buttonPanel;
	JLabel errorLabel; 
	Font font = new Font("Verdana", Font.PLAIN, 20);
	
	KeyPairGenerator keyGen;
	KeyPair key;
	
	Socket clientSocket = new Socket("192.168.100.5", 8092);
	ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
	ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
	
	public FrameAssistant(String s) throws FileNotFoundException, ClassNotFoundException, IOException {
		super(s);
		
		errorLabel = new JLabel("Error");
		errorLabel.setFont(font);
		
		cardPanel = new JPanel(new CardLayout());
		
		chatPanel = new JPanel(new BorderLayout());
		chatPanel.add(createChatViewPanel(), BorderLayout.CENTER);
		chatPanel.add(createTextInputPanel(), BorderLayout.SOUTH);
		
		cardPanel.add(chatPanel);
		
		buttonPanel = createButtonPanel();
		
		this.getContentPane().add(cardPanel);
		this.getContentPane().add(buttonPanel, BorderLayout.NORTH);
		
		this.setSize(700, 500);
		this.setVisible(true);
		
		closeWindow();
	}
	
	public void closeWindow() {
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        try {
		        	inputStream.close();
		        	outputStream.close();
					clientSocket.close();
					System.exit(0);
				} catch (IOException e) {
					System.out.println(e);
				}
		    }
		});
	}
	
	private JPanel createButtonPanel() {
		JPanel panel= new JPanel();
		
		buttonGenerateRSA = new JButton("Generate RSA");
		buttonGenerateRSA.setFont(font);
		buttonGenerateRSA.addActionListener(this);
		
		panel.add(buttonGenerateRSA);
		
		return panel;
	}
	
	private JPanel createChatViewPanel() {
		JPanel panel = new JPanel();
		
		messageListModel = new DefaultListModel<String>();
		messageJList = new JList<String>(messageListModel);
		
		messageJList.setFont(font);
		
		JScrollPane areaScrollPane = new JScrollPane(messageJList, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		areaScrollPane.setPreferredSize(new Dimension(500, 350));
		panel.add(areaScrollPane);
		
		return panel;
	}
	
	private void generateRSAKeys() throws NoSuchAlgorithmException {
		 keyGen = KeyPairGenerator.getInstance("RSA");
	     keyGen.initialize(512);
	     
	     key = keyGen.generateKeyPair();
	}
	
	private JPanel createFlowLayoutPanel(Component component) {
		JPanel panel = new JPanel();
		component.setFont(font);
		panel.add(component);
		return panel;
	}
	
	private JPanel createTextInputPanel() {
		JPanel panel = new JPanel();
		
		textAreaInput = new JTextArea(1, 15);
		
		JScrollPane areaScrollPane = new JScrollPane(textAreaInput, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		areaScrollPane.setPreferredSize(new Dimension(433, 50));
		
		textAreaInput.setFont(font);
		
		buttonSend = new JButton("Request Text");
		buttonSend.addActionListener(this);
		
		panel.add(areaScrollPane);
		panel.add(buttonSend);
		
		return panel;
	}
	
	
	public void actionPerformed(ActionEvent e) {
		CardLayout cl = (CardLayout)(cardPanel.getLayout());
		if(e.getSource() == buttonSend) {
			try {
				outputStream.writeByte(1);
				outputStream.flush();
			} catch (SocketException e1) {
				textAreaInput.setText("Server was closed");
			} catch (IOException e1) {
				System.out.println(e1.getMessage());
			}
		} else if(e.getSource() == buttonGenerateRSA) {
			try {
				this.generateRSAKeys();
				Message message = new Message("", key.getPublic());
				outputStream.writeByte(0);
				outputStream.writeObject(message);
				outputStream.flush();
				textAreaInput.setText("");
			} catch (SocketException e1) {
				textAreaInput.setText("Server was closed");
			} catch (IOException e1) {
				System.out.println(e1.getMessage());
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public void run() {
		String sessionKey = "";
		while(true) {
			try {
				if (inputStream.available() > 0) {
					byte messageType = inputStream.readByte();
	
	        	    switch(messageType) {
	            	    case 0: // Receive ecnrypted session key
	            	    	byte[] encryptedSessionKey = Base64.getDecoder().decode(inputStream.readUTF());
	            	    	sessionKey = this.decryptSessionKey(encryptedSessionKey, key.getPrivate());
	            	    	System.out.println("Descrypted session key: " + sessionKey);
	            	    	break;
	            	    case 1: // Receive encrypted text and text length
	            	    	Message message = (Message)inputStream.readObject();
	            	    	String text = message.getText();
	            	    	String length = message.getTextLength();
	            	    	int[] arr = Arrays.stream(text.substring(1, text.length()-1).split(","))
	            	    		    .map(String::trim).mapToInt(Integer::parseInt).toArray();
	            	    	arr = this.decrypt(arr, sessionKey);
	            	    	byte[] bytes = this.convertToByteArray(arr);
	            	    	String decryptedText = new String(bytes);
	            	    	
	            	    	arr = Arrays.stream(length.substring(1, length.length()-1).split(","))
	            	    		    .map(String::trim).mapToInt(Integer::parseInt).toArray();
	            	    	arr = this.decrypt(arr, sessionKey);
	            	    	bytes = this.convertToByteArray(arr);
	            	    	int decryptedTextLength = this.parseEncryptedTextLength(new String(bytes));
	            	    
	            	    	System.out.println("Decrypted text: " + decryptedText.substring(0, decryptedTextLength));
	            	    	textAreaInput.setText(decryptedText.substring(0, decryptedTextLength));
	            	    	break;
	        	    }
				}
			} catch(SocketException e) {
				System.out.println(e.getMessage());
				textAreaInput.setText("Server was closed");
				break;
			} catch(IOException e) {
				System.out.println("Stream was closed");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private byte[] convertToByteArray(int[] integers) {
		byte[] bytes = new byte[integers.length];
		for(int i = 0; i < integers.length; i ++) {
			bytes[i] = (byte) integers[i];
		}
		return bytes;
	}
	
	private int parseEncryptedTextLength(String length) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length.length(); i ++) {
			if (String.valueOf(length.charAt(i)).matches("[0-9]")) {
				builder.append(length.charAt(i));
			} else {
				break;
			}
		}
		return Integer.parseInt(builder.toString());
	}
	
	private String decryptSessionKey(byte[] ecnryptedSessionKey, PrivateKey key) {
		byte[] dectyptedText = null;
		try {
			final Cipher cipher = Cipher.getInstance("RSA");

			cipher.init(Cipher.DECRYPT_MODE, key);
			dectyptedText = cipher.doFinal(ecnryptedSessionKey);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new String(dectyptedText);
	}
	
	private int[] decrypt(int[] texts, String key) {
    	byte[] binaryKeys = key.getBytes();
		int[] keys = new int[4];
		
		for (int i = 0; i < 4; i ++) {
			keys[i] = (int)binaryKeys[i];
		}
		
		int[] decryptedText = new int[texts.length];
		for (int i = 0; i < texts.length; i += 4) {
			int[] partsToDecrypt = new int[4];
			for (int j = i; j - i < 4; j ++) {
				partsToDecrypt[j - i] = texts[j];
			}
			int[] decryptedTextParts = this.decryptRound(partsToDecrypt, keys);
			for (int j = i; j - i < 4; j ++) {
				decryptedText[j] = decryptedTextParts[j - i];
			}
		}
		
		return decryptedText;
    }
	
	private int[] decryptRound(int[] b, int[] keys) {
		b[2] = Integer.rotateRight(b[2], 22);
		b[0] = Integer.rotateRight(b[0], 5);
		b[2] = b[2] ^ b[3] ^ (b[1] << 7);
		b[0] = b[0] ^ b[1] ^ b[3];
		b[3] = Integer.rotateRight(b[3], 7);
		b[1] = Integer.rotateRight(b[1], 1);
		b[3] = b[3] ^ b[2] ^ (b[0] << 3);
		b[1] = b[0] ^ b[1] ^ b[2];
		b[2] = Integer.rotateRight(b[2], 3);
		b[0] = Integer.rotateRight(b[0], 13);
		for (int i = 0; i < 4; i ++) {
			b[i] = b[i] ^ keys[i];
		}
		return b;
	}
	
}

