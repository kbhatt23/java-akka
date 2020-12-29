package com.learning.akka.basics;

import java.util.ArrayList;
import java.util.List;

public class ConcurrentPrimeGeneratorMain {
public static void main(String[] args) throws InterruptedException {
	long start = System.currentTimeMillis();
	Result result = new Result();
	ConcurrentResultMonitor monitor =new ConcurrentResultMonitor(result);
	new Thread(monitor).start();
	ConcurrentPrimeGenerator runnable = new ConcurrentPrimeGenerator(result);
	List<Thread> threads=  new ArrayList<>();
	for(int i=0 ; i < 100; i++) {
		Thread t = new Thread(runnable);
		t.start();
		threads.add(t);
	}
	
	for(Thread t : threads) {
		t.join();
	}
	System.out.println("final set "+result.getPrimes().size());
	long end = System.currentTimeMillis();
	System.out.println("total time taken "+(end-start)+" ms.");
}
}
