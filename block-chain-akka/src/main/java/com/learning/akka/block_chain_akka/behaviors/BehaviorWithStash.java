package com.learning.akka.block_chain_akka.behaviors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.javadsl.Behaviors;
//as of now demonstrating with string only , stirng is already serializable and immutable
public class BehaviorWithStash extends AbstractBehavior<String>{
	
	//need to hold stash
	private StashBuffer<String> stashBuffer;
	private int counter;

	private BehaviorWithStash(ActorContext<String> context,StashBuffer<String> stashBuffer) {
		super(context);
		this.stashBuffer=stashBuffer;
	}

	public static Behavior<String> create(){
		//just like behaviors.timer and behaviors.supervisor etc
		return Behaviors.withStash(10, stash ->{
			return Behaviors.setup(context -> new BehaviorWithStash(context, stash));
		});
		
	}
	@Override
	public Receive<String> createReceive() {

		return newReceiveBuilder()
				.onMessageEquals("start", () ->{
					//instead of taking first 5 messages we push to buffer so that we can process later
					if(stashBuffer.isFull()) {
						getContext().getSelf().tell("process");
						getContext().getSelf().tell("start");
						return Behaviors.same();
					}
					stashBuffer.stash("jai shree ram "+(++counter));
					
					return Behaviors.same();
				})
				.onMessageEquals("process", () ->{
					//instead of taking first 5 messages we push to buffer so that we can process later
					stashBuffer.unstashAll(StashProcessingBehavior.create());
					return Behaviors.same();
				})
		.build();
	}

}
