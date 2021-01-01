package com.learning.akka.akka_concurrency.behaviours;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Duration;
import java.util.SortedSet;
import java.util.TreeSet;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class PrimeNumberManagerBehavior extends AbstractBehavior<PrimeNumberManagerBehavior.Command>{
	
	private SortedSet<BigInteger> primes = new TreeSet<BigInteger>();
	 private ActorRef<SortedSet<BigInteger>> mainActor;

	private PrimeNumberManagerBehavior(ActorContext<PrimeNumberManagerBehavior.Command> context) {
		super(context);
	}
	//marker interface
	public interface Command{
		
	}
	
	public static Behavior<PrimeNumberManagerBehavior.Command> create(){
		return Behaviors.setup(PrimeNumberManagerBehavior::new);
	}
	
	//remmeber message has to be immutable
	//both string and biginteger implemnets serializable for message transfer
	public static class CreateCommand implements Serializable,Command{
		private static final long serialVersionUID = -7951999893068417705L;
		//for string and biginteger is immutable and hence no issues in return in gettter methods
		private final String message;
		private ActorRef<SortedSet<BigInteger>> mainActor;
		public ActorRef<SortedSet<BigInteger>> getMainActor() {
			return mainActor;
		}
		public CreateCommand(String message,ActorRef<SortedSet<BigInteger>> mainActor) {
			this.message = message;
			this.mainActor=mainActor;
		}
		public CreateCommand() {
			this(null, null);
		}
		public String getMessage() {
			return message;
		}
	}
	public static class MergeCommand implements Serializable,Command{
		private static final long serialVersionUID = -7951999893068417705L;
		//for string and biginteger is immutable and hence no issues in return in gettter methods
		private final BigInteger result;
		public MergeCommand(BigInteger result) {
			this.result = result;
		}
		public MergeCommand() {
			this.result = null;
		}
		public BigInteger getResult() {
			return result;
		}
	}
	
	public static class FailureCommand implements Serializable,Command{
		private static final long serialVersionUID = -7951999893068417705L;
		//for string and biginteger is immutable and hence no issues in return in gettter methods
		private final ActorRef<PrimeNumberWorkerBehaviour.Command> worker;
		public FailureCommand(ActorRef<PrimeNumberWorkerBehaviour.Command> worker) {
			this.worker = worker;
		}
		public FailureCommand() {
			this.worker = null;
		}
		public ActorRef<PrimeNumberWorkerBehaviour.Command> getWorker() {
			return worker;
		}
		
	}
	
	
	public static class CompletionCommand implements Serializable,Command{
		private static final long serialVersionUID = -7951999893068417705L;
	}

	@Override
	public Receive<PrimeNumberManagerBehavior.Command> createReceive() {
		
	return 	newReceiveBuilder()
			.onMessage(CreateCommand.class, createCommand ->{
				String message = createCommand.getMessage();
				if("create".equals(message)) {
					for(int i=0; i<20 ; i++) {
						this.mainActor=createCommand.getMainActor();
						String name = "prime-number-worker_"+i;
						//System.out.println("assining task to "+name);
						ActorRef<PrimeNumberWorkerBehaviour.Command> spawn = getContext().spawn(PrimeNumberWorkerBehaviour.create(), name);
						
						//spawn.tell(new PrimeNumberWorkerBehaviour.Command("start", getContext().getSelf()));
						//for retrying we are using ask patter
						sendStartCommandWithRetry(spawn);
					}
				}
				return this;
			})
			.onMessage(MergeCommand.class, mergeCommand ->{
				BigInteger result = mergeCommand.getResult();
				System.out.println("Found Result : "+result);
				//no need to synchronize as items are guranteed to come in order of message sent by sender
				primes.add(result);
				
				if(primes.size()==20) {
					//System.out.println("All Task completed , final result "+primes);
					
					//also send message for main actor
					
					mainActor.tell(primes);
					
					//send competin event
					getContext().getSelf().tell(new CompletionCommand());
				}
				return this;
			})
			.onMessage(FailureCommand.class, failureCommand ->{
				//retry sending the command again
				sendStartCommandWithRetry(failureCommand.getWorker());
				return Behaviors.same();
			})
//			.onAnyMessage(command ->{
//				String message = command.getMessage();
//				if("create".equals(message)) {
//					for(int i=0; i<20 ; i++) {
//						String name = "prime-number-worker_"+i;
//						//System.out.println("assining task to "+name);
//						ActorRef<PrimeNumberWorkerBehaviour.Command> spawn = getContext().spawn(PrimeNumberWorkerBehaviour.create(), name);
//						spawn.tell(new PrimeNumberWorkerBehaviour.Command("start", getContext().getSelf()));
//					}
//				}else if("merge".equals(message)) {
//					BigInteger result = command.getResult();
//					System.out.println("Found Result : "+result);
//				}
//				return this;
//			})
			.onMessage(CompletionCommand.class, compeltionCommand ->{
				//terminate the child actors, however this is not needed if we are closing the parent child gets stopped on its own
				getContext().getChildren().forEach(childRef -> getContext().stop(childRef));
				//no toher thing to temrinate
				
				return Behaviors.stopped();
			})
			
		.build();
	}
	
	//in case there are chances all messgaes do not come, we need to timeout and retry again for that actor
	public void sendStartCommandWithRetry(ActorRef<PrimeNumberWorkerBehaviour.Command> workerActor) {
		
		getContext().ask(Command.class, workerActor, Duration.ofSeconds(5),
				me -> new PrimeNumberWorkerBehaviour.Command("start", me), 
				(response,error) ->{
					if(response != null) {
						return response;
					}else {
						getContext().getLog().info("sendStartCommandWithRetry: error occurred for worker "+workerActor.path());
						//in case of error cusotm message
						return new FailureCommand(workerActor);
					}
				});
	}
	

}
