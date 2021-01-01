package com.learning.akka.block_chain_akka.behaviors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class StashProcessingBehavior extends AbstractBehavior<String>{

	private StashProcessingBehavior(ActorContext<String> context) {
		super(context);
	}
	
	public static Behavior<String> create(){

		return Behaviors.setup(StashProcessingBehavior::new);
	}

	@Override
	public Receive<String> createReceive() {
		return newReceiveBuilder()
				.onAnyMessage(message -> {
					System.out.println("Processing stash message "+message); return this;
					})
				.build();
	}

}
