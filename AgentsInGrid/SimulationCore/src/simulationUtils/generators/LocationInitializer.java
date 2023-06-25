package simulationUtils.generators;

import mapUtils.locationPin.Location;
import mapUtils.MapConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LocationInitializer {
    public static Location generateRandomLocation() {
        return generateRandomLocations(1, (MapConfig.MAP_BOUND_X * 10)/100, (MapConfig.MAP_BOUND_X * 90)/100,
                (MapConfig.MAP_BOUND_Y * 10)/100, (MapConfig.MAP_BOUND_Y * 90)/100).get(0);
    }
    public static List<Location> generateRandomLocations(int numLocations) {
        return generateRandomLocations(numLocations, (MapConfig.MAP_BOUND_X * 10)/100, (MapConfig.MAP_BOUND_X * 90)/100,
                (MapConfig.MAP_BOUND_Y * 10)/100, (MapConfig.MAP_BOUND_Y * 90)/100);
    }
    public static List<Location> generateRandomLocations(int numLocations, int minX, int maxX, int minY, int maxY) {
        List<Location> locations = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < numLocations; i++) {
            int randomX = random.nextInt(maxX - minX + 1) + minX;
            int randomY = random.nextInt(maxY - minY + 1) + minY;
            Location location = new Location(randomX, randomY);
            locations.add(location);
        }

        return locations;
    }
}
