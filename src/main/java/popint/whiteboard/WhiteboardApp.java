package popint.whiteboard;

import javafx.application.Application;
import javafx.stage.Stage;

public class WhiteboardApp extends Application {
    private WhiteboardController controller;

    @Override
    public void start(Stage primaryStage) {
        controller = new WhiteboardController();
        WhiteboardUI ui = new WhiteboardUI(primaryStage, controller);
        ui.initializeUI();
    }

    public static void main(String[] args) {
        launch(args);
    }
}