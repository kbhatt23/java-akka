package com.learning.akka.akka_concurrency.racesimulation;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;

public class RacerunnerV2 {
public static void main(String[] args) {
	Behavior<RaceControllerBehaviorV2.Command> reaceControllerBehavior = RaceControllerBehaviorV2.create();
	ActorSystem<RaceControllerBehaviorV2.Command> runner = ActorSystem.create(reaceControllerBehavior, "race-controller");
	runner.tell(new RaceControllerBehaviorV2.StartControlCommand(100, 10));
}
}
