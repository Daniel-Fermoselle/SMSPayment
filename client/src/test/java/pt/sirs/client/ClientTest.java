package pt.sirs.client;

import static org.junit.Assert.assertEquals;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

import pt.sirs.crypto.Crypto;

public class ClientTest extends AbstractClientTest{
  
	private  static final String MOBILE1 = "911111111";
	private  static final String USERNAME1 = "nasTyMSR";
	private  static final String PASSWORD1 = "12345";
	private  static final String MOBILE2 = "914444444";
	private  static final String USERNAME2 = "alpha";
	private  static final String PASSWORD2 = "12345";
	private  static final String MOBILE3 = "917777777";
	private  static final String USERNAME3 = "austrolopi";
	private  static final String PASSWORD3 = "1234567";

	private  static final SecretKeySpec SHARED_KEY1 = new SecretKeySpec(Crypto.decode("FQsZUeftgb4uPM8o7krQkw=="),"AES");
	private  static final SecretKeySpec SHARED_KEY2 = new SecretKeySpec(Crypto.decode("RLai0vYrXHkQFrDecgvSTw=="),"AES");

	private  static final String U1_WELL_FORMED_LOGIN_SMS_FEEDBACK = "MCwCFDiIOg0WuNYVA0EGQJ3fBPzY3fy7AhQ3PZIvZOc7j48Yy0DF5HbKtc8lBA==|PhZu9XMegjszEMAIVaD/Cg==";
	private  static final String U1_BAD_SIG_LOGIN_SMS_FEEDBACK = "MCwCFDiIOg0WuNYVA0EGQJ3fBPzY3fy7AhQ3PZIvZOc7j48Yy0DF5HbKtC8lBA==|PhZu9XMegjszEMAIVaD/Cg==";
	private  static final String U1_BAD_CIPHER_LOGIN_SMS_FEEDBACK = "MCwCFDiIOg0WuNYVA0EGQJ3fBPzY3fy7AhQ3PZIvZOc7j48Yy0DF5HbKtc8lBA==|PhZu9xMegjszEMAIVaD/Cg==";
	
	private  static final String U1_WELL_FORMED_TRANSACTION_SMS_FEEDBACK = "MC0CFQCnHUjTJkLCYlJfTzmslhaNv0L3JQIUJc7IBPnVynfgF4K4ckqShIBkd7s=|sVCRmCiZ+ePE4Pm/A/s+Tg==";
	private  static final String U1_BAD_SIG_TRANSACTION_SMS_FEEDBACK = "MC0CFQCnHUjTJkLCYlJfTzmslhaNv0L3JQIUJc7IBPnVynfgF4K4ckQShIBkd7s=|sVCRmCiZ+ePE4Pm/A/s+Tg==";
	private  static final String U1_BAD_CIPHERD_TRANSACTION_SMS_FEEDBACK = "MC0CFQCnHUjTJkLCYlJfTzmslhaNv0L3JQIUJc7IBPnVynfgF4K4ckqShIBkd7s=|sVCRMCiZ+ePE4Pm/A/s+Tg==";
	
	private  static final String U1_WELL_FORMED_LOGOUT_SMS_FEEDBACK = "MC4CFQDE2McfPKFV0FhxMDvR4Jf/V80xxAIVAJgz7Ej3jr6sLLwUkno/YZzeRwpK|u0Ju3xEvbVFCaRWH27wbpA==";
	private  static final String U1_BAD_SIG_LOGOUT_SMS_FEEDBACK = "MC4CFQDE2McfPKFV0FhxMDvR4Jf/V80xxAIVAJgz7Ej3jr6sLLwUkno/YzzeRwpK|u0Ju3xEvbVFCaRWH27wbpA==";
	private  static final String U1_BAD_CIPHERD_LOGOUT_SMS_FEEDBACK = "MC4CFQDE2McfPKFV0FhxMDvR4Jf/V80xxAIVAJgz7Ej3jr6sLLwUkno/YZzeRwpK|u0Ju3xEvbvFCaRWH27wbpA==";
	
	private  static final String STATE_LOGIN_FEEDBACK = "login";
	private  static final String STATE_TRANSACTION_FEEDBACK = "transaction";
	private  static final String STATE_LOGOUT_FEEDBACK = "logout";
	private  static final String RETURN_LOGIN_FEEDBACK = "LoginOk";
	private  static final String BAD_RETURN_LOGIN_FEEDBACK = "ChamPog";
	private  static final String BAD_FRESHNESS_LOGIN_FEEDBACK = "FreshKo";
	private  static final String RETURN_TRANSACTION_FEEDBACK = "TransOk";
	private  static final String RETURN_LOGOUT_FEEDBACK = "LogoutOk";

