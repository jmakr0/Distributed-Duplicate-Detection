package de.hpi.cluster.actors.listeners;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ClusterListener extends AbstractActor {

	////////////////////////
	// Actor Construction //
	////////////////////////
	
	public static final String DEFAULT_NAME = "clusterListener";

	public static Props props() {
		return Props.create(ClusterListener.class);
	}

	/////////////////
	// Actor State //
	/////////////////
	
	private final LoggingAdapter log = Logging.getLogger(this.context().system(), this);
	private final Cluster cluster = Cluster.get(this.context().system());

	/////////////////////
	// Actor Lifecycle //
	/////////////////////
	
	@Override
	public void preStart() throws Exception {
		super.preStart();

		this.cluster.subscribe(this.self(), MemberEvent.class, UnreachableMember.class);

		// Register at this actor system's reaper
//		Reaper.watchWithDefaultReaper(this);
	}

	@Override
	public void postStop() throws Exception {
		super.postStop();

		this.cluster.unsubscribe(this.self());

		this.log.info("Stopped {}.", this.getSelf());
	}

	////////////////////
	// Actor Behavior //
	////////////////////
	
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(CurrentClusterState.class, state -> {
			this.log.info("Current members: {}", state.members());
		}).match(MemberUp.class, mUp -> {
			this.log.info("Member is Up: {}", mUp.member());
		}).match(UnreachableMember.class, mUnreachable -> {
			this.log.info("Member detected as unreachable: {}", mUnreachable.member());
		}).match(MemberRemoved.class, mRemoved -> {
			this.log.info("Member is Removed: {}", mRemoved.member());
		}).match(MemberEvent.class, message -> {
			// ignore
		}).build();
	}
}
