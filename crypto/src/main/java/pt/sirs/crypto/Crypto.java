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
	
	public static byte[] cipherSMS(String sms, Key sharedKey, IvParameterSpec ivspec) throws Exception{
		byte[] cipherText;
		
		Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		encryptCipher.init(Cipher.ENCRYPT_MODE, sharedKey, ivspec);
		cipherText = encryptCipher.doFinal(sms.getBytes());
		
		return cipherText;
	}
	
	public static String decipherSMS(byte[] msg, Key sharedKey, IvParameterSpec ivspec) throws Exception{
		byte[] decipherText;
		
		Cipher decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		decryptCipher.init(Cipher.DECRYPT_MODE, sharedKey, ivspec);
		decipherText = decryptCipher.doFinal(msg);
		
		return new String(decipherText);
	}
	
	public static String encode(byte[] msg){
		String encodedMsg = new String(Base64.encodeBase64(msg));
		
		return encodedMsg;
	}
	
	public static byte[] decode(String msg){
		byte[] decodedCipheredSms =  Base64.decodeBase64(msg.getBytes());
		
		return decodedCipheredSms;
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
