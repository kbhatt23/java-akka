package com.learning.akka.block_chain_akka.runner;

import com.learning.akka.block_chain_akka.behaviors.BehaviorWithStash;

import akka.actor.typed.ActorSystem;

public class StashMessageRunner {

	public static void main(String[] args) {
		ActorSystem<String> system = ActorSystem.create(BehaviorWithStash.create(), "stash-system");
		
		for(int i=0 ; i<50;i++) {
			system.tell("start");
		}
	}

}
