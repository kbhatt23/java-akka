package com.learning.akka.block_chain_akka.runner;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

import com.learning.akka.block_chain_akka.behaviors.BlockChainManagerBehavior;
import com.learning.akka.block_chain_akka.models.Block;
import com.learning.akka.block_chain_akka.models.BlockValidationException;
import com.learning.akka.block_chain_akka.models.HashResult;
import com.learning.akka.block_chain_akka.utils.BlockChain;
import com.learning.akka.block_chain_akka.utils.BlocksData;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;

public class BlockChainMiner {
	
	int difficultyLevel = 5;
	BlockChain blocks = new BlockChain();
	long start = System.currentTimeMillis();
	ActorSystem<BlockChainManagerBehavior.Command> actorSystem;

	private void mineNextBlock() {
		int nextBlockId = blocks.getSize();
		if (nextBlockId < 10) {
			String lastHash = nextBlockId > 0 ? blocks.getLastHash() : "0";
			Block block = BlocksData.getNextBlock(nextBlockId, lastHash);
			CompletionStage<HashResult> results = AskPattern.ask(actorSystem,
					me -> new BlockChainManagerBehavior.StartHashCommand(5, me, block),
					Duration.ofSeconds(30),
					actorSystem.scheduler());
				
			results.whenComplete( (reply,failure) -> {
				
				if (reply == null || !reply.isComplete()) {
					System.out.println("ERROR: No valid hash was found for a block");
				}
				
				block.setHash(reply.getHash());
				block.setNonce(reply.getNonce());
				
				try {
					blocks.addBlock(block);
					System.out.println("Block added with hash : " + block.getHash());
					System.out.println("Block added with nonce: " + block.getNonce());
					mineNextBlock();
				} catch (BlockValidationException e) {
					System.out.println("ERROR: No valid hash was found for a block");
				}
			});
			
		}
		else {
			Long end = System.currentTimeMillis();
			actorSystem.terminate();
			blocks.printAndValidate();
			System.out.println("Time taken " + (end - start) + " ms.");
		}
	}
	
	public void mineBlocks() {
		
		actorSystem = ActorSystem.create(BlockChainManagerBehavior.create(), "BlockChainMiner");
		mineNextBlock();
	}
	
}
