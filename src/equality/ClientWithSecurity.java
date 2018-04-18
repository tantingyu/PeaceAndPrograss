package equality;

/* Programming Assignment 2
 * Authors: Tan Ting Yu (1002169) and Chong Lok Swen (1002468)
 * Date: 17/4/2018
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

public class ClientWithSecurity extends Thread {
	
	static final String CA_CERT = "CA.crt";

	Socket clientSocket;
	DataOutputStream toServer;
	DataInputStream fromServer;
	String fileName;
	
	int cp = 1;
	Key sessionKey; // RSA public key, or AES symmetric key
	Cipher cipher;
	
	public ClientWithSecurity(String serverIP, int portNumber, String fileName, int cp) throws Exception {
		clientSocket = new Socket(serverIP, portNumber);
		fromServer = new DataInputStream(clientSocket.getInputStream());
		toServer = new DataOutputStream(clientSocket.getOutputStream());
		this.fileName = fileName;
		this.cp = cp;
	}
	
	@Override
	public void run() {
		long timeStarted = System.nanoTime();
		try {
			// ------ AUTHENTICATION PROTOCOL ------ //
			
			// request server's certificate
			print("Requesting server's certificate");
			sendMessage("Please send me your certificate.".getBytes(), -1);
			
			// receive server's certificate
			byte[] serverCert = receiveMessage();
			print("Received server's certificate");
			
			// verify server's certificate using CA's public key and extract server's
			// public key
			sessionKey = verifyCertificateExtractPublicKey(serverCert);
			if (sessionKey == null) {
				// notify certificate verification failed
				print("Certificate verification failed, aborting");
				sendMessage("Certificate invalid, bye.".getBytes(), -2);
				return;
			}
			
			// return server's handshake
			print("Server's certificate verified, returning handshake");
			sendMessage("Your certificate has been verified.".getBytes(), 0);

			// ------ CONFIDENTIALTIY PROTOCOL ------ //
			
			// configure cipher
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			
			// blocks will be encrypted using client's AES symmetric key
			if (cp == 2) {
				aesProtocol();
			}
			
			// ------ FILE TRANSFER ------ //
			
			print("Length of File: " + new File(fileName).length());
			
			// send file name
			print("Starting secure file transfer, sending file name");
			sendMessage(encryptData(fileName.getBytes(), sessionKey), 0);

			// upload file
			uploadFile();
			
			// notify upload complete
			print("Upload complete, notifying server");
			sendMessage("File upload complete.".getBytes(), 2);
			
			// receive notification that server has completed download
			print("Message from Server: " + new String(receiveMessage()));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
		
		long timeTaken = System.nanoTime() - timeStarted;
		print("Transfer Time: " + timeTaken/1000000.0 + " ms");
	}
	
	Key verifyCertificateExtractPublicKey(byte[] serverCertBytes) {
		try {
			// get public key from CA
			Certificate caCert = CertificateFactory.getInstance("X.509")
					.generateCertificate(new FileInputStream(CA_CERT));
			Key caPublicKey = caCert.getPublicKey();
			
			// verify server's certificate
			X509Certificate serverCert = (X509Certificate) CertificateFactory.getInstance("X.509")
					.generateCertificate(new ByteArrayInputStream(serverCertBytes));
			serverCert.checkValidity();
			serverCert.verify((PublicKey) caPublicKey);
	
			return serverCert.getPublicKey();
		} catch (Exception e) {
			return null;
		}
	}
	
	void aesProtocol() throws Exception {
		// receive AES key request
		print("Message from Server: " + new String(receiveMessage()));
		
		// generate AES symmetric key for file transfer
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(128);
		Key aesKey = keyGen.generateKey();
		
		// encrypt AES symmetric key with RSA public key and send to server
		print("Sending encrypted AES symmetric key to server");
		sendMessage(encryptData(aesKey.getEncoded(), sessionKey), -1);
		
		// reconfigure cipher and set session key
		cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		sessionKey = aesKey;
	}
	
	void uploadFile() throws Exception {
		// initialize input streams
		FileInputStream fileInputStream = new FileInputStream(System.getProperty("user.dir") 
				+ "/" + fileName);
		BufferedInputStream bufferedFileInputStream = 
				new BufferedInputStream(fileInputStream);
		byte[] fromFileBuffer = new byte[117];
		int numBytes = 0;
		
		// send file data in blocks
        for (boolean fileEnded = false; !fileEnded;) {
			numBytes = bufferedFileInputStream.read(fromFileBuffer);
			fileEnded = numBytes < fromFileBuffer.length;
			
			byte[] encryptedData = encryptData(fromFileBuffer, sessionKey);
			toServer.writeInt(1);
			toServer.writeInt(numBytes);
			toServer.write(encryptedData);
			toServer.flush();
		}

        // close input streams
        bufferedFileInputStream.close();
        fileInputStream.close();
	}
	
	byte[] receiveMessage() throws Exception {
		int numBytes = fromServer.readInt();
		byte[] message = new byte[numBytes];
		fromServer.readFully(message, 0, numBytes);
		return message;
	}

	void sendMessage(byte[] message, int packetType) throws Exception {
		if (packetType != -1) {
			toServer.writeInt(packetType);
		}
		toServer.writeInt(message.length);
		toServer.write(message);
		toServer.flush();
	}
	
	byte[] encryptData(byte[] block, Key key) throws Exception {
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(block);
	}
	
	void closeConnection() {
		try {
			print("Closing connection");
			clientSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void print(String message) {
		System.out.println("Client Console >> " + message);
	}
	
	public static void main(String[] args) throws Exception {
		ClientWithSecurity client = new ClientWithSecurity(args[0], Integer.parseInt(args[1]), 
				args[2], Integer.parseInt(args[3]));
		client.start();
	}
}