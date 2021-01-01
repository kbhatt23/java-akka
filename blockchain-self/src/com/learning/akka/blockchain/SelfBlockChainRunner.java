package com.learning.akka.blockchain;

import com.learning.akka.blockchain.model.Block;
import com.learning.akka.blockchain.model.BlockValidationException;
import com.learning.akka.blockchain.model.HashResult;
import com.learning.akka.blockchain.util.BlockChain;
import com.learning.akka.blockchain.util.BlockUtil;
import com.learning.akka.blockchain.util.BlocksData;

public class SelfBlockChainRunner {
	public static void main(String[] args) throws BlockValidationException {
		long start = System.currentTimeMillis();
		//hash of first entry is 0
		String lastHash = "0";
		int difficultyLevel=5;
		BlockChain blockChain = new BlockChain();
		for(int i=0;i<10;i++) {
			Block nextBlock = BlocksData.getNextBlock(i, lastHash);
			
			//generate nonce and hash
			HashResult hashResult = BlockUtil.mineBlock(nextBlock, difficultyLevel,0, 100000000);
			if (hashResult == null) {
				throw new RuntimeException("Didn't find a valid hash for block " + i);
			}
			nextBlock.setHash(hashResult.getHash());
			nextBlock.setNonce(hashResult.getNonce());
			
			//validate and add to list
			blockChain.addBlock(nextBlock);
			
			System.out.println("Block " + i + " hash : " + nextBlock.getHash());
			System.out.println("Block " + i + " nonce: " + nextBlock.getNonce());
			
			//previous hash will now be currents hash
			lastHash = nextBlock.getHash();
		}
		
		Long end = System.currentTimeMillis();
		blockChain.printAndValidate();
		
		System.out.println("Time taken " + (end - start) + " ms.");
	}
}
