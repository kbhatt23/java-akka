package com.learning.akka.akka_concurrency.actorrunner;

import com.learning.akka.akka_concurrency.behaviours.BasicChangeableBehavior;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;

public class BasicChangeableBehaviorRunner {
public static void main(String[] args) {
	Behavior<String> beahavior = BasicChangeableBehavior.create();
	ActorSystem<String> system = ActorSystem.create(beahavior, "repeatable-behavior");
	
	system.tell("random");
	system.tell("random");
	system.tell("random");
	system.tell("random");
}
}
