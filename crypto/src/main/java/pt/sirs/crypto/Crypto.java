package pt.sirs.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.pdfbox.io.ASCII85InputStream;
import org.apache.pdfbox.io.ASCII85OutputStream;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.util.encoders.Hex;

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
	
	 public static byte[] decode(String ascii85) {
		    ArrayList<Byte> list = new ArrayList<Byte>();
		    ByteArrayInputStream in_byte = null;
		    try {
		        in_byte = new ByteArrayInputStream(ascii85.getBytes("ascii"));
		    } catch (UnsupportedEncodingException e) {
		        e.printStackTrace();
		    }
		    ASCII85InputStream in_ascii = new ASCII85InputStream(in_byte);
		    try {
		        int r ;
		        while ((r = in_ascii.read()) != -1) {
		            list.add((byte) r);
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		    byte[] bytes = new byte[list.size()];
		    for (int i = 0; i < bytes.length; i++) {
		        bytes[i] = list.get(i);
		    }
		    return bytes;
		}


		public static String encode(byte[] bytes) {
		    ByteArrayOutputStream out_byte = new ByteArrayOutputStream();
		    ASCII85OutputStream  out_ascii = new ASCII85OutputStream(out_byte);

		    try {
		        out_ascii.write(bytes);
		        out_ascii.flush();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		    String res = "";
		    try {
		        res = out_byte.toString("ascii");
		    } catch (UnsupportedEncodingException e) {
		        e.printStackTrace();
		    }
		    return res;
		}

		public static void Run() throws Exception{
			KeyPair pair = GenerateKeys();
			Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", "BC");
			ecdsaSign.initSign(pair.getPrivate());
			ecdsaSign.update("olaolaolaolaolaolaolaolaolaolaolaolaolaolaolaolaola".getBytes());
			byte[] signature = ecdsaSign.sign();
			System.out.println(signature.length + "inaidjasdnasdansdiasndiasndiasidnsaisa");


			Signature mySign = Signature.getInstance("SHA256withECDSA", "BC");
			mySign.initVerify(pair.getPublic());
			mySign.update("olaolaolaolaolaolaolaolaolaolaolaolaolaolaolaolaola".getBytes());
			if(mySign.verify(signature)){
				System.out.println(encode(pair.getPublic().getEncoded()) + "O MARCAL E MUITA CHATO" + encode(pair.getPublic().getEncoded()).length());
				PublicKey publicKey = KeyFactory.getInstance("ECDSA", "BC").generatePublic(new X509EncodedKeySpec(decode(encode(pair.getPublic().getEncoded()))));
				Signature s = Signature.getInstance("SHA256withECDSA", "BC");
				s.initVerify(publicKey);
				s.update("olaolaolaolaolaolaolaolaolaolaolaolaolaolaolaolaola".getBytes());
				if(s.verify(signature)){
					System.out.println("O MARÇAL É MEU AMIGO!");
				}
			}
		}
		
		
		public static KeyPair GenerateKeys() throws Exception{
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			ECGenParameterSpec     ecGenSpec = new ECGenParameterSpec("brainpoolp160t1");
			KeyPairGenerator    g = KeyPairGenerator.getInstance("ECDSA", "BC");
			g.initialize(ecGenSpec, new SecureRandom());
			KeyPair pair = g.generateKeyPair();
			return pair;
		}
 }
