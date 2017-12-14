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

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, IOException, NoSuchAlgorithmException {
		Thread frameThread = new Thread(new FrameAssistant("Chat"));
		frameThread.start();
	}
}

class FrameAssistant extends JFrame implements ActionListener, Runnable, SBox {
	private static final long serialVersionUID = 1L;
	JTextArea textAreaInput;
	JTextArea loginArea, passwordArea;
	JButton requestTextBtn, loginBtn, okBtn;
	JPanel cardPanel, buttonPanel, loginPanel;
	Font font = new Font("Verdana", Font.PLAIN, 20);
	
	PasswordSHAEncryption passwordEncrypter = new PasswordSHAEncryption();

	KeyPairGenerator keyGen;
	KeyPair key;

	Socket clientSocket = new Socket("192.168.100.6", 8092);
	ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
	ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
	CommonCryption common = new CommonCryption();

	public FrameAssistant(String s) throws FileNotFoundException, ClassNotFoundException, IOException {
		super(s);
		cardPanel = new JPanel(new CardLayout());

		loginPanel = createLoginPanel();
		
		cardPanel.add(loginPanel);
		cardPanel.add(createTextInputPanel(), BorderLayout.CENTER);

		this.getContentPane().add(cardPanel);

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

	private void generateRSAKeys() throws NoSuchAlgorithmException {
		keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(512);

		key = keyGen.generateKeyPair();
	}

	private JPanel createTextInputPanel() {
		JPanel panel = new JPanel();

		textAreaInput = new JTextArea(100, 50);

		JScrollPane areaScrollPane = new JScrollPane(textAreaInput, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		areaScrollPane.setPreferredSize(new Dimension(433, 400));

		textAreaInput.setLineWrap(true);
		textAreaInput.setFont(font);

		requestTextBtn = new JButton("Request Text");
		requestTextBtn.addActionListener(this);

		panel.add(areaScrollPane);
		panel.add(requestTextBtn);

		return panel;
	}

	private JPanel createLoginPanel() {
		JPanel panel = new JPanel(new GridLayout(3, 2));

		JLabel loginLabel = new JLabel("Login");
		JLabel passwordLabel = new JLabel("Password");

		loginArea = new JTextArea(2, 15);
		loginArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		passwordArea = new JTextArea(2, 15);
		passwordArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		okBtn = new JButton("Ok");
		okBtn.addActionListener(this);

		panel.add(createFlowLayoutPanel(loginLabel));
		panel.add(createFlowLayoutPanel(loginArea));
		panel.add(createFlowLayoutPanel(passwordLabel));
		panel.add(createFlowLayoutPanel(passwordArea));
		panel.add(createFlowLayoutPanel(okBtn));

		return panel;
	}

	private JPanel createFlowLayoutPanel(Component component) {
		JPanel panel = new JPanel();
		component.setFont(font);
		panel.add(component);
		return panel;
	}

	public void actionPerformed(ActionEvent e) {
		CardLayout cl = (CardLayout) (cardPanel.getLayout());
		if (e.getSource() == requestTextBtn) {
			try {
				textAreaInput.setText("");
				this.generateRSAKeys();
				Message message = new Message("", key.getPublic());
				outputStream.writeByte(0);
				outputStream.writeObject(message);
				outputStream.flush();
			} catch (SocketException e1) {
				textAreaInput.setText("Server was closed");
			} catch (IOException e1) {
				System.out.println(e1.getMessage());
			} catch (NoSuchAlgorithmException e1) {
				System.out.println(e1.getMessage());
			}
		} else if (e.getSource() == okBtn) {
			String login = loginArea.getText();
			String password = passwordArea.getText();
			
			// To illustrate authentication with salt and sha1 algorithm
			// In reality need to store salt and hash in database
			String requiredHash = "f98a0ea8658d041d28f12b43db3ba1c5d4e061e0";
			byte[] storedSalt = {-82, 68, 8, 115, 50, -126, 70 , -107, 97, 3, 105, -106, -50, 5, -45, -117, -4};
			
			System.out.println(passwordEncrypter.getSHA1Password(password, storedSalt));
			if (login.equals("nick") && passwordEncrypter.getSHA1Password(password, storedSalt).equals(requiredHash)) {
				loginArea.setText("");
				passwordArea.setText("");
				cl.last(cardPanel);
			} else {
				System.out.println("Wrong password or login");
			}
		}
	}

	public void run() {
		String sessionKey = "";
		while (true) {
			try {
				if (inputStream.available() > 0) {
					byte messageType = inputStream.readByte();

					switch (messageType) {
					case 0: // Receive ecnrypted session key
						byte[] encryptedSessionKey = Base64.getDecoder().decode(inputStream.readUTF());
						sessionKey = this.decryptSessionKey(encryptedSessionKey, key.getPrivate());
						System.out.println("Descrypted session key: " + sessionKey);

						// Send text-request flag
						outputStream.writeByte(1);
						outputStream.flush();
						break;
					case 1: // Receive encrypted text and text length
						Message message = (Message) inputStream.readObject();
						String text = message.getText();
						String length = message.getTextLength();
						int[] arr = Arrays.stream(text.substring(1, text.length() - 1).split(",")).map(String::trim)
								.mapToInt(Integer::parseInt).toArray();
						arr = this.decrypt(this.reverseTextBlocks(arr), sessionKey);
						byte[] bytes = this.convertToByteArray(this.reverseTextBlocks(arr));
						String decryptedText = new String(bytes);

						arr = Arrays.stream(length.substring(1, length.length() - 1).split(",")).map(String::trim)
								.mapToInt(Integer::parseInt).toArray();
						arr = this.decrypt(this.reverseTextBlocks(arr), sessionKey);
						bytes = this.convertToByteArray(this.reverseTextBlocks(arr));
						int decryptedTextLength = this.parseEncryptedTextLength(new String(bytes));

						textAreaInput.setText(decryptedText.substring(0, decryptedTextLength));
						break;
					}
				}
			} catch (SocketException e) {
				System.out.println(e.getMessage());
				textAreaInput.setText("Server was closed");
				break;
			} catch (IOException e) {
				System.out.println("Stream was closed");
			} catch (ClassNotFoundException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private byte[] convertToByteArray(int[] integers) {
		byte[] bytes = new byte[integers.length];
		for (int i = 0; i < integers.length; i++) {
			bytes[i] = (byte) integers[i];
		}
		return bytes;
	}

	private int parseEncryptedTextLength(String length) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length.length(); i++) {
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

	private int[] reverseTextBlocks(int[] blocks) {
		if (blocks.length == 4) {
			return blocks;
		}
		int[] reversedArray = new int[blocks.length];
		int j = blocks.length - 1;
		for (int i = 0; i < blocks.length; i += 4) {
			int[] blocksToCopy = new int[4];

			for (int k = i; k - i < 4; k++) {
				blocksToCopy[k - i] = blocks[k];
			}

			for (int k = 3; k >= 0; k--) {
				reversedArray[j - k] = blocksToCopy[3 - k];
			}

			j -= 4;
		}
		return reversedArray;
	}

	private int[] decrypt(int[] texts, String key) {
		byte[] binaryKeys = key.getBytes();
		int[] keys = new int[4];

		for (int i = 0; i < 4; i++) {
			keys[i] = (int) binaryKeys[i];
		}

		int randomInitVector = 109;
		int[] decryptedText = new int[texts.length];
		for (int i = 0; i < texts.length; i += 4) {
			int[] partsToDecrypt = new int[4];
			for (int j = i; j - i < 4; j++) {
				partsToDecrypt[j - i] = texts[j];
			}

			partsToDecrypt = common.getIntegers(common.initPermutation(common.getBytes(partsToDecrypt)));
			for (int k = 31; k >= 0; k--) {
				this.linearTransform(partsToDecrypt, keys);
				partsToDecrypt = common.getIntegers(sBoxInv(common.getBytes(partsToDecrypt), k));
			}
			partsToDecrypt = common.getIntegers(common.finalPermutation(common.getBytes(partsToDecrypt)));

			// Cipher feed back mode
			for (int j = i; j - i < 4; j++) {
				if ((i + 4) >= (texts.length - 1)) { // last iteration
					partsToDecrypt[j - i] = partsToDecrypt[j - i] ^ randomInitVector;
				} else {
					partsToDecrypt[j - i] = partsToDecrypt[j - i] ^ texts[j + 4];
				}
			}

			for (int j = i; j - i < 4; j++) {
				decryptedText[j] = partsToDecrypt[j - i];
			}
		}

		return decryptedText;
	}

	private int[] linearTransform(int[] b, int[] keys) {
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
		for (int i = 0; i < 4; i++) {
			b[i] = b[i] ^ keys[i];
		}
		return b;
	}

	private byte[] sBoxInv(byte[] data, int round) {
		byte[] toUse = inverseBoxes[round % 8];
		byte[] output = new byte[16];
		for (int i = 0; i < 16; i++) {
			// Break signed-ness
			int curr = data[i] & 0xFF;
			byte low4 = (byte) (curr >>> 4);
			byte high4 = (byte) (curr & 0x0F);
			output[i] = (byte) ((toUse[low4] << 4) ^ (toUse[high4]));
		}
		return output;
	}

}
