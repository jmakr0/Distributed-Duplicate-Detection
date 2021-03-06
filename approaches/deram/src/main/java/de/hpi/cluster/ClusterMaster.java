package de.hpi.cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
import de.hpi.cluster.actors.MessageCoordinator;
import de.hpi.cluster.actors.Reaper;
import de.hpi.cluster.actors.Worker;
import de.hpi.cluster.actors.listeners.ClusterListener;

public class ClusterMaster extends ClusterSystem {
	
	public static final String MASTER_ROLE = "master";

	public static void start(String actorSystemName, Config config) {
		int workers = config.getInt("der.cluster.master.worker-actors");
		String host = config.getString("der.cluster.master.host-address");
		int port = config.getInt("der.cluster.master.port");
		String loglevel = config.getString("der.logging.level");

        final Config actorSystemConfig = createConfiguration(actorSystemName, MASTER_ROLE, host, port, host, port, loglevel);
		
		final ActorSystem system = createSystem(actorSystemName, actorSystemConfig);
		
		Cluster.get(system).registerOnMemberUp(new Runnable() {
			@Override
			public void run() {
				system.actorOf(Reaper.props(), Reaper.DEFAULT_NAME);

				system.actorOf(ClusterListener.props(), ClusterListener.DEFAULT_NAME);

				ActorRef master = system.actorOf(MessageCoordinator.props(), MessageCoordinator.DEFAULT_NAME);
				master.tell(new MessageCoordinator.ConfigMessage(config), ActorRef.noSender());
				
				for (int i = 0; i < workers; i++)
					system.actorOf(Worker.props(), Worker.DEFAULT_NAME + i);
				
			}
		});

	}

}
