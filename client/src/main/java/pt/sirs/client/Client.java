package pt.sirs.client;

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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pt.sirs.smsPacket.smsPacket;
import pt.sirs.smsPacket.Exceptions.InvalidSMSPacketValuesException;

public class Client {
	private String myIban;
	private int myMoney; //Nao iremo usar isto na versao basica. Vamos comecar com ints e depois se quisermos mudamos para floats/doubles
	
	public Client(String mib, int mm) {
		myIban=mib;
		myMoney=mm;
	}
	
	public void setMyIban(String mib){
		myIban=mib;
	}
	
	public String getMyIban(){
		return myIban;
	}
	
	public void setMyMoney(int mm){
		myMoney=mm;
	}
	
	public int getMyMoney(){
		return myMoney;
	}
	
	
	public smsPacket getSmsPacket(String OtherIban, String amount){
		smsPacket sms=null;//CUIDADO
		try {
			sms = new smsPacket(myIban,OtherIban,amount);

			File file = new File("keys/client.jks");//MUDAR QUANDO NECESSARIO
			FileInputStream is = new FileInputStream(file);
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

            /*Information for certificate to be generated */ 
            String password = "SIRS1617";
            String alias = "example";
            keystore.load(is, password.toCharArray());
            PrivateKey key = (PrivateKey)keystore.getKey(alias, password.toCharArray());
			
			sms.setSignature(key);
            
            return sms;
		} 
//TODO REVER TODOS OS CATCH		
		catch (InvalidSMSPacketValuesException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return sms;//CUIDADO
		} 
		
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return sms;//CUIDADO
		} 
		
		catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return sms;//CUIDADO
		} 
		
		catch (UnrecoverableKeyException e) {
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
	}

	public byte[] getToSend(smsPacket sms) {
		String smsToSend = sms.cipherMobile();
		byte[] cipherText = null;

		try {
			File file = new File("keys/server.cer");//MUDAR QUANDO NECESSARIO
			FileInputStream is;
			
				is = new FileInputStream(file);
			
			
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
	
	        /*Information for certificate to be generated */ 
	        String password = "SIRS1617";
	        String alias = "example";
	        keystore.load(is, password.toCharArray());
			Certificate cert = keystore.getCertificate(alias); 
			
		      final Cipher cipher = Cipher.getInstance("RSA");
		      // encrypt the plain text using the public key
		      cipher.init(Cipher.ENCRYPT_MODE, cert.getPublicKey());
		      cipherText = cipher.doFinal(smsToSend.getBytes());
			
			return cipherText;
		}
//TODO REVER TODOS OS CATCH	
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		return cipherText;//CUIDADO
		} 
		
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return cipherText;//CUIDADO
		}
		
		catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return cipherText;//CUIDADO
		} 
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return cipherText;//CUIDADO
		} 
		
		catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return cipherText;//CUIDADO
		} 
		
		catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return cipherText;//CUIDADO
		} 
		
		catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return cipherText;//CUIDADO
		} 
		
		catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return cipherText;//CUIDADO
		} 

		catch(BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return cipherText;//CUIDADO
		}
	}
}
	
