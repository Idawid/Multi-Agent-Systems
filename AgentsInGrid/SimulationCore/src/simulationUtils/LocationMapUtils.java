package simulationUtils;

import mapUtils.LocationMap;
import mapUtils.locationPin.LocationPin;

import java.rmi.Naming;
import java.util.concurrent.CompletableFuture;

public class LocationMapUtils {
    public static void updateLocationPinNonBlocking(String agentName, LocationPin location) {
        CompletableFuture.runAsync(() -> {
            try {
                LocationMap locationMap = (LocationMap) Naming.lookup(LocationMap.REMOTE_LOCATION_MAP_ENDPOINT);
                locationMap.updateLocationPin(agentName, location);
            } catch (Exception e) {}
        });
    }

    public static LocationPin getLocationPinBlocking(String agentName) {
        try {
            LocationMap locationMap = (LocationMap) Naming.lookup(LocationMap.REMOTE_LOCATION_MAP_ENDPOINT);
            return locationMap.getLocationPin(agentName);
        } catch (Exception e) {
            return null;
        }
    }
}