	Client client1;
	Client client2;
	Client client3;

	@Override
	protected void populate() {
		try {
			client1 = new Client(USERNAME1, PASSWORD1, MOBILE1);
			client2 = new Client(USERNAME2, PASSWORD2, MOBILE2);
			client3 = new Client(USERNAME3, PASSWORD3, MOBILE3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		client1.setSharedKey(SHARED_KEY1);
		client2.setSharedKey(SHARED_KEY2);
	}

	
	@Test
	public void login_feedback_sms_success() throws Exception {
		String result = client1.processFeedback(U1_WELL_FORMED_LOGIN_SMS_FEEDBACK, STATE_LOGIN_FEEDBACK);
		assertEquals(RETURN_LOGIN_FEEDBACK, result);
	}
	
	@Test
	public void bad_sig_login_feedback_sms_success() throws Exception {
		String result = client1.processFeedback(U1_BAD_SIG_LOGIN_SMS_FEEDBACK, STATE_LOGIN_FEEDBACK);
		assertEquals(BAD_RETURN_LOGIN_FEEDBACK, result);
	}
	
	@Test
	public void bad_cipher_login_feedback_sms_success() throws Exception {
		String result = client1.processFeedback(U1_BAD_CIPHER_LOGIN_SMS_FEEDBACK, STATE_LOGIN_FEEDBACK);
		assertEquals(BAD_RETURN_LOGIN_FEEDBACK, result);
	}
	
	@Test
	public void transaction_feedback_sms_success() throws Exception {
		String result = client1.processFeedback(U1_WELL_FORMED_TRANSACTION_SMS_FEEDBACK, STATE_TRANSACTION_FEEDBACK);
		assertEquals(RETURN_TRANSACTION_FEEDBACK, result);
	}
	
	@Test
	public void bad_sig_transaction_feedback_sms_success() throws Exception {
		String result = client1.processFeedback(U1_BAD_SIG_TRANSACTION_SMS_FEEDBACK, STATE_TRANSACTION_FEEDBACK);
		assertEquals(BAD_RETURN_LOGIN_FEEDBACK, result);
	}

	@Test
	public void bad_cipher_transaction_feedback_sms_success() throws Exception {
		String result = client1.processFeedback(U1_BAD_CIPHERD_TRANSACTION_SMS_FEEDBACK, STATE_TRANSACTION_FEEDBACK);
		assertEquals(BAD_RETURN_LOGIN_FEEDBACK, result);
	}
	
	@Test
	public void bad_freshess_transaction_feedback_sms_success() throws Exception {
		client1.setCounter(5);
		String result = client1.processFeedback(U1_WELL_FORMED_TRANSACTION_SMS_FEEDBACK, STATE_TRANSACTION_FEEDBACK);
		assertEquals(BAD_FRESHNESS_LOGIN_FEEDBACK, result);
	}
	
	@Test
	public void logout_feedback_sms_success() throws Exception {
		String result = client1.processFeedback(U1_WELL_FORMED_LOGOUT_SMS_FEEDBACK, STATE_LOGOUT_FEEDBACK);
		assertEquals(RETURN_LOGOUT_FEEDBACK, result);
	}
	
	@Test
	public void logout_feedback_sms_with_other_user() throws Exception {
		String result = client2.processFeedback(U1_WELL_FORMED_LOGOUT_SMS_FEEDBACK, STATE_LOGOUT_FEEDBACK);
		assertEquals(BAD_RETURN_LOGIN_FEEDBACK, result);
	}
	
	@Test
	public void bad_sig_logout_feedback_sms_success() throws Exception {
		String result = client1.processFeedback(U1_BAD_SIG_LOGOUT_SMS_FEEDBACK, STATE_LOGOUT_FEEDBACK);
		assertEquals(BAD_RETURN_LOGIN_FEEDBACK, result);
	}

	@Test
	public void bad_cipher_logout_feedback_sms_success() throws Exception {
		String result = client1.processFeedback(U1_BAD_CIPHERD_LOGOUT_SMS_FEEDBACK, STATE_LOGOUT_FEEDBACK);
		assertEquals(BAD_RETURN_LOGIN_FEEDBACK, result);
	}
	
}
