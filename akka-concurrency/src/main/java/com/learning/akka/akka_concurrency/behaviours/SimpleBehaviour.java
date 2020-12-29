package com.learning.akka.akka_concurrency.behaviours;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class SimpleBehaviour extends AbstractBehavior<String>{

	//client class might not have context and hence creating factory method for it
	private SimpleBehaviour(ActorContext<String> context) {
		super(context);
	}
	
	public static Behavior<String> create(){
		return Behaviors.setup(SimpleBehaviour::new);
	}

	@Override
	public Receive<String> createReceive() {
		
		return newReceiveBuilder()
				//gets called for all messgae , null or any value
				.onAnyMessage(message ->{
					System.out.println("Recieving Message: "+message);
					return this;
				})
				.build()
				;
		
	}

}
