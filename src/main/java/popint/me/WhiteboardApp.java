package popint.me;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WhiteboardApp extends Application {

    private double lastX = -1, lastY = -1;
    private Canvas canvas;
    private GraphicsContext gc;
    private StackPane board;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Text textNode;
    private Color currentColor = Color.BLACK;

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        gc.setStroke(currentColor);
        gc.setLineWidth(2);

        // Create Buttons
        Button clearButton = new Button("Clear");
        Button loadImageButton = new Button("Load Image");
        Button saveButton = new Button("Save");
        Button loadMediaButton = new Button("Load Media");
        Button addTextButton = new Button("Add Text");
        Button colorButton = new Button("Choose Color");

        // Assign Actions
        clearButton.setOnAction(e -> chooseClearOption());
        loadImageButton.setOnAction(e -> loadImage(stage));
        saveButton.setOnAction(e -> saveCanvas(stage));
        loadMediaButton.setOnAction(e -> loadMedia(stage));
        addTextButton.setOnAction(e -> addText());
        colorButton.setOnAction(e -> chooseColor());

        // Layout for buttons
        HBox buttonBox = new HBox(10, loadImageButton, loadMediaButton, addTextButton, clearButton, saveButton, colorButton);

        // StackPane to hold canvas & media elements
        board = new StackPane();
        board.getChildren().add(canvas);

        // Set hand cursor for canvas
        canvas.setOnMouseEntered(e -> canvas.setCursor(javafx.scene.Cursor.HAND));
        canvas.setOnMouseExited(e -> canvas.setCursor(javafx.scene.Cursor.DEFAULT));

        canvas.setOnMousePressed(e -> {
            lastX = e.getX();
            lastY = e.getY();
        });

        canvas.setOnMouseDragged(e -> {
            double currentX = e.getX(), currentY = e.getY();
            if (lastX != -1 && lastY != -1) {
                gc.setStroke(currentColor);
                gc.strokeLine(lastX, lastY, currentX, currentY);
            }
            lastX = currentX;
            lastY = currentY;
        });

        // Set hand cursor for media
        board.setOnMouseEntered(e -> {
            if (mediaView != null) {
                mediaView.setCursor(javafx.scene.Cursor.HAND);
            }
        });
        board.setOnMouseExited(e -> {
            if (mediaView != null) {
                mediaView.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });

        // Main layout
        BorderPane root = new BorderPane();
        root.setCenter(board);
        root.setTop(buttonBox);

        stage.setTitle("JavaFX Whiteboard");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    private void chooseClearOption() {
        List<String> options = Arrays.asList("Canvas", "Media", "Text", "Everything");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Everything", options);
        dialog.setTitle("Clear Board");
        dialog.setHeaderText("Choose what to clear:");
        dialog.setContentText("Options:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::clearBoard);
    }

    private void clearBoard(String option) {
        switch (option) {
            case "Canvas":
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                break;
            case "Media":
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    board.getChildren().remove(mediaView);
                    mediaPlayer = null;
                    mediaView = null;
                }
                break;
            case "Text":
                if (textNode != null) {
                    board.getChildren().remove(textNode);
                    textNode = null;
                }
                break;
            case "Everything":
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                board.getChildren().clear();
                board.getChildren().add(canvas);
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer = null;
                    mediaView = null;
                }
                textNode = null;
                break;
        }
    }

    private void chooseColor() {
        ColorPicker colorPicker = new ColorPicker(currentColor);
        Dialog<Color> dialog = new Dialog<>();
        dialog.setTitle("Choose Color");
        dialog.getDialogPane().setContent(colorPicker);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(button -> button == ButtonType.OK ? colorPicker.getValue() : null);
        Optional<Color> result = dialog.showAndWait();
        result.ifPresent(color -> currentColor = color);
    }

    private void saveCanvas(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        fileChooser.setInitialFileName("whiteboard_image.png");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, writableImage);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
            } catch (IOException ex) {
                System.out.println("Error saving canvas: " + ex.getMessage());
            }
        }
    }

    private void loadImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            Image image = new Image(file.toURI().toString());
            gc.drawImage(image, 50, 50, 200, 200);
        }
    }

    private void loadMedia(Stage stage) {
        // Open file chooser to load media (video/audio)
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Media Files", "*.mp4", "*.mp3"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            String mediaUrl = file.toURI().toString();
            Media media = new Media(mediaUrl);
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView(mediaPlayer);

            // Resize video
            mediaView.setFitWidth(200);
            mediaView.setFitHeight(150);

            // Center video
            mediaView.setX(50);
            mediaView.setY(10); // Position vertically

            // Set hand cursor for video
            mediaView.setCursor(javafx.scene.Cursor.HAND);

            board.getChildren().add(mediaView);
            mediaPlayer.play();
        }
    }

    private void addText() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Text");
        dialog.setHeaderText("Enter text to add to the board:");
        dialog.setContentText("Text:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(text -> {
            textNode = new Text(text);
            textNode.setFill(currentColor);
            board.getChildren().add(textNode);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
