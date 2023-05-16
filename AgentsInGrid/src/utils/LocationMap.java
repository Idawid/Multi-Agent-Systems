package utils;

import java.util.ArrayList;
import java.util.List;

public class LocationMap {
    private static List<Location> locations;

    public LocationMap() {
        this.locations = new ArrayList<>();
    }

    public void addLocation(Location location) {
        locations.add(location);
    }

    public void removeLocation(Location location) {
        locations.remove(location);
    }

    public List<Location> getLocations() {
        return locations;
    }

    public static double getDistanceBetweenLocations(Location location1, Location location2) {
        if (!locations.contains(location1) || !locations.contains(location2)) {
            throw new IllegalArgumentException("One or both locations are not on the map.");
        }

        return location1.getDistance(location2);
    }
}

