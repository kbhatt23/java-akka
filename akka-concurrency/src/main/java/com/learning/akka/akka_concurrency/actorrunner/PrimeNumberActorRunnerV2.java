package com.learning.akka.akka_concurrency.actorrunner;

import java.math.BigInteger;
import java.time.Duration;
import java.util.SortedSet;
import java.util.concurrent.CompletionStage;

import com.learning.akka.akka_concurrency.behaviours.PrimeNumberManagerBehaviorV2;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;

public class PrimeNumberActorRunnerV2 {
public static void main(String[] args) {
	ActorSystem<PrimeNumberManagerBehaviorV2.Command> managerActor = ActorSystem.create(PrimeNumberManagerBehaviorV2.create(),
			"prime-number-manager"
			);
	
	//managerActor.tell(new PrimeNumberManagerBehaviorV2.StartTaskCommand("start"));
	
	CompletionStage<SortedSet<BigInteger>> finalResult = AskPattern.ask(managerActor, me -> new PrimeNumberManagerBehaviorV2.StartTaskCommand("start", me),
			Duration.ofSeconds(50), managerActor.scheduler());
	
	finalResult.whenComplete((set,error )->{
		if(set != null) {
			System.out.println("PrimeNumberActorRunnerV2: Manager Task completed , printing final Set");
			set.forEach(System.out::println);
			
		}else {
			System.out.println("PrimeNumberActorRunnerV2: Unable to execute sucesfully with error "+error.getMessage());
		}
		managerActor.terminate();
	});
}
}
