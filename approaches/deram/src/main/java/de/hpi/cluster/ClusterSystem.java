package de.hpi.cluster;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class ClusterSystem {

	protected static Config createConfiguration(String actorSystemName, String actorSystemRole, String host, int port, String masterhost, int masterport, String loglevel) {
		
		// Create the Config with fallback to the application config
		return ConfigFactory.parseString(
				"akka.remote.netty.tcp.hostname = \"" + host + "\"\n" +
						"akka.remote.netty.tcp.port = " + port + "\n" +
						"akka.remote.artery.canonical.hostname = \"" + host + "\"\n" +
						"akka.remote.artery.canonical.port = " + port + "\n" +
						"akka.cluster.roles = [" + actorSystemRole + "]\n" +
						"akka.loglevel = " + loglevel + "\n" +
						"akka.cluster.seed-nodes = [\"akka://" + actorSystemName + "@" + masterhost + ":" + masterport + "\"]")
				.withFallback(ConfigFactory.load("cluster"));
	}
	
	protected static ActorSystem createSystem(String actorSystemName, Config config) {
		
		// Create the ActorSystem
		final ActorSystem system = ActorSystem.create(actorSystemName, config);
		
		// Register a callback that ends the program when the ActorSystem terminates
		system.registerOnTermination(new Runnable() {
			@Override
			public void run() {
				System.exit(0);
			}
		});
		
		// Register a callback that terminates the ActorSystem when it is detached from the cluster
		Cluster.get(system).registerOnMemberRemoved(new Runnable() {
			@Override
			public void run() {
				system.terminate();

				new Thread(() -> {
					try {
						Await.ready(system.whenTerminated(), Duration.create(10, TimeUnit.SECONDS));
					} catch (Exception e) {
						System.exit(-1);
					}
				}).start();
			}
		});
		
		return system;
	}
}
