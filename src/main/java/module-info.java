module popint.demo {
    requires javafx.controls;
    requires javafx.swing;
    requires javafx.media;
    requires javafx.fxml;


    opens popint.demo to javafx.fxml;
    exports popint.demo;
    exports popint.me;
    opens popint.me to javafx.fxml;
}