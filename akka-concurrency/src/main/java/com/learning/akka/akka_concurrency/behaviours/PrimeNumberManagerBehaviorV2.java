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

public class PrimeNumberManagerBehaviorV2 extends AbstractBehavior<PrimeNumberManagerBehaviorV2.Command>{
	private  ActorRef<SortedSet<BigInteger>> mainActor;
	private SortedSet<BigInteger> primes ;
	public static Behavior<Command> create() {
		return Behaviors.setup(PrimeNumberManagerBehaviorV2::new);
	}
	public interface Command extends Serializable{}
	
	public static class StartTaskCommand implements Command{

		private static final long serialVersionUID = 1102785900841679230L;
		
		private final String message;
		
		private final ActorRef<SortedSet<BigInteger>> mainActor;

		public ActorRef<SortedSet<BigInteger>> getMainActor() {
			return mainActor;
		}

		public StartTaskCommand(String message,ActorRef<SortedSet<BigInteger>> mainActor) {
			this.message = message;
			this.mainActor=mainActor;
		}

		public StartTaskCommand() {
			this(null,null);
		}

		public String getMessage() {
			return message;
		}
		
	}
	
	public static class CompledTaskCommand implements Command{

		private static final long serialVersionUID = 1102785900841679230L;
		
		private final BigInteger result;

		public CompledTaskCommand(BigInteger result) {
			this.result = result;
		}

		public CompledTaskCommand() {
			this(null);
		}

		public BigInteger getResult() {
			return result;
		}

		
	}
	private PrimeNumberManagerBehaviorV2(ActorContext<Command> context) {
		super(context);
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(StartTaskCommand.class, startTaskCommand ->{
					String message = startTaskCommand.getMessage();
					if("start".contentEquals(message)) {
						this.primes= new TreeSet<BigInteger>();
						//the actor present in main thread
						this.mainActor=startTaskCommand.getMainActor();
						
						//generate 20 actors generatiung differnet prime numbers
						for(int i=0 ; i <20; i++) {
							//each actor have differnet path and thread and data and message quuee
							ActorRef<PrimeNumberWorkerBehaviorV2.Command> childActor = getContext().spawn(PrimeNumberWorkerBehaviorV2.create(), "prime-number-worker_"+i);
							childActor.tell(new PrimeNumberWorkerBehaviorV2.GeneratePrimeNumberCommand("generate", getContext().getSelf()));
							
							//calling it again, also we know that ordering is always maintained,
							//so each actor will generate 2 items in order
							//same actor called
							childActor.tell(new PrimeNumberWorkerBehaviorV2.GeneratePrimeNumberCommand("generate", getContext().getSelf()));
						}
						
					}
					return this;
				})
				.onMessage(CompledTaskCommand.class, compledTaskCommand ->{
					BigInteger result = compledTaskCommand.getResult();
					System.out.println("Manager Got Result: "+result);
					primes.add(result);
					if(primes.size() == 20) {
						//System.out.println("Manager Task completed , printing final Set");
						
						//primes.forEach(System.out::println);
						//just send message to main actor
						mainActor.tell(primes);
						return Behaviors.stopped();
					}
					return this;
				})
				.onAnyMessage(command ->{
					System.out.println("Manager "+getContext().getSelf().path()+" recieving unknown command");
					return this;
				})
				.build();
	}
}
