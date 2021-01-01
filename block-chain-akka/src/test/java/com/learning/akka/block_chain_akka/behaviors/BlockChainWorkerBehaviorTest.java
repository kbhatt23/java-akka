package com.learning.akka.block_chain_akka.behaviors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.learning.akka.block_chain_akka.behaviors.BlockChainManagerBehavior.HashResultCommand;
import com.learning.akka.block_chain_akka.models.Block;
import com.learning.akka.block_chain_akka.models.HashResult;
import com.learning.akka.block_chain_akka.utils.BlocksData;

import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;

public class BlockChainWorkerBehaviorTest {

	@Test
	public void testNoonceGenerationBasicSucess() {
		TestInbox<BlockChainManagerBehavior.Command> testActorInbox = TestInbox.create();
		// BehaviorTestKit is useful in unit test, loads only that specific behavior and
		// other actors and behaviors will be ignored
		BehaviorTestKit<BlockChainWorkerBehavior.Command> testKit = BehaviorTestKit
				.create(BlockChainWorkerBehavior.create(), "test-worker");
		Block firstBlock = BlocksData.getNextBlock(0, "0");
		// int difficulty= 4;
		// for 3 result will be sucesfull
		int difficulty = 3;
		BlockChainWorkerBehavior.Command message = new BlockChainWorkerBehavior.Command(firstBlock, 0, difficulty,
				testActorInbox.getRef());
		testKit.run(message);

		// fail();
		// assertions
		List<CapturedLogEvent> allLogEntries = testKit.getAllLogEntries();
		assertEquals(2, allLogEntries.size());
		assertEquals("Task started", allLogEntries.get(0).message());
		// System.out.println("found stirng "+allLogEntries.get(1).message());
		// sucess
		assertEquals(true, allLogEntries.get(1).message().contains("Noonce Generated hash"));
	}

	@Test
	public void testNoonceGenerationBasicFailure() {
		TestInbox<BlockChainManagerBehavior.Command> testActorInbox = TestInbox.create();
		// BehaviorTestKit is useful in unit test, loads only that specific behavior and
		// other actors and behaviors will be ignored
		BehaviorTestKit<BlockChainWorkerBehavior.Command> testKit = BehaviorTestKit
				.create(BlockChainWorkerBehavior.create(), "test-worker");
		Block firstBlock = BlocksData.getNextBlock(0, "0");
		// int difficulty= 4;
		// for 3 result will be sucesfull
		int difficulty = 4;
		BlockChainWorkerBehavior.Command message = new BlockChainWorkerBehavior.Command(firstBlock, 0, difficulty,
				testActorInbox.getRef());
		testKit.run(message);

		// fail();
		// assertions
		List<CapturedLogEvent> allLogEntries = testKit.getAllLogEntries();
		assertEquals(2, allLogEntries.size());
		assertEquals("Task started", allLogEntries.get(0).message());
		// System.out.println("found stirng "+allLogEntries.get(1).message());
		// failture
		assertEquals("null", allLogEntries.get(1).message());

	}

	@Test
	public void testNoonceGenerationBasicSuccessMessage() {
		TestInbox<BlockChainManagerBehavior.Command> testActorInbox = TestInbox.create();
		// BehaviorTestKit is useful in unit test, loads only that specific behavior and
		// other actors and behaviors will be ignored
		BehaviorTestKit<BlockChainWorkerBehavior.Command> testKit = BehaviorTestKit
				.create(BlockChainWorkerBehavior.create(), "test-worker");
		Block firstBlock = BlocksData.getNextBlock(0, "0");
		// int difficulty= 4;
		// for 3 result will be sucesfull
		int difficulty = 3;
		String expectedZeros = "";
		for(int i=0; i<3;i++) {
			expectedZeros+="0";
		}
		BlockChainWorkerBehavior.Command message = new BlockChainWorkerBehavior.Command(firstBlock, 0, difficulty,
				testActorInbox.getRef());
		testKit.run(message);

		assertTrue(testActorInbox.hasMessages());
		HashResultCommand receiveMessage = (HashResultCommand) testActorInbox.receiveMessage();
		 HashResult hashResult = receiveMessage.getHashResult();
		assertTrue(hashResult.getHash().startsWith(expectedZeros));
		
	}

	@Test
	public void testNoonceGenerationBasicFailureMessage() {
		TestInbox<BlockChainManagerBehavior.Command> testActorInbox = TestInbox.create();
		// BehaviorTestKit is useful in unit test, loads only that specific behavior and
		// other actors and behaviors will be ignored
		BehaviorTestKit<BlockChainWorkerBehavior.Command> testKit = BehaviorTestKit
				.create(BlockChainWorkerBehavior.create(), "test-worker");
		Block firstBlock = BlocksData.getNextBlock(0, "0");
		// int difficulty= 4;
		// for 3 result will be sucesfull
		int difficulty = 4;
		BlockChainWorkerBehavior.Command message = new BlockChainWorkerBehavior.Command(firstBlock, 0, difficulty,
				testActorInbox.getRef());
		testKit.run(message);

		//shud not recieve any message as unable to generate hash with such high difficulty level
		assertFalse(testActorInbox.hasMessages());
		
		
	}
}
