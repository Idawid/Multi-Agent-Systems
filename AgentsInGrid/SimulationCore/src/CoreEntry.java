import containers.MainContainer;
import containers.RetailerContainer;
import containers.WarehouseContainer;
//import simulation.LocationMapVisualizer;
import simulationUtils.Constants;
import simulationUtils.LocationInitializer;
import simulationUtils.LocationMapObserver;
import utils.Location;
import utils.LocationMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoreEntry {
    public static void main(String[] args) {
        // Initialize the main container, it's always required
        containers.MainContainer mainContainer = new MainContainer(Constants.CONTAINER_MAIN);

        // Init the map
        LocationMapObserver map = new LocationMapObserver(LocationMap.getInstance());

        // Load the locations for different retailers
        List<Location> retailerLocations = LocationInitializer.generateRandomLocations(3, 0, 100, 0, 100);
        RetailerContainer retailerContainer = new RetailerContainer(Constants.CONTAINER_RETAIL, retailerLocations);

        // Load the locations for different warehouses
        List<Location> warehouseLocations1 = LocationInitializer.generateRandomLocations(2, 0, 100, 0, 100);
        List<Location> truckLocations1 = new ArrayList<>();
        truckLocations1.addAll(Collections.nCopies(2, warehouseLocations1.get(0)));
        truckLocations1.addAll(Collections.nCopies(1, warehouseLocations1.get(1)));

        LocationInitializer.generateRandomLocations(5, 0, 100, 0, 100);
        WarehouseContainer warehouseContainer1 = new WarehouseContainer(Constants.CONTAINER_WAREHOUSE_PREFIX, 1, warehouseLocations1, truckLocations1);
    }
}
