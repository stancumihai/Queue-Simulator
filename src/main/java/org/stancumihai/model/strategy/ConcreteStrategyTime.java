package org.stancumihai.model.strategy;

import org.stancumihai.model.Client;
import org.stancumihai.model.Queue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcreteStrategyTime implements Strategy {

    private static AtomicInteger shortestQueueIndex = new AtomicInteger(0);

    @Override
    public void addTask(List<Queue> servers, Client client) {

        AtomicInteger min = servers.get(0).getWaitingPeriod();
        AtomicInteger poz = new AtomicInteger(0);
        for (int i = 1; i < servers.size(); i++) {
            if (servers.get(i).getWaitingPeriod().get() < min.get()) {
                min = servers.get(i).getWaitingPeriod();
                poz.set(i);
            }
        }
        servers.get(poz.get()).addClient(client);
        setQueueIndex(poz);
    }

    public static AtomicInteger getQueueIndex() {
        return shortestQueueIndex;
    }

    public static void setQueueIndex(AtomicInteger queueIndex) {
        ConcreteStrategyTime.shortestQueueIndex = queueIndex;
    }
}
