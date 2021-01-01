package com.learning.akka.akka_concurrency.actorrunner;

import java.math.BigInteger;
import java.time.Duration;
import java.util.SortedSet;
import java.util.concurrent.CompletionStage;

import com.learning.akka.akka_concurrency.behaviours.PrimeNumberManagerBehavior;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AskPattern;

public class PrimeNumberActorRunner {

	public static void main(String[] args) {

		Behavior<PrimeNumberManagerBehavior.Command> managerBehavior = PrimeNumberManagerBehavior.create();
		ActorSystem<PrimeNumberManagerBehavior.Command> runner = ActorSystem.create(managerBehavior, "prime-number-manager");
		//using tell we cna not get back data or some contnet form manager
		//runner.tell(new PrimeNumberManagerBehavior.CreateCommand("create"));
	
	        CompletionStage<SortedSet<BigInteger>> result = AskPattern.ask(runner,
	                (me) -> new PrimeNumberManagerBehavior.CreateCommand("create", me),
	                Duration.ofSeconds(20),
	                runner.scheduler());
	        
	        result.whenComplete((sortedSet, error) ->{
	        	if(sortedSet != null) {
	        		System.out.println("PrimeNumberActorRunner: All Task completed , final result "+sortedSet);
	        	}else {
	        		System.out.println("PrimeNumberActorRunner: Task unable to coplete succesfully with error "+error.getMessage());
	        	}
	        });
		
	}

}
