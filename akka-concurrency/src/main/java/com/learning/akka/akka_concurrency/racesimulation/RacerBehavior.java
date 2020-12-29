package com.learning.akka.akka_concurrency.racesimulation;

import java.io.Serializable;
import java.util.Random;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class RacerBehavior extends AbstractBehavior<RacerBehavior.Command>{
	private final double defaultAverageSpeed = 48.2;
	private int averageSpeedAdjustmentFactor;
	private Random random;	
	private int raceLength;
	
	private double currentSpeed = 0;
	private double currentPosition = 0;
	
	private double getMaxSpeed() {
		return defaultAverageSpeed * (1+((double)averageSpeedAdjustmentFactor / 100));
	}
		
	private double getDistanceMovedPerSecond() {
		return currentSpeed * 1000 / 3600;
	}
	
	private void determineNextSpeed() {
		if (currentPosition < (raceLength / 4)) {
			currentSpeed = currentSpeed  + (((getMaxSpeed() - currentSpeed) / 10) * random.nextDouble());
		}
		else {
			currentSpeed = currentSpeed * (0.5 + random.nextDouble());
		}
	
		if (currentSpeed > getMaxSpeed()) 
			currentSpeed = getMaxSpeed();
		
		if (currentSpeed < 5)
			currentSpeed = 5;
		
		if (currentPosition > (raceLength / 2) && currentSpeed < getMaxSpeed() / 2) {
			currentSpeed = getMaxSpeed() / 2;
		}
	}

	private RacerBehavior(ActorContext<Command> context) {
		super(context);
	}
	
	public static Behavior<Command> create(){
		return Behaviors.setup(RacerBehavior::new);
	}

	public interface Command extends Serializable{}
	
	//command to start(pass other data)
	public static class StartRaceCommand implements Command{

		private static final long serialVersionUID = -146915017583198692L;
		
		private final int raceLength;
		public StartRaceCommand(int raceLength) {
		this.raceLength=raceLength;	
		}
		public StartRaceCommand() {
			this(0);	
			}
		public int getRaceLength() {
			return raceLength;
		}
		
	}
	
	//command to pass the current position
	public static class RaceCurrentPositionCommand implements Command{

		private static final long serialVersionUID = -146915017583198692L;
		
		private final ActorRef<RaceControllerBehavior.Command> sender;
		public RaceCurrentPositionCommand(ActorRef<RaceControllerBehavior.Command> sender) {
		this.sender=sender;	
		}
		public RaceCurrentPositionCommand() {
			this(null);	
			}
		public ActorRef<RaceControllerBehavior.Command> getSender() {
			return sender;
		}
		
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(StartRaceCommand.class, startRaceCommand ->{
					this.raceLength=startRaceCommand.getRaceLength();
					this.random=new Random();
					averageSpeedAdjustmentFactor = random.nextInt(30) - 10;
					return this;
				})
				.onMessage(RaceCurrentPositionCommand.class, raceCurrentPositionCommand ->{
					determineNextSpeed();
					currentPosition += getDistanceMovedPerSecond();
					if (currentPosition > raceLength )
						currentPosition  = raceLength;
					raceCurrentPositionCommand.getSender().tell(new RaceControllerBehavior.RacePositionCommand((int)currentPosition, getContext().getSelf()));
					return this;
				})
					.build();
	}
	
}
