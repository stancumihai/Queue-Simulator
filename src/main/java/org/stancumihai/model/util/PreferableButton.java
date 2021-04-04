package org.stancumihai.model.util;

import javafx.scene.control.Button;
import org.stancumihai.model.Client;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class PreferableButton {

    public static Button preferableButton(AtomicInteger index, String text) {
        Button button = new Button(text + (index.get() + 1));
        button.setPrefSize(75, 30);
        return button;
    }

    public static Button inQueueClients(ArrayList<Client> clients, int index, String service) {
            Button button = new Button((index + 1) + "|" + clients.get(index).gettArrival() + "|" + service);
            button.setPrefSize(80, 30);
            return button;
    }
}
