package org.stancumihai.model.strategy;

import org.stancumihai.model.Client;
import org.stancumihai.model.Queue;

import java.util.List;

public interface Strategy {

    void addTask(List<Queue> servers, Client task);
}
