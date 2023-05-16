package utils;

import java.util.*;

public class LocationMap {
    private static int mapBoundX;
    private static int mapBoundY;
    private static Map<String, LocationPin> locationPins;

    static {
        locationPins = new HashMap<>();
    }
    public LocationMap(int mapBoundX, int mapBoundY) {
        this.mapBoundX = mapBoundX;
        this.mapBoundY = mapBoundY;
        locationPins = new HashMap<>();
    }

    public int getMapBoundX() {
        return mapBoundX;
    }

    public int getMapBoundY() {
        return mapBoundY;
    }

    public static void addLocationPin(String agentName, LocationPin pin) {
        locationPins.put(agentName, pin);
    }

    public static void removeLocationPin(String agentName) {
        locationPins.remove(agentName);
    }

    public static void updateLocationPin(String agentName, LocationPin pin) {
        // Map.put() also updates the record
        locationPins.put(agentName, pin);
    }

    public static LocationPin getLocationPin(String agentName) {
        return locationPins.get(agentName);
    }
}

