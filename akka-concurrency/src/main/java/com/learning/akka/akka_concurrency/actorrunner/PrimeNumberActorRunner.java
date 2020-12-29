package com.learning.akka.akka_concurrency.actorrunner;

import com.learning.akka.akka_concurrency.behaviours.PrimeNumberManagerBehavior;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;

public class PrimeNumberActorRunner {

	public static void main(String[] args) {

		Behavior<PrimeNumberManagerBehavior.Command> managerBehavior = PrimeNumberManagerBehavior.create();
		ActorSystem<PrimeNumberManagerBehavior.Command> runner = ActorSystem.create(managerBehavior, "prime-number-manager");
		runner.tell(new PrimeNumberManagerBehavior.CreateCommand("create"));
	}

}
