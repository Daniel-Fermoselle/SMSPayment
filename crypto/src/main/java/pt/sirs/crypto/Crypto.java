package pt.sirs.crypto;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Base64;

public class Crypto {
	
	public static String cipherSMS(String sms, Key sharedKey) throws Exception{
		byte[] cipherText;
		IvParameterSpec ivspec;
		
		ivspec = generateIV();
		Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		encryptCipher.init(Cipher.ENCRYPT_MODE, sharedKey, ivspec);
		cipherText = encryptCipher.doFinal(sms.getBytes());
		
		//Concatenate IV with cipherText
		byte[] IVCipherText = new byte[ivspec.getIV().length + cipherText.length];
		System.arraycopy(ivspec.getIV(), 0, IVCipherText, 0, ivspec.getIV().length);
		System.arraycopy(cipherText, 0, IVCipherText, ivspec.getIV().length, cipherText.length);
		
		String encodedCipherText = new String(Base64.encodeBase64(IVCipherText));
		return encodedCipherText;
	}
	
	public static String decipherSMS(String cipheredSms, Key sharedKey) throws Exception{
		byte[] decipherText, iv, msg;
		
		byte[] decodedCipheredSms =  Base64.decodeBase64(cipheredSms.getBytes());

		iv = Arrays.copyOfRange(decodedCipheredSms, 0, 16);
		msg = Arrays.copyOfRange(decodedCipheredSms, 16, decodedCipheredSms.length);
		System.out.println("MSG len " + msg.length);
		Cipher decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		decryptCipher.init(Cipher.DECRYPT_MODE, sharedKey, new IvParameterSpec(iv));
		decipherText = decryptCipher.doFinal(msg);
		return new String(decipherText);
	}

	public static Key getKeyFromKeyStore(String keystoreLocation, String keystorePass, String alias, String keyPass) throws Exception{

            InputStream keystoreStream = new FileInputStream(keystoreLocation);
            KeyStore keystore = KeyStore.getInstance("JCEKS");
            keystore.load(keystoreStream, keystorePass.toCharArray());
            if (!keystore.containsAlias(alias)) {
                throw new RuntimeException("Alias for key not found");
            }
            Key key = keystore.getKey(alias, keyPass.toCharArray());
            return key;
	}
	
	public static IvParameterSpec generateIV() throws NoSuchAlgorithmException{
		SecureRandom randomSecureRandom;
		IvParameterSpec ivParams;
		
		randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
		byte[] iv = new byte[16];
		randomSecureRandom.nextBytes(iv);
		ivParams = new IvParameterSpec(iv);
		
		return ivParams;
	}	

 }
