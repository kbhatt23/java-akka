package com.learning.akka.akka_concurrency.actorrunner;

import com.learning.akka.akka_concurrency.behaviours.SimpleBehaviourWithMessageChains;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;

public class SimpleChainedBehaviourRunner {
	public static void main(String[] args) {

		Behavior<String> behaviour = SimpleBehaviourWithMessageChains.create();
		ActorSystem<String> actorSystem = ActorSystem.create(behaviour, "simple-chained-actor");
		
		//will get ignored, if there is no onmessagaeequals
		//but we shud have default method onany for this to showup
		actorSystem.tell("nothing");
		actorSystem.tell("create");
		actorSystem.tell("update");
		actorSystem.tell("create-actor");
		actorSystem.tell("delete");
	}
}
