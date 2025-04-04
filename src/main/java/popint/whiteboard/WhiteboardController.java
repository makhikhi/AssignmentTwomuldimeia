package popint.whiteboard;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
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
import java.util.Optional;

public class WhiteboardController {
    // Drawing state
    private double lastX = -1, lastY = -1;
    private GraphicsContext gc;
    private Canvas canvas;

    // Media state
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;

    // Text state
    private Text textNode;

    // Current settings
    private Color currentColor = Color.BLACK;
    private double brushSize = 2.0;
    private BrushType currentBrush = BrushType.PEN;

    public enum BrushType {
        PEN, MARKER, ERASER
    }

    public void initializeDrawingContext(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        updateBrushSettings();
    }

    public void updateBrushSettings() {
        switch (currentBrush) {
            case PEN:
                gc.setStroke(currentColor);
                gc.setLineWidth(brushSize);
                gc.setGlobalAlpha(1.0);
                break;
            case MARKER:
                gc.setStroke(currentColor);
                gc.setLineWidth(brushSize * 1.5);
                gc.setGlobalAlpha(0.5);
                break;
            case ERASER:
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(brushSize * 2);
                gc.setGlobalAlpha(1.0);
                break;
        }
    }

    public void handleMousePressed(double x, double y) {
        lastX = x;
        lastY = y;
    }

    public void handleMouseDragged(double x, double y) {
        if (lastX != -1 && lastY != -1) {
            drawBetweenPoints(lastX, lastY, x, y);
        }
        lastX = x;
        lastY = y;
    }

    private void drawBetweenPoints(double x1, double y1, double x2, double y2) {
        gc.strokeLine(x1, y1, x2, y2);
    }

    public void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void clearMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
            mediaView = null;
        }
    }

    public void clearText() {
        textNode = null;
    }

    public boolean isCanvasEmpty() {
        WritableImage snapshot = new WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());
        canvas.snapshot(null, snapshot);

        for (int x = 0; x < snapshot.getWidth(); x++) {
            for (int y = 0; y < snapshot.getHeight(); y++) {
                if (snapshot.getPixelReader().getColor(x, y).getOpacity() > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
        updateBrushSettings();
    }

    public void setBrushSize(double size) {
        this.brushSize = size;
        updateBrushSettings();
    }

    public void setCurrentBrush(BrushType brushType) {
        this.currentBrush = brushType;
        updateBrushSettings();
    }

    public void saveCanvas(Stage stage) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("JPG Files", "*.jpg")
        );
        fileChooser.setInitialFileName("whiteboard_image.png");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, writableImage);
            String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), extension, file);
        }
    }

    public void loadImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            Image image = new Image(file.toURI().toString());
            gc.drawImage(image, 50, 50, image.getWidth()/2, image.getHeight()/2);
        }
    }

    public Optional<MediaView> loadMedia(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Media Files", "*.mp4", "*.mp3"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            String mediaUrl = file.toURI().toString();
            Media media = new Media(mediaUrl);
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView(mediaPlayer);
            mediaView.setFitWidth(300);
            mediaView.setFitHeight(200);
            mediaPlayer.play();
            return Optional.of(mediaView);
        }
        return Optional.empty();
    }

    public Text createTextNode(String text) {
        textNode = new Text(text);
        textNode.setFill(currentColor);
        textNode.setFont(javafx.scene.text.Font.font("Arial", 24));
        return textNode;
    }

    // Getters for UI to access state
    public boolean hasMedia() {
        return mediaPlayer != null;
    }

    public boolean hasText() {
        return textNode != null;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public double getBrushSize() {
        return brushSize;
    }

    public BrushType getCurrentBrush() {
        return currentBrush;
    }
}