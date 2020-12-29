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

public class PrimeNumberWorkerBehaviour extends AbstractBehavior<PrimeNumberWorkerBehaviour.Command>{

	//immutable and serializable
	public static class Command implements Serializable{
		private static final long serialVersionUID = 2918439488053891452L;
		private final String message;
		private final ActorRef<PrimeNumberManagerBehavior.Command> sender;
		public Command(String message,
				ActorRef<PrimeNumberManagerBehavior.Command> sender) {
			this.message = message;
			this.sender = sender;
		}
		public Command() {
			this.message = null;
			this.sender = null;
		}
		public String getMessage() {
			return message;
		}
		public ActorRef<PrimeNumberManagerBehavior.Command> getSender() {
			return sender;
		}
		
	}
	
	private PrimeNumberWorkerBehaviour(ActorContext<PrimeNumberWorkerBehaviour.Command> context) {
		super(context);
	}
	
	public static Behavior<PrimeNumberWorkerBehaviour.Command> create(){
		return  Behaviors.setup(PrimeNumberWorkerBehaviour::new);
	}

	@Override
	public Receive<PrimeNumberWorkerBehaviour.Command> createReceive() {
		return newReceiveBuilder()
				.onAnyMessage(command ->{
					String message = command.getMessage();
					if("start".equals(message)) {
					BigInteger bigInteger = new BigInteger(2000, new Random());
					BigInteger nextProbablePrime = bigInteger.nextProbablePrime();
					PrimeNumberManagerBehavior.Command resultCommand = new PrimeNumberManagerBehavior.MergeCommand( nextProbablePrime);
					command.getSender().tell(resultCommand);
				}
					return this;
					})
			.build();
	}

}
