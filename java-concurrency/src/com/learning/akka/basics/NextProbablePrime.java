package com.learning.akka.basics;

import java.math.BigInteger;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

public class NextProbablePrime {

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		SortedSet<BigInteger> primes = new TreeSet<BigInteger>();
		
		while(primes.size() < 100) {
			BigInteger current = new BigInteger(2000, new Random());
			//System.out.println("current biginteger "+current);
			BigInteger nextProbablePrime = current.nextProbablePrime();
			//System.out.println("next probable biginteger "+nextProbablePrime);
			primes.add(nextProbablePrime);
		}
		System.out.println("final set "+primes.size());
		long end = System.currentTimeMillis();
		System.out.println("total time taken "+(end-start)+" ms.");
	}

}
