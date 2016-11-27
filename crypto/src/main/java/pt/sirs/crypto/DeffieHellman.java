package pt.sirs.crypto;

/***
 * This source code was not developed by us, it was developed by https://github.com/pannous
 * and the code can be found in the repository https://github.com/pannous/Diffie-Hellman/blob/master/DH.java
 */

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeffieHellman {
	private static int bitLength=512;	
	private static int certainty=20;
    private static final SecureRandom rnd = new SecureRandom();
    
	public static BigInteger findPrimeRoot(BigInteger p){
		int start= 2001;
		for(int i=start;i<100000000;i++)
			if(isPrimeRoot(BigInteger.valueOf(i),p))
				return BigInteger.valueOf(i);
		return BigInteger.valueOf(0);
	}


	public static BigInteger findPrime(){
		Random rnd=new Random();
		BigInteger p=BigInteger.ZERO;
		p= new BigInteger(bitLength, certainty, rnd);
		return p;
	}
	
	private static boolean isPrimeRoot(BigInteger g, BigInteger p)
	{
	    BigInteger totient = p.subtract(BigInteger.ONE);
	    List<BigInteger> factors = primeFactors(totient);
	    int i = 0;
	    int j = factors.size();
	    for(;i < j; i++)
	    {
	        BigInteger factor = factors.get(i);
	        BigInteger t = totient.divide( factor);
			if(g.modPow(t, p).equals(BigInteger.ONE))return false;
	    }
	    return true;
	}
	
	private static List<BigInteger> primeFactors(BigInteger number) {
	    BigInteger n = number;
		BigInteger i=BigInteger.valueOf(2);
		BigInteger limit=BigInteger.valueOf(10000);
	   	List<BigInteger> factors = new ArrayList<BigInteger>();
	   	while (!n.equals(BigInteger.ONE)){
			while (n.mod(i).equals(BigInteger.ZERO)){
	        factors.add(i);
			n=n.divide(i);
			if(isPrime(n)){
				factors.add(n);
				return factors;
			}
	     	}
			i=i.add(BigInteger.ONE);
			if(i.equals(limit))return factors;
		}
			System.out.println(factors);
	   return factors;
	 }
	
	private static boolean miller_rabin_pass(BigInteger a, BigInteger n) {
	    BigInteger n_minus_one = n.subtract(BigInteger.ONE);
	    BigInteger d = n_minus_one;
		int s = d.getLowestSetBit();
		d = d.shiftRight(s);
	    BigInteger a_to_power = a.modPow(d, n);
	    if (a_to_power.equals(BigInteger.ONE)) return true;
	    for (int i = 0; i < s-1; i++) {
	        if (a_to_power.equals(n_minus_one)) return true;
	        a_to_power = a_to_power.multiply(a_to_power).mod(n);
	    }
	    if (a_to_power.equals(n_minus_one)) return true;
	    return false;
	}

	public static boolean miller_rabin(BigInteger n) {
	    for (int repeat = 0; repeat < 20; repeat++) {
	        BigInteger a;
	        do {
	            a = new BigInteger(n.bitLength(), rnd);
	        } while (a.equals(BigInteger.ZERO));
	        if (!miller_rabin_pass(a, n)) {
	            return false;
	        }
	    }
	    return true;
	}

	private static boolean isPrime(BigInteger r){
		return miller_rabin(r);
	}

}
