package org.stancumihai.controller;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import org.stancumihai.Main;
import org.stancumihai.model.Client;
import org.stancumihai.model.Queue;
import org.stancumihai.model.Scheduler;
import org.stancumihai.model.util.ArrivalTimeSort;
import org.stancumihai.model.util.PreferableButton;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PrimaryController implements Initializable {

    private final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("file.txt"));
    private final int MAX_SPINNER_SIZE = 100;
    private final ArrayList<Client> generatedClients = new ArrayList<>();
    private ArrayList<FlowPane> flowPanes = new ArrayList<>();
    private ArrayList<Button> clientButtons = new ArrayList<>();
    private final AtomicInteger rowIndex = new AtomicInteger(0);
    private AtomicInteger columnIndex = new AtomicInteger(0);
    private Scheduler scheduler;
    private Thread backGroundThread;
    private static IntegerProperty clientSpinnerProperty, queuesSpinnerProperty, simTimeSpinnerProperty, minimArrTimeSpinnerProperty,
            maximumArrTimeSpinnerProperty, minServiceTimeSpinnerProperty, maxServiceTimeSpinnerProperty;
    private static final ArrayList<Button> queueButtons = new ArrayList<>();
    private int averageWaitingTime = 0;
    private int peakHour = 0;
    public FlowPane waitingLine, flowPane1, flowPane2, flowPane3, flowPane4, flowPane5, flowPane6, flowPane7, flowPane8, flowPane9,
            flowPane10, flowPane11, flowPane12, flowPane13, flowPane14, flowPane15, flowPane16, flowPane17, flowPane18, flowPane19,
            flowPane20;
    public TextArea resultTextField;
    public GridPane gridPaneScrollPane;
    public ScrollPane scrollPane;
    public Button startButton, stopButton, clearButton, initButton;
    public Spinner<Integer> clientsSpinner, queuesSpinner, simTimeSpinner, minimArrTimeSpinner, maxArrTimeSpinner,
            minServiceTimeSpinner, maxServiceTimeSpinner;
    public ProgressBar progressBar;
    public ProgressIndicator progressIndicator;

    public PrimaryController() throws IOException {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initSpinners();
        initBindings();
        initQueueSpinnerProperty();
        initClientsProperty();
        createMainTask();
        initGridPane();
    }

    private int calculateClientsPerHour(List<Queue> servers) {
        int nr = 0;
        for (Queue queue : servers) {
            nr += queue.getClients().size();
        }
        return nr;
    }

    public ArrayList<Client> fromBlockingQueueToArrayList(BlockingQueue<Client> clients) {
        return new ArrayList<>(clients);
    }

    public ArrayList<Button> makeMeGoodButtonList(ArrayList<Client> clientList) {
        ArrayList<Button> buttons = new ArrayList<>();
        for (Client client : clientList) {
            Button button = new Button(client.getID() + "|" + client.gettArrival() + "|" + client.gettService());
            button.setPrefSize(75, 30);
            buttons.add(button);
        }
        return buttons;
    }

    private void renderGUITask() throws IOException {
        AtomicInteger currentTime = new AtomicInteger(0);
        AtomicInteger deletedSize = new AtomicInteger(0);

        while (currentTime.get() < simTimeSpinner.getValue()) {
            ArrayList<ArrayList<Button>> buttonsMatrix = new ArrayList<>(new ArrayList<>());
            for (int i = 0; i < 100; i++) {
                buttonsMatrix.add(new ArrayList<>());
            }
            BlockingQueue<Client> allClientsToBePutInGui = new LinkedBlockingQueue<>();
            ArrayList<Client> toBeDeletedClients = new ArrayList<>();
            ArrayList<Button> toBeDeletedButtons = new ArrayList<>();
            AtomicInteger shortestQueueIndex = new AtomicInteger(0);
            for (Client client : generatedClients) {
                if (client.gettArrival().get() == currentTime.get()) {
                    shortestQueueIndex.set(scheduler.dispatchTask(client));
                    averageWaitingTime = averageWaitingTime + scheduler.getServers().
                            get(shortestQueueIndex.get()).getWaitingPeriod().get() - client.gettService().get();
                    toBeDeletedClients.add(client);
                    toBeDeletedButtons.add(clientButtons.get(deletedSize.get()));
                    deletedSize.getAndIncrement();
                }
            }
            if (calculateClientsPerHour(scheduler.getServers()) > peakHour) {
                peakHour = calculateClientsPerHour(scheduler.getServers());
            }
            this.bufferedWriter.write("Time " + currentTime.get());
            this.bufferedWriter.write("\n");
            clientButtons.removeAll(toBeDeletedButtons);
            generatedClients.removeAll(toBeDeletedClients);
            toBeDeletedButtons.clear();
            for (int i = 0; i < toBeDeletedClients.size(); i++) {
                Button button = PreferableButton.inQueueClients(toBeDeletedClients, i, toBeDeletedClients.get(i).gettService().toString());
                toBeDeletedButtons.add(button);
            }
            for (Client client : generatedClients) {
                if (client.gettService().get() != 0) {
                    bufferedWriter.write(client.toString());
                    bufferedWriter.write("\n");
                }
            }
            for (Queue queue : scheduler.getServers()) {
                bufferedWriter.write(queue.toString());
                bufferedWriter.write("\n");
            }

            for (int i = 0; i < queueButtons.size(); i++) {
                BlockingQueue<Client> blockingQueue = scheduler.getServers().get(i).getClients();
                ArrayList<Client> clients = fromBlockingQueueToArrayList(blockingQueue);
                ArrayList<Button> buttonsLine = makeMeGoodButtonList(clients);
                int cnt = 0;
                for (Button button : buttonsLine) {
                    buttonsMatrix.get(i).add(button);
                }
            }

            System.out.println(currentTime.get());
            for (int i = 0; i < queueButtons.size(); i++) {
                buttonsMatrix.get(i).forEach(System.out::println);
            }
            System.out.println();
            bufferedWriter.write("------->");
            bufferedWriter.write("------->");
            bufferedWriter.write("\n");
            try {
                Platform.runLater(() -> {
                    int previousClientButtonsSize = clientButtons.size() + toBeDeletedClients.size();
                    for (int i = 0; i < previousClientButtonsSize; i++) {
                        if (waitingLine.getChildren() != null) {
                            waitingLine.getChildren().remove(0);
                        }
                    }
                    for (Button clientButton : clientButtons) {
                        Objects.requireNonNull(waitingLine.getChildren()).add(clientButton);
                    }

                    for (int i = 0; i < queueButtons.size(); i++) {
                        flowPanes.get(i).getChildren().clear();
                    }
                    for (int i = 0; i < queueButtons.size(); i++) {
                        flowPanes.get(i).getChildren().addAll(buttonsMatrix.get(i));
                    }
                    deletedSize.set(0);
                    toBeDeletedButtons.clear();
                    toBeDeletedClients.clear();
                });
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentTime.getAndIncrement();
            progressBar.setProgress((float) currentTime.get() / (float) simTimeSpinner.getValue());
            progressIndicator.setProgress((float) currentTime.get() / (float) simTimeSpinner.getValue());
        }
        bufferedWriter.close();
        if (currentTime.get() == simTimeSpinner.getValue()) {
            progressIndicator.setProgress(1);
            progressBar.setProgress(1);
            resultTextField.appendText("Done simulating!\n");
            resultTextField.appendText("The peak hour is :" + peakHour);
            resultTextField.appendText("    The average waiting time is: " + (float) averageWaitingTime / (float) clientsSpinner.getValue());
        }
    }

    private void createMainTask() {
        Runnable runnable = () -> {
            try {
                renderGUITask();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        backGroundThread = new Thread(runnable);
    }

    public void initialiseSimulation() {
        scheduler = new Scheduler(queuesSpinner.getValue(), clientsSpinner.getValue());
    }

    @FXML
    public void changeToSecondary() throws IOException {
        Main.setRoot("secondary");
    }

    @FXML
    public void startButton() {
        initialiseSimulation();
        backGroundThread.start();
    }

    @FXML
    public void clearButton() {
        gridPaneScrollPane.getChildren().clear();
        progressBar.setProgress(0);
        progressIndicator.setProgress(0);
        columnIndex.set(0);
        rowIndex.set(0);
        initSpinners();
    }

    @FXML
    void stopButton() {
        try {
            if (scrollPane.getContent() != null) {
                System.exit(0);
            }
        } catch (Exception e) {
            System.exit(0);
        }
    }

    @FXML
    void initButton() {
        initWaitingLine();
    }

    public void initWaitingLine() {
        ArrayList<Client> clients = generateNTasks();
        int copyVal = clientSpinnerProperty.getValue();
        waitingLine.getChildren().removeAll(clientButtons);
        columnIndex = new AtomicInteger(2);
        clientButtons = new ArrayList<>();
        for (int i = 0; i < copyVal; i++) {
            Button button = PreferableButton.inQueueClients(clients, i, String.valueOf(clients.get(i).gettService()));
            clientButtons.add(button);
            waitingLine.getChildren().add(i, button);
            columnIndex.getAndIncrement();
        }
    }

    public ArrayList<Client> generateNTasks() {
        Random random = new Random();

        for (int i = 1; i <= clientsSpinner.getValue(); i++) {
            AtomicInteger serviceTime = new AtomicInteger(Math.abs(random.nextInt() % maxServiceTimeSpinner.getValue()));
            while (serviceTime.get() < minServiceTimeSpinner.getValue()) {
                serviceTime.set(random.nextInt(maxServiceTimeSpinner.getValue()));
            }
            AtomicInteger arrivalTime = new AtomicInteger(Math.abs(random.nextInt() % maxArrTimeSpinner.getValue()));
            while (arrivalTime.get() < minServiceTimeSpinner.getValue()) {
                arrivalTime.set(random.nextInt(maxArrTimeSpinner.getValue()));
            }
            generatedClients.add(new Client(i, arrivalTime, serviceTime));
        }
        generatedClients.sort(new ArrivalTimeSort());
        return generatedClients;
    }

    public void initSpinners() {

        clientsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SPINNER_SIZE * 10));
        queuesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SPINNER_SIZE));
        simTimeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SPINNER_SIZE * 2));
        minServiceTimeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SPINNER_SIZE));
        maxServiceTimeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SPINNER_SIZE));
        minimArrTimeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SPINNER_SIZE));
        maxArrTimeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SPINNER_SIZE));
    }

    public void initBindings() {
        clientSpinnerProperty = new SimpleIntegerProperty();
        queuesSpinnerProperty = new SimpleIntegerProperty();
        simTimeSpinnerProperty = new SimpleIntegerProperty();
        minimArrTimeSpinnerProperty = new SimpleIntegerProperty();
        maximumArrTimeSpinnerProperty = new SimpleIntegerProperty();
        minServiceTimeSpinnerProperty = new SimpleIntegerProperty();
        maxServiceTimeSpinnerProperty = new SimpleIntegerProperty();

        clientSpinnerProperty.bind(clientsSpinner.valueProperty());
        queuesSpinnerProperty.bind(queuesSpinner.valueProperty());
        simTimeSpinnerProperty.bind(simTimeSpinner.valueProperty());
        minimArrTimeSpinnerProperty.bind(minimArrTimeSpinner.valueProperty());
        maximumArrTimeSpinnerProperty.bind(maxServiceTimeSpinner.valueProperty());
        minServiceTimeSpinnerProperty.bind(minServiceTimeSpinner.valueProperty());
        maxServiceTimeSpinnerProperty.bind(maxServiceTimeSpinner.valueProperty());
    }

    private void initQueueSpinnerProperty() {

        queuesSpinnerProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() > oldValue.intValue()) {
                queueButtons.add(PreferableButton.preferableButton(rowIndex, "Queue"));
                gridPaneScrollPane.add(queueButtons.get(queueButtons.size() - 1), 0, rowIndex.get() + 1);
                rowIndex.getAndIncrement();
            } else if (newValue.intValue() < oldValue.intValue()) {
                gridPaneScrollPane.getChildren().remove(queueButtons.get(queueButtons.size() - 1));
                queueButtons.remove(queueButtons.size() - 1);
                rowIndex.getAndDecrement();
            }
        });
    }

    private void initClientsProperty() {
        clientSpinnerProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() > oldValue.intValue()) {
                clientButtons.add(PreferableButton.preferableButton(columnIndex, "Client"));
                waitingLine.getChildren().add(columnIndex.get(), clientButtons.get(clientButtons.size() - 1));
                columnIndex.getAndIncrement();
            } else if (newValue.intValue() < oldValue.intValue()) {
                waitingLine.getChildren().remove(clientButtons.get(clientButtons.size() - 1));
                clientButtons.remove(clientButtons.size() - 1);
                columnIndex.getAndDecrement();
            }
        });
    }

    private void initGridPane() {

        flowPanes = new ArrayList<>(100);
        gridPaneScrollPane.getChildren().addAll(1, flowPanes);
        flowPanes = new ArrayList<>(Arrays.asList(flowPane1, flowPane2, flowPane3, flowPane4, flowPane5, flowPane6, flowPane7,
                flowPane8, flowPane9, flowPane10, flowPane11, flowPane12, flowPane13, flowPane14, flowPane15, flowPane16,
                flowPane17, flowPane18, flowPane19, flowPane20));

    }
}