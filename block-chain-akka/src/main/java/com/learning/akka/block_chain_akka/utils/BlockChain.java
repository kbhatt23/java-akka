package com.learning.akka.block_chain_akka.utils;

import java.util.LinkedList;

import com.learning.akka.block_chain_akka.models.Block;
import com.learning.akka.block_chain_akka.models.BlockValidationException;



public class BlockChain {

	// shud be good as size is usually very large and we do only insertions
		private LinkedList<Block> blocks = new LinkedList<>();
		
		public void addBlock(Block block) throws BlockValidationException{
			//first entrys hash is 0
			String chainPreviousHash = "0";
			if(blocks.size() > 0) {
				 chainPreviousHash = blocks.getLast().getHash();
			}
			
			
			//need to check list last item's has is equal to newly adding item's previous hash
			
			if(!block.getPreviousHash().equals(chainPreviousHash)) {
				throw new BlockValidationException();
			}
			
			if(!BlockUtil.validateBlock(block)) {
				throw new BlockValidationException();
			}
			blocks.add(block);
		}
		
		public void printAndValidate() {
			String lastHash = "0";
			for (Block block : blocks) {
				System.out.println("Block " + block.getTransaction().getId() + " ");
				System.out.println(block.getTransaction());
				
				if (block.getPreviousHash().equals(lastHash)) {
					System.out.print("Last hash matches ");
				} else {
					System.out.print("Last hash doesn't match ");
				}
				
				if (BlockUtil.validateBlock(block)) {
					System.out.println("and hash is valid");
				} else {
					System.out.println("and hash is invalid");
				}
				
				lastHash = block.getHash();
				
			}
		}

		public String getLastHash() {
			if (blocks.size() > 0)
				return blocks.getLast().getHash();
			return null;
		}

		public int getSize() {
			return blocks.size();
		}
}
