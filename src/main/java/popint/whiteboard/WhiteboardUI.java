package popint.whiteboard;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.media.MediaView;
import javafx.util.Pair;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WhiteboardUI {
    private final Stage stage;
    private final WhiteboardController controller;
    private Canvas canvas;
    private StackPane board;

    public WhiteboardUI(Stage stage, WhiteboardController controller) {
        this.stage = stage;
        this.controller = controller;
    }

    public void initializeUI() {
        // Initialize canvas
        canvas = new Canvas(800, 600);
        controller.initializeDrawingContext(canvas);

        // Create UI controls
        Button clearButton = createStyledButton("Clear");
        Button loadImageButton = createStyledButton("Load Image");
        Button saveButton = createStyledButton("Save");
        Button loadMediaButton = createStyledButton("Load Media");
        Button addTextButton = createStyledButton("Add Text");
        Button colorButton = createStyledButton("Choose Color");

        // Brush selection controls
        Label brushLabel = new Label("Brush:");
        brushLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        ComboBox<WhiteboardController.BrushType> brushComboBox = new ComboBox<>();
        brushComboBox.getItems().addAll(WhiteboardController.BrushType.values());
        brushComboBox.setValue(controller.getCurrentBrush());
        brushComboBox.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #ccc;");

        // Brush size controls
        Label sizeLabel = new Label("Size:");
        sizeLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Slider sizeSlider = new Slider(1, 20, controller.getBrushSize());
        sizeSlider.setShowTickLabels(true);
        sizeSlider.setShowTickMarks(true);
        sizeSlider.setMajorTickUnit(5);
        sizeSlider.setMinorTickCount(1);
        sizeSlider.setStyle("-fx-control-inner-background: #f8f8f8;");

        // Set button actions
        clearButton.setOnAction(e -> handleClear());
        loadImageButton.setOnAction(e -> controller.loadImage(stage));
        saveButton.setOnAction(e -> handleSave());
        loadMediaButton.setOnAction(e -> handleLoadMedia());
        addTextButton.setOnAction(e -> handleAddText());
        colorButton.setOnAction(e -> handleChooseColor());

        // Brush selection action
        brushComboBox.setOnAction(e ->
                controller.setCurrentBrush(brushComboBox.getValue()));

        // Brush size adjustment action
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                controller.setBrushSize(newVal.doubleValue()));

        // Layout for buttons and brush controls
        HBox buttonBox = new HBox(10,
                loadImageButton, loadMediaButton, addTextButton,
                clearButton, saveButton, colorButton,
                brushLabel, brushComboBox,
                sizeLabel, sizeSlider
        );
        buttonBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");

        // StackPane to hold canvas & media elements
        board = new StackPane();
        board.getChildren().add(canvas);
        board.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1;");

        // Set cursor for canvas
        canvas.setOnMouseEntered(e -> canvas.setCursor(javafx.scene.Cursor.HAND));
        canvas.setOnMouseExited(e -> canvas.setCursor(javafx.scene.Cursor.DEFAULT));

        // Mouse event handlers for drawing
        canvas.setOnMousePressed(e ->
                controller.handleMousePressed(e.getX(), e.getY()));
        canvas.setOnMouseDragged(e ->
                controller.handleMouseDragged(e.getX(), e.getY()));

        // Main layout
        BorderPane root = new BorderPane();
        root.setCenter(board);
        root.setTop(buttonBox);
        root.setStyle("-fx-background-color: #f9f9f9;");

        stage.setTitle("Whiteboard App with Brush Selection");
        stage.setScene(new Scene(root, 900, 650));
        stage.show();
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: #4a6baf;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 15;" +
                        "-fx-background-radius: 4;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 0);"
        );

        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #3a5a9f;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 15;" +
                        "-fx-background-radius: 4;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 3, 0, 0, 0);"
        ));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: #4a6baf;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 15;" +
                        "-fx-background-radius: 4;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 0);"
        ));

        return button;
    }

    private void handleClear() {
        List<String> options = Arrays.asList("Canvas", "Media", "Text", "Everything");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Everything", options);
        dialog.setTitle("Clear Board");
        dialog.setHeaderText("Choose what to clear:");
        dialog.setContentText("Options:");
        dialog.getDialogPane().setStyle("-fx-background-color: #f8f8f8;");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(option -> {
            boolean somethingCleared = false;

            switch (option) {
                case "Canvas":
                    controller.clearCanvas();
                    somethingCleared = true;
                    break;
                case "Media":
                    if (controller.hasMedia()) {
                        controller.clearMedia();
                        board.getChildren().removeIf(node -> node instanceof MediaView);
                        somethingCleared = true;
                    }
                    break;
                case "Text":
                    if (controller.hasText()) {
                        controller.clearText();
                        board.getChildren().removeIf(node -> node instanceof Text);
                        somethingCleared = true;
                    }
                    break;
                case "Everything":
                    boolean hadContent = false;

                    if (!controller.isCanvasEmpty()) {
                        controller.clearCanvas();
                        hadContent = true;
                    }
                    if (controller.hasMedia()) {
                        controller.clearMedia();
                        board.getChildren().removeIf(node -> node instanceof MediaView);
                        hadContent = true;
                    }
                    if (controller.hasText()) {
                        controller.clearText();
                        board.getChildren().removeIf(node -> node instanceof Text);
                        hadContent = true;
                    }

                    somethingCleared = hadContent;
                    break;
            }

            showAlert(somethingCleared ?
                            "Success" : "Info",
                    somethingCleared ?
                            "Content cleared successfully!" : "There was nothing to clear.",
                    Alert.AlertType.INFORMATION);
        });
    }

    private void handleSave() {
        if (controller.isCanvasEmpty() && !controller.hasMedia() && !controller.hasText()) {
            showAlert("Info", "There is nothing to save.", Alert.AlertType.INFORMATION);
            return;
        }

        try {
            controller.saveCanvas(stage);
            showAlert("Success", "Content saved successfully!", Alert.AlertType.INFORMATION);
        } catch (IOException ex) {
            showAlert("Error", "Failed to save image: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleLoadMedia() {
        Optional<MediaView> mediaView = controller.loadMedia(stage);
        mediaView.ifPresent(view -> {
            view.setX(50);
            view.setY(10);
            board.getChildren().add(view);
        });
    }

    private void handleAddText() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Text");
        dialog.setHeaderText("Enter text to add to the board:");
        dialog.setContentText("Text:");
        dialog.getDialogPane().setStyle("-fx-background-color: #f8f8f8;");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(text -> {
            Text textNode = controller.createTextNode(text);
            board.getChildren().add(textNode);
        });
    }

    private void handleChooseColor() {
        ColorPicker colorPicker = new ColorPicker(controller.getCurrentColor());
        colorPicker.setStyle("-fx-background-color: #f8f8f8;");

        Dialog<Color> dialog = new Dialog<>();
        dialog.setTitle("Choose Color");
        dialog.getDialogPane().setContent(colorPicker);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(button -> button == ButtonType.OK ? colorPicker.getValue() : null);
        dialog.getDialogPane().setStyle("-fx-background-color: #f8f8f8;");

        Optional<Color> result = dialog.showAndWait();
        result.ifPresent(controller::setCurrentColor);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #f8f8f8;");
        alert.showAndWait();
    }
}