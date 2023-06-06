import containers.MainContainer;
import containers.RetailerContainer;
import containers.WarehouseContainer;
//import simulation.LocationMapVisualizer;
import simulationUtils.Constants;
import simulationUtils.LocationInitializer;
import utils.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoreEntry {
    public static void main(String[] args) {

        try {
            // RMI setup
            LocateRegistry.createRegistry(1099);
            LocationMap locationMap = new LocationMapImpl();
            UnicastRemoteObject.unexportObject(locationMap, true);
            LocationMap stub = (LocationMap) UnicastRemoteObject.exportObject(locationMap, 0);
            Naming.rebind("rmi://localhost/locationMap", stub);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Initialize the main container, it's always required
        containers.MainContainer mainContainer = new MainContainer(Constants.CONTAINER_MAIN);

        // Load the locations for different retailers
        List<Location> retailerLocations = LocationInitializer.generateRandomLocations(3, 0, MapConfig.MAP_BOUND_X, 0, MapConfig.MAP_BOUND_Y);
        RetailerContainer retailerContainer = new RetailerContainer(Constants.CONTAINER_RETAIL, retailerLocations);

        // Load the locations for different warehouses
        List<Location> warehouseLocations1 = LocationInitializer.generateRandomLocations(2, 0, MapConfig.MAP_BOUND_X, 0, MapConfig.MAP_BOUND_Y);
        List<Location> truckLocations1 = new ArrayList<>();
        truckLocations1.addAll(Collections.nCopies(2, warehouseLocations1.get(0))); // 2 in warehouse 0
        truckLocations1.addAll(Collections.nCopies(1, warehouseLocations1.get(1))); // 1 in warehouse 1

        WarehouseContainer warehouseContainer1 = new WarehouseContainer(Constants.CONTAINER_WAREHOUSE_PREFIX, 1, warehouseLocations1, truckLocations1);
    }
}
