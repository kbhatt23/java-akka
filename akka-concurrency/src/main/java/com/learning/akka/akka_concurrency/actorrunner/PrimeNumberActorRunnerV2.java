package com.learning.akka.akka_concurrency.actorrunner;

import com.learning.akka.akka_concurrency.behaviours.PrimeNumberManagerBehaviorV2;

import akka.actor.typed.ActorSystem;

public class PrimeNumberActorRunnerV2 {
public static void main(String[] args) {
	ActorSystem<PrimeNumberManagerBehaviorV2.Command> managerActor = ActorSystem.create(PrimeNumberManagerBehaviorV2.create(),
			"prime-number-manager"
			);
	
	managerActor.tell(new PrimeNumberManagerBehaviorV2.StartTaskCommand("start"));
}
}
