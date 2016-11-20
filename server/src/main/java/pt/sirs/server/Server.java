package pt.sirs.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pt.sirs.smsPacket.smsPacket;

public class Server {
	
	private ArrayList<Account> accounts;
	
    public Server(){
    	this.accounts = new ArrayList<Account>();
    }    
    
    public smsPacket getMessage(byte[] smsReceived) {
		smsPacket sms=null;

		try {
			File file = new File("/home/daniel/Desktop/SIRS-1617/SMSPayment/server/keys/server.jks");//MUDAR QUANDO NECESSARIO
			FileInputStream is = new FileInputStream(file);
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

            /*Information for certificate to be generated */ 
            String password = "SIRS1617";
            String alias = "example";
            keystore.load(is, password.toCharArray());
            PrivateKey key = (PrivateKey)keystore.getKey(alias, password.toCharArray()); 
			
		      final Cipher cipher = Cipher.getInstance("RSA");
		      // encrypt the plain text using the public key
		      cipher.init(Cipher.DECRYPT_MODE, key);
		      byte[] toDecipher = cipher.doFinal(smsReceived);
		      
		      
		      File file2 = new File("/home/daniel/Desktop/SIRS-1617/SMSPayment/server/keys/client.cer");//MUDAR QUANDO NECESSARIO
				FileInputStream is2;
				
					is2 = new FileInputStream(file2);
				
				
				KeyStore keystore2 = KeyStore.getInstance(KeyStore.getDefaultType());
		        keystore2.load(is2, password.toCharArray());
				Certificate cert = keystore.getCertificate(alias); 
		      
		      
		      String  decipher = new String(toDecipher);
		      sms=sms.decipherMobile(decipher, cert.getPublicKey());
		      
			
			return sms;
		}
		//TODO REVER TODOS OS CATCH	
				catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();	
				return sms;//CUIDADO
				} 
				
				catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return sms;//CUIDADO
				}
				
				catch (CertificateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return sms;//CUIDADO
				} 
				
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return sms;//CUIDADO
				} 
				
				catch (KeyStoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return sms;//CUIDADO
				} 
				
				catch (InvalidKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return sms;//CUIDADO
				} 
				
				catch (NoSuchPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return sms;//CUIDADO
				} 
				
				catch (IllegalBlockSizeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return sms;//CUIDADO
				} 

				catch(BadPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return sms;//CUIDADO
				} catch (UnrecoverableKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return sms;//CUIDADO
				}
    }
    
}