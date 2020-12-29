package com.learning.akka.basics;

import java.math.BigInteger;
import java.util.Random;

public class ConcurrentPrimeGenerator implements Runnable{

	private Result result;
	public ConcurrentPrimeGenerator(Result result) {
		this.result=  result;
	}
	
	public Result getResult() {
		return result;
	}

	@Override
	public void run() {
		BigInteger current = new BigInteger(2000, new Random());
		//System.out.println("current biginteger "+current);
		BigInteger nextProbablePrime = current.nextProbablePrime();
		//System.out.println("next probable biginteger "+nextProbablePrime);
		result.addPrime(nextProbablePrime);
	}
	
	
}
