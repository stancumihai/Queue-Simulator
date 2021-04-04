module org.example {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.stancumihai.controller to javafx.fxml;
    exports org.stancumihai;
}