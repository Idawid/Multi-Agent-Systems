package simulation;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import utils.Constants;
import utils.LocationPin;

import java.util.Map;

public class LocationMapVisualizer extends Application {
    private static final int MAP_WIDTH = Constants.MAP_BOUND_X;
    private static final int MAP_HEIGHT = Constants.MAP_BOUND_Y;
    private static final int PIN_RADIUS = 5;
    private static final int GRID_SIZE = 20;
    private Pane root;

    private static ObservableMap<String, LocationPin> locationPins;

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();

        Canvas canvas = new Canvas(MAP_WIDTH, MAP_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawGrid(gc);

        root.getChildren().add(canvas);

        Scene scene = new Scene(root, MAP_WIDTH, MAP_HEIGHT);
        scene.setFill(Color.LIGHTGRAY);

        primaryStage.setTitle("Location Map Visualizer");
        primaryStage.setScene(sce   ne);
        primaryStage.setMinWidth(MAP_WIDTH);
        primaryStage.setMaxWidth(MAP_WIDTH);
        primaryStage.setMinHeight(MAP_HEIGHT);
        primaryStage.setMaxHeight(MAP_HEIGHT);

        primaryStage.show();

        refreshVisualization();
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.5);

        for (int x = 0; x <= MAP_WIDTH; x += GRID_SIZE) {
            gc.strokeLine(x, 0, x, MAP_HEIGHT);
        }

        for (int y = 0; y <= MAP_HEIGHT; y += GRID_SIZE) {
            gc.strokeLine(0, y, MAP_WIDTH, y);
        }
    }

    public void initLocationPins(Map<String, LocationPin> locationPins) {
        this.locationPins = FXCollections.observableMap(locationPins);
        this.locationPins.addListener((MapChangeListener<String, LocationPin>) change -> {
            if (change.wasAdded()) {
                addPin(change.getValueAdded());
            } else if (change.wasRemoved()) {
                removePin(change.getValueRemoved());
            }
        });
        refreshVisualization();
    }

    private void refreshVisualization() {
        if(root == null) {
            return;
        }
        //root.getChildren().clear();
        for (LocationPin pin : locationPins.values()) {
            addPin(pin);
        }
    }

    private void addPin(LocationPin pin) {
        if(root == null) {
            return;
        }
        Circle pinShape = new Circle(pin.getX(), pin.getY(), PIN_RADIUS);
        root.getChildren().add(pinShape);
    }

    private void removePin(LocationPin pin) {
        if(root == null) {
            return;
        }
        root.getChildren().removeIf(node ->
                node instanceof Circle && ((Circle) node).getCenterX() == pin.getX() &&
                        ((Circle) node).getCenterY() == pin.getY());
    }
    public static void startVisualization() {
        Thread javafxThread = new Thread(() -> Application.launch(LocationMapVisualizer.class));
        javafxThread.start();
    }

    public static ObservableMap<String, LocationPin> getLocationPins() {
        return locationPins;
    }

    public static void setLocationPins(ObservableMap<String, LocationPin> locationPins) {
        LocationMapVisualizer.locationPins = locationPins;
    }
}
