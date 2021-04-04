package org.stancumihai.model;

import org.stancumihai.model.strategy.ConcreteStrategyQueue;
import org.stancumihai.model.strategy.ConcreteStrategyTime;
import org.stancumihai.model.strategy.Strategy;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {

    private final List<Queue> servers = new ArrayList<>();
    private final int maxNoServers;
    private final int maxTaskPerServer;
    private Strategy strategy = new ConcreteStrategyTime();

    public Scheduler(int maxNoServers, int maxTaskPerServer) {
        this.maxNoServers = maxNoServers;
        this.maxTaskPerServer = maxTaskPerServer;
        initScheduler(maxNoServers, maxTaskPerServer);
    }

    public void initScheduler(int maxNoServers, int maxTaskPerServer) {
        for (int i = 0; i < maxNoServers; i++) {
            Queue queue = new Queue();
            servers.add(queue);
            Thread t = new Thread(queue);
            t.start();
        }
    }

    public void changeStrategy(SelectionPolicy policy) {
        if (policy == SelectionPolicy.SHORTESTH_QUEUE) {
            strategy = new ConcreteStrategyQueue();
        }
        if (policy == SelectionPolicy.SHORTESTH_TIME) {
            strategy = new ConcreteStrategyTime();
        }
    }

    public int dispatchTask(Client client) {
        strategy.addTask(servers, client);
        return ConcreteStrategyTime.getQueueIndex().get();
    }

    public List<Queue> getServers() {
        return servers;
    }
}
