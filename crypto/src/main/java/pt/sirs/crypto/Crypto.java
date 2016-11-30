package pt.sirs.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;

import java.security.spec.PKCS8EncodedKeySpec;

import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


import pt.sirs.crypto.DeffieHellman;

public class Crypto {
	private static int bitLength=512;	
	
	public static byte[] cipherSMS(String sms, Key sharedKey) throws Exception{
		byte[] cipherText;
		
		Cipher encryptCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		encryptCipher.init(Cipher.ENCRYPT_MODE, sharedKey);
		cipherText = encryptCipher.doFinal(sms.getBytes());
		
		return cipherText;
	}
	
	public static String decipherSMS(byte[] msg, Key sharedKey) throws Exception{
		byte[] decipherText;
		
		Cipher decryptCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		decryptCipher.init(Cipher.DECRYPT_MODE, sharedKey);
		decipherText = decryptCipher.doFinal(msg);
		
		return new String(decipherText);
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
	
	public static String encode(byte[] msg){
		String encodedMsg = new String(Base64.encodeBase64(msg));
		
		return encodedMsg;
	}
	
	public static byte[] decode(String msg){
		byte[] decodedCipheredSms =  Base64.decodeBase64(msg.getBytes());
		
		return decodedCipheredSms;
	}
		
	public static byte[] sign(String msg, PrivateKey privKey) throws Exception{
		Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", "BC");
		ecdsaSign.initSign(privKey);
		ecdsaSign.update(msg.getBytes());
		return ecdsaSign.sign();
	}
	
	public static boolean verifySign(String msg, byte[] signature, PublicKey pubKey) throws Exception {
		Signature s = Signature.getInstance("SHA256withECDSA", "BC");
		s.initVerify(pubKey);
		s.update(msg.getBytes());
		return s.verify(signature);
	}
	
	public static PublicKey getPubKeyFromByte(byte[] bytePubKey) throws Exception{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		return KeyFactory.getInstance("ECDSA", "BC").generatePublic(new X509EncodedKeySpec(bytePubKey));
	}
	
	public static PrivateKey getPrivKeyFromByte(byte[] bytePrivKey) throws Exception{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		return KeyFactory.getInstance("ECDSA", "BC").generatePrivate(new PKCS8EncodedKeySpec(bytePrivKey));
	}
	
	public static KeyPair GenerateKeys() throws Exception{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		ECGenParameterSpec ecGenSpec = new ECGenParameterSpec("brainpoolp160t1");
		KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");
		g.initialize(ecGenSpec, new SecureRandom());
		KeyPair pair = g.generateKeyPair();
		return pair;
	}
	
	public static void saveKeyInFile(Key Key, String filename) throws Exception{
		/* save the public key in a file */
		byte[] key = Key.getEncoded();
		FileOutputStream keyfos = new FileOutputStream(filename);
		keyfos.write(key);
		keyfos.close();
	}
	
	public static PublicKey readPubKeyFromFile(String filename) throws Exception{
		File f = new File(filename);
	    FileInputStream fis = new FileInputStream(f);
	    DataInputStream dis = new DataInputStream(fis);
	    byte[] keyBytes = new byte[(int) f.length()];
	    dis.readFully(keyBytes);
	    dis.close();
	    
	    return getPubKeyFromByte(keyBytes);
	}
	
	public static PrivateKey readPrivKeyFromFile(String filename) throws Exception{
		File f = new File(filename);
	    FileInputStream fis = new FileInputStream(f);
	    DataInputStream dis = new DataInputStream(fis);
	    byte[] keyBytes = new byte[(int) f.length()];
	    dis.readFully(keyBytes);
	    dis.close();
	    
	    return getPrivKeyFromByte(keyBytes);
	}
	
	public static void main(String args[]) throws Exception{
		KeyPair keyPair;
		for(String entity : args){
			keyPair = GenerateKeys();
			saveKeyInFile(keyPair.getPrivate(), "PrivateKey" + entity);
			saveKeyInFile(keyPair.getPublic(), "PublicKey" + entity);
		}			
	}
 }
