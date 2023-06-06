package simulation;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import utils.*;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class LocationMapVisualizer extends Application implements LocationMapObserver, Serializable {

    private static final int MAP_WIDTH = MapConfig.MAP_BOUND_X;
    private static final int MAP_HEIGHT = MapConfig.MAP_BOUND_Y;
    private static final int PIN_RADIUS = 5;
    private static final int GRID_SIZE = 20;

    private Map<String, LocationPin> locationPins = new HashMap<>();
    private Pane root;
    private LocationMap locationMap;


    @Override
    public void start(Stage primaryStage) {
        try {
            locationMap = (LocationMap) Naming.lookup("rmi://localhost/locationMap");
            UnicastRemoteObject.exportObject(locationMap, 0);
            LocationMapObserverProxy proxy = new LocationMapObserverProxy(this);
            locationMap.registerObserver(proxy);
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
        removeAllCircles();
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

    private void removeAllCircles() {
        if (root == null) {
            return;
        }
        root.getChildren().removeIf(Circle.class::isInstance);
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
