package com.learning.akka.basics;

public class ConcurrentResultMonitor implements Runnable{

	private Result result;
	public ConcurrentResultMonitor(Result result) {
		this.result=  result;
	}
	
	public Result getResult() {
		return result;
	}

	@Override
	public void run() {
		while(result.getPrimes().size() < 100) {
			System.out.println("Got Items "+result.getPrimes().size()+" so far...........");
			result.printItems();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
}
