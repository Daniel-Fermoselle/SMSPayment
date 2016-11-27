package pt.sirs.crypto;

import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import pt.sirs.crypto.DeffieHellman;

public class Crypto {
	private static int bitLength=512;	
	
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

	public static IvParameterSpec generateIV() throws NoSuchAlgorithmException{
		SecureRandom randomSecureRandom;
		IvParameterSpec ivParams;
		
		randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
		byte[] iv = new byte[16];
		randomSecureRandom.nextBytes(iv);
		ivParams = new IvParameterSpec(iv);
		
		return ivParams;
	}	
	
	public static BigInteger[] GeneratePandG(){
		BigInteger generatorValue,primeValue;
		primeValue = DeffieHellman.findPrime();
	    generatorValue	= DeffieHellman.findPrimeRoot(primeValue);
	    BigInteger[] toReturn = new BigInteger[2];
	    toReturn[0] = primeValue;
	    toReturn[1] = generatorValue;
	    return toReturn;
	}
	
	public static BigInteger generateSecretValue(){
	  	Random randomGenerator = new Random();
	    return new BigInteger(bitLength-2,randomGenerator);
	}	

	public static SecretKeySpec generateKeyFromBigInt(BigInteger sharedKey) throws Exception{
		String getAValue = sharedKey.toString();
	    byte [] key = getAValue.getBytes("UTF-8");
	    
	    MessageDigest sha = MessageDigest.getInstance("SHA-256");
	    key =  sha.digest(key);
	    key = Arrays.copyOf(key, 16);
	    return  new SecretKeySpec(key,"AES");
	}
	
 }
