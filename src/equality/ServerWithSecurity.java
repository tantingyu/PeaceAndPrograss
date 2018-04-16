package equality;

/* Programming Assignment 2
 * Authors: Tan Ting Yu (1002169) and Chong Lok Swen (1002468)
 * Date: -
 */

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ServerWithSecurity extends Thread {
	
	static final String CERT_DIRECTORY = "server.crt";
	static final String RSA_PRIVATE_KEY = "privateServer.der";
	
	int cp;
	ServerSocket serverSocket;
	
	public ServerWithSecurity(int socketNumber, int cp) throws Exception {
		serverSocket = new ServerSocket(socketNumber);
		this.cp = cp;
	}
	
	@Override
	public void run() {
		try {
			new ConnectionSocket(serverSocket.accept()).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class ConnectionSocket extends Thread {
		
		Socket connectionSocket;
		Certificate certificate;
		Key privateKey;
		Key sessionKey;	// RSA private key (CP-1), or AES symmetric key (CP-2)
		Cipher cipher;
		
		DataInputStream fromClient;
		DataOutputStream toClient;
		FileOutputStream fileOutputStream;
		BufferedOutputStream bufferedOutputStream;
		
		ConnectionSocket(Socket connectionSocket) throws Exception {
			this.connectionSocket = connectionSocket;
			certificate = (X509Certificate) getCertificate("X.509");
			privateKey = loadPrivateKey();
		}
		
		@Override
		public void run() {
			try {
				fromClient = new DataInputStream(connectionSocket.getInputStream());
				toClient = new DataOutputStream(connectionSocket.getOutputStream());
				
				/* ------ START OF AUTHENTICATION PROTOCOL ------ */
				// receive client's hello
				byte[] hello = receiveMessage();
				print("Message from Client: " + new String(hello));
				
				// send certificate to client
				print("Sending certificate to client");
				byte[] serverCert = certificate.getEncoded();
				sendMessage(serverCert);
				
				// receive client's handshake
				byte[] handshake = receiveMessage();
				print("Message from Client: " + new String(handshake));
				/* ------ END OF AUTHENTICATION PROTOCOL ------ */
				
				/* ------ START OF CONFIDENTIALITY PROTOCOL ------ */
				// configure cipher
				cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");	
				
				// blocks will be encrypted using server's RSA public key
				if (cp == 1) {
					sessionKey = privateKey;
				// blocks will be encrypted using client's AES symmetric key
				} else if (cp == 2) {
					// request client's AES symmetric key
					print("Requesting client's AES symmetric key");
					byte[] aesRequest = "Please send me your AES symmetric key.".getBytes();
					sendMessage(aesRequest);
					
					// receive client's AES symmetric key
					int numBytes = fromClient.readInt();
					byte[] aesKeyEncrypted = new byte[128];
					fromClient.readFully(aesKeyEncrypted, 0, 128);
					print("Received client's AES symmetric key");
					
					// decrypt client's AES symmetric key using RSA private key
					byte[] aesKeyDecrypted = decryptData(aesKeyEncrypted, privateKey);
					
					// reconfigure cipher and set session key
					cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
					sessionKey = new SecretKeySpec(aesKeyDecrypted, 0, aesKeyDecrypted.length, "AES");
				}
				/* ------ END OF CONFIDENTIALTIY PROTOCOL ------ */
				
				// begin file transfer
				downloadFile();
				
				// close connection
				print("Download complete, closing connection");
				connectionSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Certificate getCertificate(String type) throws Exception {
			return CertificateFactory.getInstance(type)
						.generateCertificate(new FileInputStream(CERT_DIRECTORY));
		}
		
		Key loadPrivateKey() throws Exception {
			File rsaPrivateKey = new File(RSA_PRIVATE_KEY);
			int length = (int) rsaPrivateKey.length();
			byte[] rsaPrivateKeyBytes = new byte[length];
			
			DataInputStream dataInputStream = new DataInputStream(new FileInputStream(rsaPrivateKey));
			dataInputStream.readFully(rsaPrivateKeyBytes, 0, length);
			dataInputStream.close();

			return KeyFactory.getInstance("RSA")
					.generatePrivate(new PKCS8EncodedKeySpec(rsaPrivateKeyBytes));
		}
		
		void downloadFile() throws Exception {
			int numBytes = 0;
			while (true) {
				int packetType = fromClient.readInt();
				if (packetType == 0) {
					// receive file name
					byte[] decryptedFileName = decryptData(receiveMessage(), sessionKey);
					
					// initialize output streams
					fileOutputStream = new FileOutputStream("recv/" + 
							new String(decryptedFileName, 0, decryptedFileName.length));
					bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
				} else if (packetType == 1) {
					// receive file data block
					numBytes = fromClient.readInt();
					byte [] encryptedBlock = new byte[128];
					fromClient.readFully(encryptedBlock, 0, 128);
					
					if (numBytes > 0) {
						byte[] decryptedBlock = decryptData(encryptedBlock, sessionKey);
						bufferedOutputStream.write(decryptedBlock, 0, decryptedBlock.length);
					} 
				} else if (packetType == 2) {
					// receive notification that client has completed upload
					byte[] notifyComplete = receiveMessage();
					print("Message from Client: " + new String(notifyComplete));
					
					// close output streams
					bufferedOutputStream.close();
					fileOutputStream.close();
					return;
				}
			}
		}

		byte[] receiveMessage() throws Exception {
			int numBytes = fromClient.readInt();
			byte[] message = new byte[numBytes];
			fromClient.readFully(message, 0, numBytes);
			return message;
		}
		
		void sendMessage(byte[] message) throws Exception {
			toClient.writeInt(message.length);
			toClient.write(message);
			toClient.flush();
		}
		
		byte[] decryptData(byte[] block, Key key) throws Exception {
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(block);
		}
	}
	
	void print(String message) {
		System.out.println("Server Console >> " + message);
	}
	
	public static void main(String[] args) throws Exception {
		ServerWithSecurity server = new ServerWithSecurity(Integer.parseInt(args[0]), 
				Integer.parseInt(args[1]));
		server.start();
	}
}