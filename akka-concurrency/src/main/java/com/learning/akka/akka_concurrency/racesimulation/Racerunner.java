package com.learning.akka.akka_concurrency.racesimulation;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;

public class Racerunner {
public static void main(String[] args) {
	Behavior<RaceControllerBehavior.Command> reaceControllerBehavior = RaceControllerBehavior.create();
	ActorSystem<RaceControllerBehavior.Command> runner = ActorSystem.create(reaceControllerBehavior, "race-controller");
	runner.tell(new RaceControllerBehavior.StartControlCommand(100, 10));
}
}
