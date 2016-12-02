package pt.sirs.crypto;


import javax.crypto.BadPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;



public class CryptoTest extends AbstractCryptoTest{
  
	private  static final SecretKeySpec SHARED_KEY1 = new SecretKeySpec(Crypto.decode("Y3gNMPdxtqc6F0YCJKu2gg=="),"AES");

	private  static final SecretKeySpec SHARED_KEY2 = new SecretKeySpec(Crypto.decode("RLai0vYrXHkQFrDecgvSTw=="),"AES");

	private static final String PLAINTEXT_1 = "sigmaJEM|100|0";
	private static final String PLAINTEXT_2 = "nasTyMSR|100|0";
	
	private static final String PT_1_CIPHERED_K1 = "7LYcFbNpyHrQ8dXcCeCK+w==";
	private static final String PT_1_CIPHERED_K1_CORRUPTED = "7LYcFbNpyHrQ8dXcCEcK+w==";

	private static final String PT_2_CIPHERED_K1 = "DjW1EA3wd2TTlIeJeY5Hbg==";


	@Override
	protected void populate() {

	}

	
	@Test
	public void cipher_success() throws Exception {
		byte cipher[] = Crypto.cipherSMS(PLAINTEXT_1, SHARED_KEY1);
		assertEquals(PT_1_CIPHERED_K1,Crypto.encode(cipher));
	}
	
	@Test
	public void cipher_different_key() throws Exception {
		byte cipher[] = Crypto.cipherSMS(PLAINTEXT_1, SHARED_KEY2);
		assertNotEquals(PT_1_CIPHERED_K1,Crypto.encode(cipher));
	}
	
	@Test
	public void cipher_different_plaintext() throws Exception {
		byte cipher[] = Crypto.cipherSMS(PLAINTEXT_2, SHARED_KEY1);
		assertNotEquals(PT_1_CIPHERED_K1,Crypto.encode(cipher));
	}
	
	@Test
	public void cipher_success_K2() throws Exception {
		byte cipher[] = Crypto.cipherSMS(PLAINTEXT_2, SHARED_KEY1);
		assertEquals(PT_2_CIPHERED_K1,Crypto.encode(cipher));
	}
	
	@Test
	public void decipher_success() throws Exception {
		String deciphered = Crypto.decipherSMS(Crypto.decode(PT_1_CIPHERED_K1), SHARED_KEY1);
		assertEquals(PLAINTEXT_1, deciphered);
	}
	
	@Test
	public void decipher_different_pl() throws Exception {
		String deciphered = Crypto.decipherSMS(Crypto.decode(PT_2_CIPHERED_K1), SHARED_KEY1);
		assertNotEquals(PLAINTEXT_1, deciphered);
	}
	
	@Test (expected = BadPaddingException.class)
	public void decipher_different_key() throws Exception {
		Crypto.decipherSMS(Crypto.decode(PT_1_CIPHERED_K1), SHARED_KEY2);
	}
	
	@Test (expected = BadPaddingException.class)
	public void decipher_corrupted_cipher() throws Exception {
		Crypto.decipherSMS(Crypto.decode(PT_1_CIPHERED_K1_CORRUPTED), SHARED_KEY1);
	}
	
}
