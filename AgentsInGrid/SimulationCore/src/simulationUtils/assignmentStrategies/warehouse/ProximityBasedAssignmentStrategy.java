package simulationUtils.assignmentStrategies.warehouse;

import agents.WarehouseAgent;
import jade.core.AID;
import mapUtils.locationPin.Location;
import simulationUtils.Order;

import java.io.Serializable;
import java.util.List;

public class ProximityBasedAssignmentStrategy implements WarehouseAssignmentStrategy, Serializable {
    @Override
    public AID assignWarehouseAgent(Order order, List<WarehouseAgent> warehouses) {
        if (warehouses == null || warehouses.isEmpty()) {
            return null; // No available warehouses
        }

        AID assignedWarehouse = null;
        double shortestDistance = Double.MAX_VALUE;
        Location deliveryLocation = order.getDestination();

        for (WarehouseAgent warehouse : warehouses) {
            double distance = warehouse.getLocationPin().getDistance(deliveryLocation);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                assignedWarehouse = warehouse.getAID();
            }
        }

        return assignedWarehouse;
    }
}
