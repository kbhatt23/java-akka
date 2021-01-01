package com.learning.akka.block_chain_akka.behaviors;

import java.io.Serializable;

import com.learning.akka.block_chain_akka.models.Block;
import com.learning.akka.block_chain_akka.models.HashResult;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class BlockChainManagerBehavior extends AbstractBehavior<BlockChainManagerBehavior.Command>{

	private int difficultyLevel;
	private ActorRef<HashResult> mainClassActor;
	private  Block block;
	 private int currentNonce = 0;
	 
	 private boolean currentlyMining ;
	
	private BlockChainManagerBehavior(ActorContext<Command> context) {
		super(context);
	}
	
	public static Behavior<Command> create(){
		return Behaviors.setup(BlockChainManagerBehavior::new);
	}

	public interface Command extends Serializable{}
	
	public static class StartHashCommand implements Command{
		private static final long serialVersionUID = 7774975056340987470L;
		
		private final int difficultyLevel;
		private final ActorRef<HashResult> mainClassActor;
		private final Block block;
		public StartHashCommand(int difficultyLevel, ActorRef<HashResult> mainClassActor, Block block) {
			super();
			this.difficultyLevel = difficultyLevel;
			this.mainClassActor = mainClassActor;
			this.block = block;
		}
		public int getDifficultyLevel() {
			return difficultyLevel;
		}
		public ActorRef<HashResult> getMainClassActor() {
			return mainClassActor;
		}
		public Block getBlock() {
			return block;
		}
	}
	
	public static class HashResultCommand implements Command{
		private static final long serialVersionUID = 7774975056340987470L;
		
		private final HashResult hashResult;
		public HashResult getHashResult() {
			return hashResult;
		}
		public HashResultCommand(HashResult hashResult) {
			this.hashResult = hashResult;
		}
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				//added just ot handle a temination erro of child-> since we are monitoring
				.onSignal(Terminated.class, error ->{
					generateNextActor();
					return Behaviors.same();
					})
				.onMessage(StartHashCommand.class, command ->{
					this.mainClassActor=command.getMainClassActor();
					this.difficultyLevel=command.getDifficultyLevel();
					this.block = command.getBlock();
					this.currentlyMining=true;
					//generate 10 actors
					for(int i=0 ; i < 10 ; i++) {
						generateNextActor();
					}
					
					return Behaviors.same();
				})
				.onMessage(HashResultCommand.class, command ->{
					//since we need new actors lets kill them
					getContext().getChildren().forEach(child -> getContext().stop(child));
					this.mainClassActor.tell(command.getHashResult());
					currentlyMining=false;
					return this;
				})
				.build();
				
				
	}
	
	private void generateNextActor() {
		if(currentlyMining) {
		//Behavior<BlockChainWorkerBehavior.Command> workerBehavior = BlockChainWorkerBehavior.create();
		
		//just create new actor and resume the message processing
		Behavior<BlockChainWorkerBehavior.Command> workerBehavior = Behaviors.supervise(BlockChainWorkerBehavior.create()).onFailure(SupervisorStrategy.resume());
		
		BlockChainWorkerBehavior.Command workerCommand = new BlockChainWorkerBehavior.Command(block, currentNonce*1000, difficultyLevel, getContext().getSelf());
		ActorRef<BlockChainWorkerBehavior.Command> workerActor = getContext().spawn(workerBehavior, "worker" + currentNonce);
		getContext().watch(workerActor);
		workerActor.tell(workerCommand);
		currentNonce++;
		}
	}

}
