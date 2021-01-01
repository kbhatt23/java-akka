package com.learning.akka.block_chain_akka.behaviors;

import java.io.Serializable;
import java.util.Random;

import org.slf4j.Logger;

import com.learning.akka.block_chain_akka.models.Block;
import com.learning.akka.block_chain_akka.models.HashResult;
import com.learning.akka.block_chain_akka.utils.BlockUtil;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
//worker will generate nonce and hash from blocks
public class BlockChainWorkerBehavior extends AbstractBehavior<BlockChainWorkerBehavior.Command>{

	private BlockChainWorkerBehavior(ActorContext<Command> context) {
		super(context);
	}
	
	public static Behavior<BlockChainWorkerBehavior.Command> create(){
		return Behaviors.setup(BlockChainWorkerBehavior::new);
	}

	public static class Command implements Serializable{

		private static final long serialVersionUID = 4336047898121395338L;
		
		private final Block block;
		
		private final int startNonce;
		
		private final int difficultyLevel;
		
		private final ActorRef<BlockChainManagerBehavior.Command> sender;

		public Command(Block block, int startNonce, int difficultyLevel,ActorRef<BlockChainManagerBehavior.Command> sender) {
			this.block = block;
			this.startNonce = startNonce;
			this.difficultyLevel = difficultyLevel;
			this.sender=sender;
		}
		
		public Command() {
			this(null, 0, 0,null);
		}

		public Block getBlock() {
			return block;
		}

		public int getStartNonce() {
			return startNonce;
		}

		public int getDifficultyLevel() {
			return difficultyLevel;
		}
		public ActorRef<BlockChainManagerBehavior.Command> getSender() {
			return sender;
		}

	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onAnyMessage(command ->{
					Logger log = getContext().getLog();
					log.info("Task started");
					int difficultyLevel=command.getDifficultyLevel();
					String hash = new String(new char[difficultyLevel]).replace("\0", "X");
					String target = new String(new char[difficultyLevel]).replace("\0", "0");
					int nonce = command.getStartNonce();
					Block block = command.getBlock();
					while (!hash.substring(0, difficultyLevel).equals(target) && nonce < command.getStartNonce() + 1000) {
						nonce++;
						String dataToEncode = block.getPreviousHash() + Long.toString(block.getTransaction().getTimestamp())
								+ Integer.toString(nonce) + block.getTransaction();
						hash = BlockUtil.calculateHash(dataToEncode);
					}
					if (hash.substring(0, difficultyLevel).equals(target)) {
						HashResult hashResult = new HashResult();
						hashResult.foundAHash(hash, nonce);
						log.info("Noonce Generated hash "+hashResult.getHash() + " and noonce "+ hashResult.getNonce());
						//lets send the hashresult to the sender
						command.getSender().tell(new BlockChainManagerBehavior.HashResultCommand(hashResult));
						return Behaviors.same();
					} else {
						log.info("null");
						
						//lets say it gives excpetkion sometime
						//since we are terminating and having a monitor signla, it can be handled there
						Random random = new Random();
					//	if(random.nextInt(2) == 1)
						//	throw new RuntimeException("unable to create hash sucesfully");
						
						//return Behaviors.same();
						return Behaviors.stopped();
					}
				})
				.build();
	}

}
