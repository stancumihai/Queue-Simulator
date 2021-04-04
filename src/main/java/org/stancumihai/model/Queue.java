package org.stancumihai.model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Queue implements Runnable {

    static int cnt = 0;
    private int ID;
    private BlockingQueue<Client> clients;
    private AtomicInteger waitingPeriod;

    public Queue() {
        this.ID = cnt + 1;
        cnt++;
        this.clients = new LinkedBlockingQueue<>();
        this.waitingPeriod = new AtomicInteger();
    }

    public void addClient(Client client) {
        clients.add(client);
        for (int i = 0; i < client.gettService().get(); i++) {
            waitingPeriod.getAndIncrement();
        }
    }

    @Override
    public void run() {
        while (true) {
            if (!clients.isEmpty()) {
                Client thisClient = clients.peek();
                AtomicInteger val = new AtomicInteger(thisClient.gettService().get());
                try {
                    for (int i = thisClient.gettService().get(); i > 0; i--) {
                        Thread.sleep(1000);
                        waitingPeriod.getAndDecrement();
                        val.getAndDecrement();
                        thisClient.settService(val);
                    }
                    clients.remove();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public BlockingQueue<Client> getClients() {
        return clients;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setClients(BlockingQueue<Client> clients) {
        this.clients = clients;
    }

    public AtomicInteger getWaitingPeriod() {
        return waitingPeriod;
    }

    public void setWaitingPeriod(AtomicInteger waitingPeriod) {
        this.waitingPeriod = waitingPeriod;
    }

    @Override
    public String toString() {
        return "Queue{" +
                "ID=" + ID +
                ", clients=" + clients +
                ", waitingPeriod=" + waitingPeriod +
                '}';
    }
}