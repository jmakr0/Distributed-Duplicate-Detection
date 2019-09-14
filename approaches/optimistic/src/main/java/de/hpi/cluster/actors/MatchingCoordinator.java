package de.hpi.cluster.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MatchingCoordinator extends AbstractActor {

    public static final String DEFAULT_NAME = "matching-coordinator";

    private double similarityThreshold;
    private int thresholdMin;
    private int thresholdMax;
    private Set<Set<Integer>> duplicates = new HashSet<>();
    private List<ActorRef> working = new LinkedList<>();
    private Set<ActorRef> done = new HashSet<>();
    private ActorRef master;

    public static Props props() {
        return Props.create(MatchingCoordinator.class);
    }

    private final LoggingAdapter log = Logging.getLogger(this.context().system(), this);

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class ConfigMessage implements Serializable {
        private static final long serialVersionUID = -7332428742629706627L;
        private ConfigMessage() {}
        private Config config;
    }

    @Data
    @AllArgsConstructor
    public static class StartSimilarityMessage implements Serializable {
        private static final long serialVersionUID = -4243198881468862195L;
        private StartSimilarityMessage() {}
        protected ActorRef worker;
    }

    @Data @AllArgsConstructor
    public static class DuplicateMessage implements Serializable {
        private static final long serialVersionUID = -1111194311112342425L;
        private DuplicateMessage() {}
        protected Set<Set<Integer>> duplicates;
    }

    @Data @AllArgsConstructor
    public static class WorkerFinishedMatchingMessage implements Serializable {
        private static final long serialVersionUID = -1111194361812333325L;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();

        Reaper.watchWithDefaultReaper(this);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();

        // Log the stop event
        this.log.info("Stopped {}.", this.getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ConfigMessage.class, this::handle)
                .match(StartSimilarityMessage.class, this::handle)
                .match(DuplicateMessage.class, this::handle)
                .match(WorkerFinishedMatchingMessage.class, this::handle)
                .matchAny(object -> this.log.info("Received unknown message: \"{}\"", object.toString()))
                .build();
    }

    private void handle(ConfigMessage configMessage) {
        this.master = this.sender();

        Config config = configMessage.config;
        this.similarityThreshold = config.getDouble("der.duplicate-detection.similarity-threshold");
        this.thresholdMin = config.getInt("der.similarity.abs-comparator.threshold-min");
        this.thresholdMax = config.getInt("der.similarity.abs-comparator.threshold-max");
    }

    private void handle(StartSimilarityMessage startSimilarityMessage) {
        ActorRef worker = startSimilarityMessage.worker;

        this.working.add(worker);
        this.done.remove(worker);

        this.log.info("Similarity message to {}", worker.path().name());
        worker.tell(new Worker.SimilarityMessage(this.similarityThreshold, this.thresholdMin, this.thresholdMax), this.master);
    }

    private void handle(DuplicateMessage duplicateMessage) {
//        this.log.info("Duplicate {}", duplicateMessage.duplicates);

        this.duplicates.addAll(duplicateMessage.duplicates);
    }

    private void handle(WorkerFinishedMatchingMessage workerFinishedMatchingMessage) {
        ActorRef worker = this.sender();

        this.working.remove(worker);

        if (!this.working.contains(worker)) {
            this.done.add(this.sender());
        }

        if (working.isEmpty()) {
            this.log.info("finish similarity");
            this.log.info("number of duplicates: {}", this.duplicates.size());
//            this.log.info(String.valueOf(this.duplicates));
            this.master.tell(new Master.MatchingCompletedMessage(this.duplicates, this.done), this.self());
        }
    }

}
