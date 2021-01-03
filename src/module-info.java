module DeepCasey {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires java.desktop;

    exports application;
    opens application to javafx.graphics;
}