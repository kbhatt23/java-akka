package com.learning.akka.akka_concurrency.racesimulation;

import java.io.Serializable;
import java.util.Random;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class RacerBehaviorV2 extends AbstractBehavior<RacerBehaviorV2.Command>{
	private final double defaultAverageSpeed = 48.2;
	private int averageSpeedAdjustmentFactor;
	private Random random;	
	//private int raceLength;
	
	private double currentSpeed = 0;
	//private double currentPosition = 0;
	
	private double getMaxSpeed() {
		return defaultAverageSpeed * (1+((double)averageSpeedAdjustmentFactor / 100));
	}
		
	private double getDistanceMovedPerSecond() {
		return currentSpeed * 1000 / 3600;
	}
	
	private void determineNextSpeed(int raceLength, double currentPosition) {
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

	private RacerBehaviorV2(ActorContext<Command> context) {
		super(context);
	}
	
	public static Behavior<Command> create(){
		return Behaviors.setup(RacerBehaviorV2::new);
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
		
		private final ActorRef<RaceControllerBehaviorV2.Command> sender;
		public RaceCurrentPositionCommand(ActorRef<RaceControllerBehaviorV2.Command> sender) {
		this.sender=sender;	
		}
		public RaceCurrentPositionCommand() {
			this(null);	
			}
		public ActorRef<RaceControllerBehaviorV2.Command> getSender() {
			return sender;
		}
		
	}

	@Override
	public Receive<Command> createReceive() {
		return notYetStartedBehavior();
	}
	
	public Receive<Command> notYetStartedBehavior() {
		return newReceiveBuilder()
				.onMessage(StartRaceCommand.class, startRaceCommand ->{
					int raceLength=startRaceCommand.getRaceLength();
					this.random=new Random();
					averageSpeedAdjustmentFactor = random.nextInt(30) - 10;
					//from now on if some one calls notYetStarted messag eit goes to running behavior method
					return runningBehavior(raceLength,0);
				})
				//when not started position is 0 only,no need to determine and calculate
				.onMessage(RaceCurrentPositionCommand.class, raceCurrentPositionCommand ->{
					raceCurrentPositionCommand.getSender().tell(new RaceControllerBehaviorV2.RacePositionCommand(0, getContext().getSelf()));
					return Behaviors.same();
				})
					.build();
	}
	
	
	public Receive<Command> runningBehavior(int raceLength, int currentPosition) {
		return newReceiveBuilder()
				.onMessage(RaceCurrentPositionCommand.class, raceCurrentPositionCommand ->{
					int positionCopy = currentPosition;
					determineNextSpeed(raceLength,positionCopy);
					positionCopy += getDistanceMovedPerSecond();
					if (positionCopy > raceLength )
						positionCopy  = raceLength;
					raceCurrentPositionCommand.getSender().tell(new RaceControllerBehaviorV2.RacePositionCommand((int)positionCopy, getContext().getSelf()));
					
					if(positionCopy == raceLength) {
						return completedBehavior(raceLength);
					}else {
						//not yet compelted the race
						return runningBehavior(raceLength, positionCopy);
					}
					
				})
					.build();
	}
	
	
	public Receive<Command> completedBehavior(int raceLength) {
		return newReceiveBuilder()
				.onMessage(RaceCurrentPositionCommand.class, raceCurrentPositionCommand ->{
					getContext().getLog().info("Race completed by racer: "+getContext().getSelf().path());
					//System.out.println("Position asked by a racer who completed the race, racer name: "+getContext().getSelf().path());
					raceCurrentPositionCommand.getSender().tell(new RaceControllerBehaviorV2.RacePositionCommand(raceLength, getContext().getSelf()));
					raceCurrentPositionCommand.getSender().tell(new RaceControllerBehaviorV2.RaceCompletionCommand( getContext().getSelf()));
					
					//better than force shutting down
					return Behaviors.ignore();
					//stopping this way is dangerous as the parent will keep on publishing message, so it will show error of dead letter messages
					
					//return Behaviors.stopped();
				})
					.build();
	}
}
