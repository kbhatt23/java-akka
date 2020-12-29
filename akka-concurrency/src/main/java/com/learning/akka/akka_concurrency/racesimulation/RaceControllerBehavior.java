package com.learning.akka.akka_concurrency.racesimulation;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class RaceControllerBehavior extends AbstractBehavior<RaceControllerBehavior.Command>{
	private RaceControllerBehavior(ActorContext<Command> context) {
		super(context);
	}
	private long startTime;
	private Map<ActorRef<RacerBehavior.Command>, Integer> postitionMap;
	private Object RACE_TIMER = new Object();
	static int displayLength = 160;

	public static Behavior<RaceControllerBehavior.Command> create() {
		return Behaviors.setup(RaceControllerBehavior::new);
	}

	public interface Command extends Serializable {
	}

	public static class StartControlCommand implements Command {

		private static final long serialVersionUID = -146915017583198692L;

		private final int raceLength;
		private final int numOfRacers;

		public StartControlCommand(int raceLength, int numOfRacers) {
			this.raceLength = raceLength;
			this.numOfRacers = numOfRacers;
		}

		public StartControlCommand() {
			this(0, 0);
		}

		public int getRaceLength() {
			return raceLength;
		}

		public int getNumOfRacers() {
			return numOfRacers;
		}

	}
	private  class GetPositionTimerCommand implements Command {
		private static final long serialVersionUID = -3420538738350039047L;
		}
	
	
	public static class RacePositionCommand implements Command {

		private static final long serialVersionUID = -146915017583198692L;

		private final int currentPosition;
		private final ActorRef<RacerBehavior.Command> racerActor;
		public RacePositionCommand(int currentPosition, ActorRef<RacerBehavior.Command> racerActor) {
			this.currentPosition = currentPosition;
			this.racerActor = racerActor;
		}
		public RacePositionCommand() {
			this(0,null);
		}
		public int getCurrentPosition() {
			return currentPosition;
		}
		public ActorRef<RacerBehavior.Command> getRacerActor() {
			return racerActor;
		}


	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder().onMessage(StartControlCommand.class, startControlCommand -> {
			this.startTime=System.currentTimeMillis();
			this.postitionMap=new HashMap<>();
			int raceLength = startControlCommand.getRaceLength();
			int numOfRacers = startControlCommand.getNumOfRacers();
			for (int i = 0; i < numOfRacers; i++) {
				ActorRef<RacerBehavior.Command> racerActor = getContext().spawn(RacerBehavior.create(), "racer-" + i);
				racerActor.tell(new RacerBehavior.StartRaceCommand(raceLength));
				//becuae of timer it will call again and again the race controller a message, so we need it
				this.postitionMap.put(racerActor, 0);
			}
		return 	Behaviors.withTimers(timer ->{
			timer.startTimerAtFixedRate(RACE_TIMER , new GetPositionTimerCommand(), Duration.ofSeconds(1));
				return this;
			});
		//setting up a timer on start command, so that every 1 second a call goes to the controller and from map list we can ask actors for currnet position
			//return this;
		})
				.onMessage(RacePositionCommand.class, racePositionCommand ->{
					int currentPosition = racePositionCommand.getCurrentPosition();
					ActorRef<RacerBehavior.Command> racerActor = racePositionCommand.getRacerActor();
					//System.out.println("Recieved racer "+racerActor.path()+" current position as "+currentPosition);
					this.postitionMap.put(racerActor, currentPosition);
					return this;
				})
				.onMessage(GetPositionTimerCommand.class, getPositionTimerCommand ->{
					this.postitionMap.keySet().forEach(racerActor -> racerActor.tell(new RacerBehavior.RaceCurrentPositionCommand(getContext().getSelf())));
					displayRace(postitionMap);
					return this;
				})
				.build();
	}
	
	private  void displayRace(Map<ActorRef<RacerBehavior.Command>, Integer> currentPositions) {
		for (int i = 0; i < 50; ++i) System.out.println();
		System.out.println("Race has been running for " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds.");
		System.out.println("    " + new String (new char[displayLength]).replace('\0', '='));
		int i=0;
		for (Entry<ActorRef<RacerBehavior.Command>, Integer> entries: currentPositions.entrySet()) {
			System.out.println(i + " : "  + new String (new char[entries.getValue() * displayLength / 100]).replace('\0', '*'));
			i++;
		}
	}

}
