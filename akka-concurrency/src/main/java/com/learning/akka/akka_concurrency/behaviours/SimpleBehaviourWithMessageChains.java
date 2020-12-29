package com.learning.akka.akka_concurrency.behaviours;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class SimpleBehaviourWithMessageChains extends AbstractBehavior<String>{

	//client class might not have context and hence creating factory method for it
	private SimpleBehaviourWithMessageChains(ActorContext<String> context) {
		super(context);
	}
	
	public static Behavior<String> create(){
		return Behaviors.setup(SimpleBehaviourWithMessageChains::new);
	}

	@Override
	public Receive<String> createReceive() {
		
		return newReceiveBuilder()
				
				.onMessageEquals("create", () ->{
					System.out.println("jai shree ram while creating using context "+getContext().getSelf().path());
					return this;
				})
				.onMessageEquals("update", () ->{
					System.out.println("jai shree ram while updating using context "+getContext().getSelf().path());
					return this;
				})
				.onMessageEquals("delete", () ->{
					System.out.println("jai shree ram while deleting using context "+getContext().getSelf().path());
					return this;
				})
				.onMessageEquals("create-actor", () ->{
					//spawn new actor
					ActorRef<String> childActor = getContext().spawn(SimpleBehaviourWithMessageChains.create(), "child-actor");
					childActor.tell("create");
					return this;
				})
				//if nothing is found above in conditional check, default handling is below
				//gets called for all messages , for null any value of string message
				.onAnyMessage(message ->{
					System.out.println("Recieving Message: "+message);
					return this;
				})
				.build()
				;
		
	}

}
