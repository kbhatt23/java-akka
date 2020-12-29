package com.learning.akka.akka_concurrency.behaviours;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class PrimeNumberManagerBehavior extends AbstractBehavior<PrimeNumberManagerBehavior.Command>{
	
	private SortedSet<BigInteger> primes = new TreeSet<BigInteger>();

	private PrimeNumberManagerBehavior(ActorContext<PrimeNumberManagerBehavior.Command> context) {
		super(context);
	}
	//marker interface
	public interface Command{
		
	}
	
	public static Behavior<PrimeNumberManagerBehavior.Command> create(){
		return Behaviors.setup(PrimeNumberManagerBehavior::new);
	}
	
	//remmeber message has to be immutable
	//both string and biginteger implemnets serializable for message transfer
	public static class CreateCommand implements Serializable,Command{
		private static final long serialVersionUID = -7951999893068417705L;
		//for string and biginteger is immutable and hence no issues in return in gettter methods
		private final String message;
		public CreateCommand(String message) {
			this.message = message;
		}
		public CreateCommand() {
			this.message = null;
		}
		public String getMessage() {
			return message;
		}
	}
	public static class MergeCommand implements Serializable,Command{
		private static final long serialVersionUID = -7951999893068417705L;
		//for string and biginteger is immutable and hence no issues in return in gettter methods
		private final BigInteger result;
		public MergeCommand(BigInteger result) {
			this.result = result;
		}
		public MergeCommand() {
			this.result = null;
		}
		public BigInteger getResult() {
			return result;
		}
	}

	@Override
	public Receive<PrimeNumberManagerBehavior.Command> createReceive() {
		
	return 	newReceiveBuilder()
			.onMessage(CreateCommand.class, createCommand ->{
				String message = createCommand.getMessage();
				if("create".equals(message)) {
					for(int i=0; i<20 ; i++) {
						String name = "prime-number-worker_"+i;
						//System.out.println("assining task to "+name);
						ActorRef<PrimeNumberWorkerBehaviour.Command> spawn = getContext().spawn(PrimeNumberWorkerBehaviour.create(), name);
						spawn.tell(new PrimeNumberWorkerBehaviour.Command("start", getContext().getSelf()));
					}
				}
				return this;
			})
			.onMessage(MergeCommand.class, mergeCommand ->{
				BigInteger result = mergeCommand.getResult();
				System.out.println("Found Result : "+result);
				//no need to synchronize as items are guranteed to come in order of message sent by sender
				primes.add(result);
				
				if(primes.size()==20) {
					System.out.println("All Task completed , final result "+primes);
				}
				return this;
			})
			
//			.onAnyMessage(command ->{
//				String message = command.getMessage();
//				if("create".equals(message)) {
//					for(int i=0; i<20 ; i++) {
//						String name = "prime-number-worker_"+i;
//						//System.out.println("assining task to "+name);
//						ActorRef<PrimeNumberWorkerBehaviour.Command> spawn = getContext().spawn(PrimeNumberWorkerBehaviour.create(), name);
//						spawn.tell(new PrimeNumberWorkerBehaviour.Command("start", getContext().getSelf()));
//					}
//				}else if("merge".equals(message)) {
//					BigInteger result = command.getResult();
//					System.out.println("Found Result : "+result);
//				}
//				return this;
//			})
			
		.build();
	}

}
