package pt.sirs.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.BinaryCodec;

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
	
	public static BigInteger[] GeneratePandG(){
		BigInteger generatorValue,primeValue;
		primeValue = DeffieHellman.findPrime();// BigInteger.valueOf((long)g);
	    System.out.println("the prime is "+primeValue);
	    generatorValue	= DeffieHellman.findPrimeRoot(primeValue);//BigInteger.valueOf((long)p);
	    System.out.println("the generator of the prime is "+generatorValue);
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
	    System.out.println(key.length + "   key: " + key);
	    key = Arrays.copyOf(key, 16);
	    return  new SecretKeySpec(key,"AES");
	}
	
/*
  public static void DH() throws Exception {
	  	Random randomGenerator = new Random();
	    BigInteger generatorValue,primeValue,publicA,publicB,secretA,secretB,sharedKeyA,sharedKeyB;

	    primeValue = DeffieHellman.findPrime();// BigInteger.valueOf((long)g);
	    System.out.println("the prime is "+primeValue);
	    generatorValue	= DeffieHellman.findPrimeRoot(primeValue);//BigInteger.valueOf((long)p);
	    System.out.println("the generator of the prime is "+generatorValue);

		// on machine 1
	    secretA = new BigInteger(bitLength-2,randomGenerator);
		// on machine 2
	    secretB = new BigInteger(bitLength-2,randomGenerator);

		// to be published:
	    publicA=generatorValue.modPow(secretA, primeValue);
	    publicB=generatorValue.modPow(secretB, primeValue);
	    sharedKeyA = publicB.modPow(secretA,primeValue);// should always be same as:
	    sharedKeyB = publicA.modPow(secretB,primeValue);
//	    System.out.println(new String(Base64.encodeBase64(publicA.toByteArray())) + " LENG: " + new String(Base64.encodeBase64(publicA.toByteArray())).length());

	    System.out.println(" the public key of A is "+publicA);
	    System.out.println("the public key of B is "+publicB);
	    System.out.println("the shared key for A is "+sharedKeyA);
	    System.out.println("the shared key for B is "+sharedKeyB);
	    System.out.println("The secret key for A is "+secretA);
	    System.out.println("The secret key for B is "+secretB);

	    String getAValue=sharedKeyA.toString();
	    String getBValue=sharedKeyB.toString();
	    
	    byte [] key = getAValue.getBytes("UTF-8");
	    
	    MessageDigest sha = MessageDigest.getInstance("SHA-256");
	    key =  sha.digest(key);
	    System.out.println(key.length + "   key: " + key);
	    key = Arrays.copyOf(key, 16);
	    SecretKeySpec secretKeySpec =  new SecretKeySpec(key,"AES");

  	}*/
	
 }
