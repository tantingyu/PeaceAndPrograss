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
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ServerWithSecurity extends Thread {
	
	static final int PORT_NUMBER = 4321;
	static final int CP = 1;
	
	static final String CERT_DIRECTORY = "server.crt";
	static final String RSA_PRIVATE_KEY = "privateServer.der";
	public int cp = 1;
	
	ServerSocket serverSocket;
	Cipher cipher;
	
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
				int numBytes = 0;

				// receive client's hello
				numBytes = fromClient.readInt();
				byte[] certRequest = new byte[numBytes];
				fromClient.readFully(certRequest, 0, numBytes);
				print("Message from Client: " + new String(certRequest));
				
				// send certificate to client
				print("Sending certificate to client.");
				byte[] serverCertBytes = certificate.getEncoded();
				toClient.writeInt(serverCertBytes.length);
				toClient.write(serverCertBytes);
				toClient.flush();
				
				// receive client's handshake
				numBytes = fromClient.readInt();
				byte[] handshake = new byte[numBytes];
				fromClient.readFully(handshake);
				print("Message from Client: " + new String(handshake));
				
				// configure cipher
				cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");	
				
				if (cp == 1) {
					sessionKey = privateKey;
				} else if (cp == 2) {
					// request client's AES symmetric key
					print("Requesting client's AES symmetric key.");
					byte[] aesRequest = "Please send me your AES symmetric key.".getBytes();
					toClient.writeInt(aesRequest.length);
					toClient.write(aesRequest);
					toClient.flush();
					
					// receive client's AES symmetric key
					numBytes = fromClient.readInt();
					byte[] aesKeyEncrypted = new byte[128];
					fromClient.readFully(aesKeyEncrypted, 0, 128);
					print("Received client's AES symmetric key.");
					
					// decrypt client's AES symmetric key using RSA private key
					byte[] aesKeyDecrypted = decryptData(aesKeyEncrypted, privateKey);
					sessionKey = new SecretKeySpec(aesKeyDecrypted, 0, aesKeyDecrypted.length, "AES");
					
					// reconfigure cipher
					cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
				}
				
				// begin file transfer
				downloadFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		void downloadFile() throws Exception {
			int numBytes = 0;
			while (!connectionSocket.isClosed()) {
				int packetType = fromClient.readInt();
				if (packetType == 0) {
					// receive file name
					numBytes = fromClient.readInt();
					byte[] encryptedFileName = new byte[numBytes];
					fromClient.readFully(encryptedFileName, 0, numBytes);
					
					byte[] decryptedFileName = decryptData(encryptedFileName, sessionKey);
					// create output file and streams
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
						bufferedOutputStream.write(decryptedBlock, 0, numBytes);
					} 
				} else if (packetType == 2) {
					numBytes = fromClient.readInt();
					byte[] notifyComplete = new byte[numBytes];
					fromClient.readFully(notifyComplete, 0, numBytes);
					print("Message from Client: " + new String(notifyComplete));
					
					// close output streams
					print("Download complete, closing connection.");
					bufferedOutputStream.close();
					fileOutputStream.close();
					connectionSocket.close();
				}
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

		byte[] decryptData(byte[] block, Key key) throws Exception {
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(block);
		}
	}
	
	void print(String message) {
		System.out.println("Server Console -\n\t" + message);
	}
	
	public static void main(String[] args) throws Exception {
		ServerWithSecurity server = new ServerWithSecurity(PORT_NUMBER, CP);
		// ServerWithSecurity server = new ServerWithSecurity(Integer.parseInt(args[0]), 
		//		Integer.parseInt(args[1]));
		server.start();
	}
}
