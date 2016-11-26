package pt.sirs.crypto;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;

import javax.crypto.Cipher;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;

public class Crypto {
	
	public static String cipherSMS(String sms, Key sharedKey) throws Exception{
		byte[] cipherText;
		
		Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		encryptCipher.init(Cipher.ENCRYPT_MODE, sharedKey);
		cipherText = encryptCipher.doFinal(parseBase64Binary(sms));
		return printBase64Binary(cipherText);
	}
	
	public static String decipherSMS(byte[] cipheredSms, Key sharedKey) throws Exception{
		byte[] decipherText;
		
		Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		encryptCipher.init(Cipher.DECRYPT_MODE, sharedKey);
		decipherText = encryptCipher.doFinal(cipheredSms);
		return printBase64Binary(decipherText);
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

 }
