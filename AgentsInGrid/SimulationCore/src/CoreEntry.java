import agents.MainHub;
import containers.MainContainer;
import containers.RetailerContainer;
import containers.WarehouseContainer;
//import simulation.LocationMapVisualizer;
import simulationUtils.generators.LocationInitializer;
import mapUtils.*;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static simulationUtils.Constants.*;

public class CoreEntry {
    public static void main(String[] args) {

        try {
            // RMI setup
            LocateRegistry.createRegistry(1099);
            LocationMap locationMap = new LocationMapImpl();
            UnicastRemoteObject.unexportObject(locationMap, true);
            LocationMap stub = (LocationMap) UnicastRemoteObject.exportObject(locationMap, 0);
            Naming.rebind(LocationMap.REMOTE_LOCATION_MAP_ENDPOINT, stub);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Initialize the main container, required
        MainContainer mainContainer = new MainContainer(CONTAINER_MAIN);
        Location mainHubLocation = LocationInitializer.generateRandomLocation();
        mainContainer.addAgent(AGENT_MAIN_HUB_PREFIX + "0", new MainHub(mainHubLocation));

        // Load the locations for different retailers
        List<Location> retailerLocations = LocationInitializer.generateRandomLocations(5);
        RetailerContainer retailerContainer = new RetailerContainer(CONTAINER_RETAIL, retailerLocations);

        // Load the locations for different warehouses
        List<Location> warehouseLocations1 = LocationInitializer.generateRandomLocations(3);
        List<Location> truckLocations1 = new ArrayList<>();
        truckLocations1.addAll(Collections.nCopies(3, warehouseLocations1.get(0))); // 3 in warehouse 0
        //truckLocations1.addAll(Collections.nCopies(3, warehouseLocations1.get(1))); // 3 in warehouse 1
        //truckLocations1.addAll(Collections.nCopies(3, warehouseLocations1.get(2))); // 3 in warehouse 2
        //truckLocations1.addAll(Collections.nCopies(0, warehouseLocations1.get(1))); // 1 in warehouse 1

        WarehouseContainer warehouseContainer1 = new WarehouseContainer(CONTAINER_WAREHOUSE_PREFIX, 1, warehouseLocations1, truckLocations1);
    }
}
