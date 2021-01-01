package com.learning.akka.block_chain_akka.models;

public class Block {

	private Transaction transaction;
	
	//minign wil do the generation of this nonce based on difficulty
	//meaning hash shud start with n 0's so will be looping and time consuming
	private int nonce;
	
	private String hash;
	
	private String previousHash;

	public Block(Transaction transaction, String previousHash) {
		this.transaction = transaction;
		this.previousHash = previousHash;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public int getNonce() {
		return nonce;
	}

	public void setNonce(int nonce) {
		this.nonce = nonce;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getPreviousHash() {
		return previousHash;
	}

	public void setPreviousHash(String previousHash) {
		this.previousHash = previousHash;
	}
	
	
}
