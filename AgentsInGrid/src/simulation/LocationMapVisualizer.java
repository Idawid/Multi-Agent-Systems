package simulation;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import utils.LocationPin;

import java.util.Map;

public class LocationMapVisualizer extends Application {
    private static final int MAP_WIDTH = 800;
    private static final int MAP_HEIGHT = 600;
    private static final int PIN_RADIUS = 5;

    private Pane root;

    private static ObservableMap<String, LocationPin> locationPins;

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();
        Scene scene = new Scene(root, MAP_WIDTH, MAP_HEIGHT);

        primaryStage.setTitle("Location Map Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
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
        root.getChildren().clear();
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
        launch();
    }

    public static ObservableMap<String, LocationPin> getLocationPins() {
        return locationPins;
    }

    public static void setLocationPins(ObservableMap<String, LocationPin> locationPins) {
        LocationMapVisualizer.locationPins = locationPins;
    }
}
