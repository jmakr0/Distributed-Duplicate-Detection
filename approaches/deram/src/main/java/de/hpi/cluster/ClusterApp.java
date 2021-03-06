package de.hpi.cluster;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class ClusterApp {

    public static final String ACTOR_SYSTEM_NAME = "deram";

    public static void main(String[] args) {

        Command command = new Command();
        JCommander jCommander = JCommander.newBuilder()
                .addCommand(ClusterMaster.MASTER_ROLE, command)
                .addCommand(ClusterWorker.WORKER_ROLE, command)
                .build();

        try {
            jCommander.parse(args);

            if (jCommander.getParsedCommand() == null) {
                throw new ParameterException("No command given.");
            }

            // load config
            Config config = ConfigFactory.parseFile(new File(command.configPath));

            switch (jCommander.getParsedCommand()) {
                case ClusterMaster.MASTER_ROLE:
                    ClusterMaster.start(ACTOR_SYSTEM_NAME, config);
                    break;
                case ClusterWorker.WORKER_ROLE:
                    ClusterWorker.start(ACTOR_SYSTEM_NAME, config);
                    break;
                default:
                    throw new AssertionError();
            }

        } catch (ParameterException e) {
            System.out.printf("Could not parse args: %s\n", e.getMessage());
            if (jCommander.getParsedCommand() == null) {
                jCommander.usage();
            } else {
                jCommander.usage(jCommander.getParsedCommand());
            }
            System.exit(1);
        }
    }

    static class Command {
        @Parameter(names = {"-c", "--config"}, description = "path to the config file", required = true)
        String configPath;
    }

}
