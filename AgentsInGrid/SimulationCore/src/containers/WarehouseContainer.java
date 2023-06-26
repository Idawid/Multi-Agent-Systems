package containers;
import agents.TruckAgent;
import agents.WarehouseAgent;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import simulationUtils.Constants;
import mapUtils.locationPin.Location;

import java.util.ArrayList;
import java.util.List;

public class WarehouseContainer {
    private static int idWarehouseCounter = 0;
    private static int idTruckCounter = 0;
    private ContainerController warehouseContainer;
    public WarehouseContainer(String containerName, int containerId, List<Location> warehouseLocations, List<Location> truckLocations) {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.MAIN_PORT, "1098");
        p.setParameter(Profile.CONTAINER_NAME, containerName + containerId);
        warehouseContainer = rt.createAgentContainer(p);

        // Trucks
        List<TruckAgent> truckAgentList = new ArrayList<>();
        for (Location location : truckLocations) { // Trucks rest in the warehouses
            TruckAgent truckAgent = new TruckAgent(location);
            truckAgentList.add(truckAgent);
            addAgent(Constants.AGENT_TRUCK_PREFIX + idTruckCounter, truckAgent);
            idTruckCounter++;
        }

        // Warehouses
        for (Location location : warehouseLocations) {
            WarehouseAgent warehouseAgent = new WarehouseAgent(location);
            addAgent(Constants.AGENT_WAREHOUSE_PREFIX + idWarehouseCounter, warehouseAgent);
            idWarehouseCounter++;
        }
    }

    public void addAgent(String agentName, Agent agent) {
        try {
            AgentController agentController = warehouseContainer.acceptNewAgent(agentName, agent);
            agentController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
