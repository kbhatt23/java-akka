package com.learning.akka.block_chain_akka.runner;
public class BlockChainAkkaMain {
		
	public static void main(String[] args) {
			
		BlockChainMiner miner = new BlockChainMiner();
		miner.mineBlocks();
		
	}
	
}
