package com.learning.akka.akka_concurrency.actorrunner;

import com.learning.akka.akka_concurrency.behaviours.SimpleBehaviour;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;

public class SimpleBehaviourRunner {
public static void main(String[] args) {
	Behavior<String> behavior = SimpleBehaviour.create();
	ActorSystem<String> actorSystem = ActorSystem.create(behavior, "simple-actor");
	
	actorSystem.tell("jai shree ram");
	actorSystem.tell("jai radhe krishna");
	//actorSystem.tell(null);
	//actorSystem.tell("jai shree ram");
	
}
}
