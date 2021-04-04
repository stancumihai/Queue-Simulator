package org.stancumihai.model.util;

import org.stancumihai.model.Client;

import java.util.Comparator;

public class ArrivalTimeSort implements Comparator<Client> {

    @Override
    public int compare(Client o1, Client o2) {
        return Integer.compare(o1.gettArrival().get(), o2.gettArrival().get());
    }
}
