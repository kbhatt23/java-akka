package com.learning.akka.basics;

import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;

public class Result {

	private SortedSet<BigInteger> primes;
	public Result() {
		primes = new TreeSet<>();
	}
	
	
	public SortedSet<BigInteger> getPrimes(){
		return primes;
	}
	
	//differnet thread are printing the items whil other threads are adding itmes in the set, hence synchronized
	public synchronized void addPrime(BigInteger prime) {
		primes.add(prime);
	}
	//differnet thread are printing the items whil other threads are adding itmes in the set, hence synchronized
	public synchronized void printItems() {
		getPrimes().forEach(System.out::println);
	}
}
