package org.stancumihai.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.stancumihai.Main;

import java.io.IOException;

public class SecondaryController {

    @FXML
    public TextField textField;

    @FXML
    private void changeToPrimary() throws IOException {
        Main.setRoot("primary");
    }
}
