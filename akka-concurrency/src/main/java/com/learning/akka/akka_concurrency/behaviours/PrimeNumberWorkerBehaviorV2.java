package com.learning.akka.akka_concurrency.behaviours;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class PrimeNumberWorkerBehaviorV2 extends AbstractBehavior<PrimeNumberWorkerBehaviorV2.Command>{

	//better not save the state in actor instance
	//private BigInteger prime=null;
	public static Behavior<Command> create() {
		return Behaviors.setup(PrimeNumberWorkerBehaviorV2::new);
	}
	
	public interface Command extends Serializable{}

	//serializable and immutable
	public static class GeneratePrimeNumberCommand implements Command{

		private static final long serialVersionUID = -1753731521559024924L;
		
		private final String eventType;
		private final ActorRef<PrimeNumberManagerBehaviorV2.Command> sender;
		
		public GeneratePrimeNumberCommand(String eventType, ActorRef<PrimeNumberManagerBehaviorV2.Command> sender) {
			this.eventType = eventType;
			this.sender = sender;
		}
		

		public GeneratePrimeNumberCommand() {
			this(null,null);
		}


		public String getEventType() {
			return eventType;
		}


		public ActorRef<PrimeNumberManagerBehaviorV2.Command> getSender() {
			return sender;
		}

		
	}
	private PrimeNumberWorkerBehaviorV2(ActorContext<Command> context) {
		super(context);
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(GeneratePrimeNumberCommand.class, generatePrimeNumberCommand ->{
					String eventType = generatePrimeNumberCommand.getEventType();
					BigInteger primeStateless = null;
					if ("generate".equals(eventType)) {
						//System.out.println("getting called first time");
						ActorRef<PrimeNumberManagerBehaviorV2.Command> sender = generatePrimeNumberCommand.getSender();
						//if (prime == null) {
							BigInteger random = new BigInteger(2000, new Random());
							BigInteger nextProbablePrime = random.nextProbablePrime();
							//prime = nextProbablePrime;
							primeStateless=nextProbablePrime;
						//}
						sender.tell(new PrimeNumberManagerBehaviorV2.CompledTaskCommand(primeStateless));
					}
					
					//both lines are same
					//return Behaviors.same();
					//return this;
					
					//for first time this hander will called and after that whenever same message comes to actor , will be handled using below method
					return mesageHandlingSecondTime(primeStateless);
				})
				.onAnyMessage(command ->{
					System.out.println("Worker "+getContext().getSelf().path()+" recieving unknown command");
					return this;
				})
				.build();
	}
	
	public Receive<Command> mesageHandlingSecondTime(BigInteger primeStateless) {
		return newReceiveBuilder()
				.onMessage(GeneratePrimeNumberCommand.class, generatePrimeNumberCommand ->{
					//System.out.println("getting called second time");
					String eventType = generatePrimeNumberCommand.getEventType();
					if ("generate".equals(eventType)) {
						ActorRef<PrimeNumberManagerBehaviorV2.Command> sender = generatePrimeNumberCommand.getSender();
						sender.tell(new PrimeNumberManagerBehaviorV2.CompledTaskCommand(primeStateless));
					}
					
					//both lines are same
					return Behaviors.same();
					//return this;
				})
				.build();
	}
}
