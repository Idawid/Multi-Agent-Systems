package visualization;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import mapUtils.*;
import mapUtils.locationPin.LocationPin;
import visualizationUtils.IconContainerBuilder;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Map;

public class LocationMapVisualizer extends Application implements LocationMapObserver {
    private static final int MAP_WIDTH = MapConfig.MAP_BOUND_X;
    private static final int MAP_HEIGHT = MapConfig.MAP_BOUND_Y;
    private static final int GRID_SIZE = 20;

    private Pane root;

    @Override
    public void start(Stage primaryStage) {
        registerWithLocationMap();

        root = new Pane();

        Canvas canvas = new Canvas(MAP_WIDTH, MAP_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawGrid(gc);

        root.getChildren().add(canvas);

        Scene scene = new Scene(root, MAP_WIDTH, MAP_HEIGHT);
        scene.setFill(Color.LIGHTGRAY);

        primaryStage.setTitle("Location Map Visualizer");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MAP_WIDTH);
        primaryStage.setMaxWidth(MAP_WIDTH);
        primaryStage.setMinHeight(MAP_HEIGHT);
        primaryStage.setMaxHeight(MAP_HEIGHT);

        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            deregisterFromLocationMap();
            Platform.exit();
            System.exit(0);
        });

        initLocationPins();
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

    public void initLocationPins() {
        if (root == null) {
            return;
        }

        removeAllIcons();
        try {
            LocationMap locationMap = (LocationMap) Naming.lookup(LocationMap.REMOTE_LOCATION_MAP_ENDPOINT);
            for (Map.Entry<String, LocationPin> entry : locationMap.getLocationPins().entrySet()) {
                String pinName = entry.getKey();
                LocationPin pin = entry.getValue();
                addIcon(pinName, pin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addIcon(String name, LocationPin pin) {
        if (root == null) {
            return;
        }

        VBox iconContainer = IconContainerBuilder.createLocationPinView.createLocationPinView(pin, name);
        root.getChildren().add(iconContainer);
    }

    private void removeIcon(String pinName) {
        if (root == null) {
            return;
        }

        root.getChildren().removeIf(node -> {
            Object pinId = node.getProperties().get("pinId");
            return pinId instanceof String && pinId.equals(pinName);
        });
    }

    private void removeAllIcons() {
        if (root == null) {
            return;
        }

        root.getChildren().removeIf(VBox.class::isInstance);
    }

    public static void main(String[] args) {
        Application.launch(LocationMapVisualizer.class);
    }

    private void registerWithLocationMap() {
        try {
            LocationMap locationMap = (LocationMap) Naming.lookup(LocationMap.REMOTE_LOCATION_MAP_ENDPOINT);
            locationMap.registerObserver(new LocationMapObserverProxy(this));
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }
    private void deregisterFromLocationMap() {
        try {
            LocationMap locationMap = (LocationMap) Naming.lookup(LocationMap.REMOTE_LOCATION_MAP_ENDPOINT);
            locationMap.unregisterObserver(new LocationMapObserverProxy(this));
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    @Override
    public void locationUpdated(String agentName, LocationPin newLocationPin) throws RemoteException {
        Platform.runLater(() -> {
            removeIcon(agentName);
            if (newLocationPin != null) {
                addIcon(agentName, newLocationPin);
            }
        });
    }

    @Override
    public String getUniqueId() {
        return this.getClass().getSimpleName();
    }
}
