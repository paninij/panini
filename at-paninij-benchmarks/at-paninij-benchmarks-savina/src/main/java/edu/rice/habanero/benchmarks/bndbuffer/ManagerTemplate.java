package edu.rice.habanero.benchmarks.bndbuffer;

import java.util.LinkedList;
import java.util.Queue;

import org.paninij.benchmarks.savina.util.FlagFuture;
import org.paninij.lang.Capsule;
import org.paninij.lang.Child;

@Capsule public class ManagerTemplate {

    int numConsumers = ProdConsBoundedBufferConfig.numConsumers;
    int numProducers = ProdConsBoundedBufferConfig.numProducers;
    int numTerminatedProducers = 0;

    @Child Consumer[] consumers = new Consumer[numConsumers];
    @Child Producer[] producers = new Producer[numProducers];

    Queue<Double> availableData = new LinkedList<Double>();
    Queue<Consumer> availableConsumers = new LinkedList<Consumer>();
    Queue<Producer> availableProducers = new LinkedList<Producer>();

    int adjustedBufferSize = ProdConsBoundedBufferConfig.bufferSize - numProducers;
    FlagFuture flag = new FlagFuture();

    public void design(Manager self) {
        for (Producer p : producers) p.wire(self);
        for (Consumer c : consumers) c.wire(self);
    }

    public FlagFuture start() {
        for (int i = 0; i < consumers.length; i++) {
            availableConsumers.add(consumers[i]);
            consumers[i].setId(i);
        }

        for (int i = 0; i < producers.length; i++) {
            producers[i].setId(i);
            producers[i].produce();
        }

        return flag;
    }

    public void dataProduced(int id, double data) {

        if (availableConsumers.isEmpty()) {
            availableData.add(data);
        } else {
            availableConsumers.poll().consume(data);
        }

        if (availableData.size() >= adjustedBufferSize) {
            availableProducers.add(producers[id]);
        } else {
            producers[id].produce();
        }

    }

    public void dataConsumed(int id) {

        if (availableData.isEmpty()) {
            availableConsumers.add(consumers[id]);
            tryExit();
        } else {
            consumers[id].consume(availableData.poll());

            if (!availableProducers.isEmpty()) {
                availableProducers.poll().produce();
            }
        }
    }

    public void producerFinished() {
        numTerminatedProducers++;
        tryExit();
    }

    private void tryExit() {
        if (numTerminatedProducers == numProducers && availableConsumers.size() == numConsumers) {
            for (Consumer c : consumers) c.exit();
            for (Producer p : producers) p.exit();
            flag.resolve();
        }
    }

}
