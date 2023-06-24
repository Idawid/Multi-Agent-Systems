package simulation;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import mapUtils.*;

import java.io.Serializable;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class LocationMapVisualizer extends Application implements LocationMapObserver, Serializable {
    // TODO [1]: locationPin drawing
    //  - draw stock of Truck/Warehouse
    //  - draw profits of Retailer

    // TODO [2]: make X close the application
    //  - annoying.

    // TODO [2?] event type (add / delete)
    //  - don't sync every pin on the map, don't delete/add each children (just the ones changed)
    //  don't implement unless lags, but if it lags it has priority

    private static final int MAP_WIDTH = MapConfig.MAP_BOUND_X;
    private static final int MAP_HEIGHT = MapConfig.MAP_BOUND_Y;
    private static final int GRID_SIZE = 20;

    private Map<String, LocationPin> locationPins = new HashMap<>();
    private Pane root;
    private LocationMap locationMap;


    @Override
    public void start(Stage primaryStage) {
        try {
            locationMap = (LocationMap) Naming.lookup(LocationMap.REMOTE_LOCATION_MAP_ENDPOINT);
            UnicastRemoteObject.exportObject(locationMap, 0);
            locationMap.registerObserver(new LocationMapObserverProxy(this));
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
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
        try {
            this.locationPins = new HashMap<>(locationMap.getLocationPins());
            refreshVisualization();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void refreshVisualization() {
        if (root == null) {
            return;
        }
        removeAllIcons();
        for (LocationPin pin : locationPins.values()) {
            addIcon(pin);
        }
    }

    private void addIcon(LocationPin pin) {
        if (root == null) {
            return;
        }

        ImageView icon = new ImageView();
        // Set the appropriate image for the agent type based on the AgentType enum
        String resourcePath = Paths.get(System.getProperty("user.dir"),"Visualization", "resources").toString();
        switch (pin.getAgentType()) {
            case AGENT_RETAILER:
                icon.setImage(new Image(Paths.get(resourcePath, "retailer_icon.png").toString()));
                break;
            case AGENT_TRUCK:
                icon.setImage(new Image(Paths.get(resourcePath, "truck_icon.png").toString()));
                break;
            case AGENT_WAREHOUSE:
                icon.setImage(new Image(Paths.get(resourcePath, "warehouse_icon.png").toString()));
                break;
            case AGENT_MAIN_HUB:
                icon.setImage(new Image(Paths.get(resourcePath, "main_hub_icon.png").toString()));
                break;
            default:
                // Handle unknown agent types
                break;
        }
        icon.setPreserveRatio(true);
        icon.setFitWidth(50);

        icon.setLayoutX(pin.getX());
        icon.setLayoutY(pin.getY());
        root.getChildren().add(icon);
    }

    private void removeIcon(LocationPin pin) {
        if (root == null) {
            return;
        }

        root.getChildren().removeIf(node ->
                node instanceof ImageView && node.getLayoutX() == pin.getX() &&
                        node.getLayoutY() == pin.getY());
    }

    private void removeAllIcons() {
        if (root == null) {
            return;
        }

        root.getChildren().removeIf(ImageView.class::isInstance);
    }

    public static void main(String[] args) {
        Thread javafxThread = new Thread(() -> Application.launch(LocationMapVisualizer.class));
        javafxThread.start();
    }

    @Override
    public void locationUpdated(String agentName, LocationPin newLocationPin) throws RemoteException {
        this.locationPins = new HashMap<>(locationMap.getLocationPins());
        Platform.runLater(this::refreshVisualization);
    }
}
