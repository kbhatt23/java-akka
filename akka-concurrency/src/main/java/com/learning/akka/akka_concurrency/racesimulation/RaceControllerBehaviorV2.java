package com.learning.akka.akka_concurrency.racesimulation;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class RaceControllerBehaviorV2 extends AbstractBehavior<RaceControllerBehaviorV2.Command>{
	private RaceControllerBehaviorV2(ActorContext<Command> context) {
		super(context);
	}
	private long startTime;
	private int numOfRacers;
	private Map<ActorRef<RacerBehaviorV2.Command>, Integer> postitionMap;
	private Map<ActorRef<RacerBehaviorV2.Command>, Long> finishingMap;
	private Object RACE_TIMER = new Object();
	static int displayLength = 160;

	public static Behavior<RaceControllerBehaviorV2.Command> create() {
		return Behaviors.setup(RaceControllerBehaviorV2::new);
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
		private final ActorRef<RacerBehaviorV2.Command> racerActor;
		public RacePositionCommand(int currentPosition, ActorRef<RacerBehaviorV2.Command> racerActor) {
			this.currentPosition = currentPosition;
			this.racerActor = racerActor;
		}
		public RacePositionCommand() {
			this(0,null);
		}
		public int getCurrentPosition() {
			return currentPosition;
		}
		public ActorRef<RacerBehaviorV2.Command> getRacerActor() {
			return racerActor;
		}


	}
	
	public static class RaceCompletionCommand implements Command {

		private static final long serialVersionUID = -146915017583198692L;

		private final ActorRef<RacerBehaviorV2.Command> racerActor;
		public RaceCompletionCommand(ActorRef<RacerBehaviorV2.Command> racerActor) {
			this.racerActor = racerActor;
		}
		public RaceCompletionCommand() {
			this(null);
		}
		public ActorRef<RacerBehaviorV2.Command> getRacerActor() {
			return racerActor;
		}


	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder().onMessage(StartControlCommand.class, startControlCommand -> {
			this.startTime=System.currentTimeMillis();
			this.postitionMap=new HashMap<>();
			//preserve the inseriton order , so that winner is shown first
			this.finishingMap=new LinkedHashMap<>();
			int raceLength = startControlCommand.getRaceLength();
			int numOfRacers = startControlCommand.getNumOfRacers();
			this.numOfRacers=numOfRacers;
			for (int i = 0; i < numOfRacers; i++) {
				ActorRef<RacerBehaviorV2.Command> racerActor = getContext().spawn(RacerBehaviorV2.create(), "racer-" + i);
				racerActor.tell(new RacerBehaviorV2.StartRaceCommand(raceLength));
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
					ActorRef<RacerBehaviorV2.Command> racerActor = racePositionCommand.getRacerActor();
					//System.out.println("Recieved racer "+racerActor.path()+" current position as "+currentPosition);
					this.postitionMap.put(racerActor, currentPosition);
					return this;
				})
				.onMessage(GetPositionTimerCommand.class, getPositionTimerCommand ->{
					this.postitionMap.keySet().forEach(racerActor -> racerActor.tell(new RacerBehaviorV2.RaceCurrentPositionCommand(getContext().getSelf())));
					displayRace(postitionMap);
					return this;
				})
				.onMessage(RaceCompletionCommand.class, raceCompletionCommand ->{
					finishingMap.put(raceCompletionCommand.getRacerActor(), System.currentTimeMillis());
					if(finishingMap.size() == this.numOfRacers) {
						return completRacingOperation();
					}else {
						return Behaviors.same();
					}
				})
				.build();
	}
	//once competion message is called no other message other than timer message will be sent to queue of controller actor
	public Receive<Command> completRacingOperation() {
		return newReceiveBuilder()
				.onMessage(GetPositionTimerCommand.class,c -> {
					Logger log = getContext().getLog();
					log.info("All racers finished,printing results");
					finishingMap.forEach((key,value) ->{
						log.info("Racer "+key.path()+" finished task in "+
								(value-this.startTime)/1000+" seconds"
								);
					});
					
					finishingMap.entrySet().forEach(entry -> getContext().stop(entry.getKey()));
					return Behaviors.withTimers(timer ->{
						//just cancel only this timer
						timer.cancel(RACE_TIMER);
						return Behaviors.stopped();
						});
				})
				.build();
	}
	
	private  void displayRace(Map<ActorRef<RacerBehaviorV2.Command>, Integer> currentPositions) {
		for (int i = 0; i < 50; ++i) System.out.println();
		System.out.println("Race has been running for " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds.");
		System.out.println("    " + new String (new char[displayLength]).replace('\0', '='));
		int i=0;
		for (Entry<ActorRef<RacerBehaviorV2.Command>, Integer> entries: currentPositions.entrySet()) {
			System.out.println(i + " : "  + new String (new char[entries.getValue() * displayLength / 100]).replace('\0', '*'));
			i++;
		}
	}

}
