package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationMap {
    private int mapBoundX;
    private int mapBoundY;
    private volatile Map<String, LocationPin> locationPins;

    // Singleton instance
    private static LocationMap instance;
    // constructor is private to prevent direct instantiation
    private LocationMap() {
        this.mapBoundX = MapConfig.MAP_BOUND_X;
        this.mapBoundY = MapConfig.MAP_BOUND_Y;
        this.locationPins = new HashMap<>();
    }

    public static synchronized LocationMap getInstance() {
        if (instance == null) {
            instance = new LocationMap();
        }
        return instance;
    }

    public int getMapBoundX() {
        return mapBoundX;
    }

    public int getMapBoundY() {
        return mapBoundY;
    }

    public Map<String, LocationPin> getLocationPins() {
        return locationPins;
    }

    public LocationPin getLocationPin(String agentName) {
        return locationPins.get(agentName);
    }

    public synchronized void addLocationPin(String agentName, LocationPin pin) {
        locationPins.put(agentName, pin);
        // Notify observers about the change in locationPins
        notifyObservers();
    }

    public synchronized void removeLocationPin(String agentName) {
        locationPins.remove(agentName);
        // Notify observers about the change in locationPins
        notifyObservers();
    }

    public synchronized void updateLocationPin(String agentName, LocationPin pin) {
        // Map.put() updates the records too
        locationPins.put(agentName, pin);
        notifyObservers();
    }

    // Observer pattern implementation
    private List<Observer> observers = new ArrayList<>();

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this);
        }
    }

    // Observer interface
    public interface Observer {
        void update(LocationMap locationMap);
    }
}
