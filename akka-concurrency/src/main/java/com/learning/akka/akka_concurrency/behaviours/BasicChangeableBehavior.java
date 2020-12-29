package com.learning.akka.akka_concurrency.behaviours;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.Behaviors;

//same message will get processed in different behavior from second time
public class BasicChangeableBehavior extends AbstractBehavior<String>{

	private BasicChangeableBehavior(ActorContext<String> context) {
		super(context);
	}
	
	public static Behavior<String> create(){
		return Behaviors.setup(BasicChangeableBehavior::new);
	}

	@Override
	public Receive<String> createReceive() {
		return newReceiveBuilder()
				.onMessageEquals("random", () ->{
					String val = "jai shree ram";
					System.out.println("Recieving message in the main recieve method with random value: "+val);
					return recieveSupplementary(val);
				})
				
				.build();
	}
	
	public Receive<String> recieveSupplementary(String val) {
		return newReceiveBuilder()
				.onMessageEquals("random", () ->{
					System.out.println("Recieving message in the main recieveSupplementary method with value: "+val);
					return Behaviors.same();
				})
				
				.build();
	}

}
