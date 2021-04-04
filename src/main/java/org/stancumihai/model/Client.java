package org.stancumihai.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Client {

    private int ID;
    private AtomicInteger tArrival;
    private AtomicInteger tService;

    public Client(int ID, AtomicInteger tArrival, AtomicInteger tService) {
        this.ID = ID;
        this.tArrival = tArrival;
        this.tService = tService;
    }


    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public AtomicInteger gettArrival() {
        return tArrival;
    }

    public void settArrival(AtomicInteger tArrival) {
        this.tArrival = tArrival;
    }

    public AtomicInteger gettService() {
        return tService;
    }

    public void settService(AtomicInteger tService) {
        this.tService = tService;
    }

    @Override
    public String toString() {
        return "Client{" +
                "ID=" + ID +
                ", tArrival=" + tArrival +
                ", tService=" + tService +
                '}';
    }
}
