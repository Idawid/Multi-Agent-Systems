package simulationUtils.assignmentStrategies.warehouse;

import agents.WarehouseAgent;
import jade.core.AID;
import mapUtils.Location;
import simulationUtils.Task;

import java.io.Serializable;
import java.util.List;

public class ProximityBasedAssignmentStrategy implements WarehouseAssignmentStrategy, Serializable {
    @Override
    public AID assignWarehouseAgent(Task task, List<WarehouseAgent> warehouses) {
        AID closestWarehouseAgent = null;

        try {
            Location deliveryLocation = task.getDestination();
            double minDistance = Double.MAX_VALUE;

            for (WarehouseAgent warehouse : warehouses) {

                double distance = warehouse.getLocationPin().getDistance(deliveryLocation);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestWarehouseAgent = warehouse.getAID();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return closestWarehouseAgent;
    }
}
