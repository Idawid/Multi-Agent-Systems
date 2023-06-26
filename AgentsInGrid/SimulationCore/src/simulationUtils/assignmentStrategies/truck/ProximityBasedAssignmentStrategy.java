package simulationUtils.assignmentStrategies.truck;

import agents.TruckAgent;
import jade.core.AID;
import mapUtils.locationPin.Location;
import simulationUtils.Order;

import java.io.Serializable;
import java.util.List;

public class ProximityBasedAssignmentStrategy implements TruckAssignmentStrategy, Serializable {
    // TODO [3]
    //  (we use random strategy anyway)
    //  - trucks moves this has to be implemented differently (check?)
    @Override
    public AID assignTruckAgent(Order order, List<TruckAgent> trucks) {
        if (trucks == null || trucks.isEmpty()) {
            return null; // No available trucks
        }

        AID assignedTruck = null;
        double shortestDistance = Double.MAX_VALUE;
        Location deliveryLocation = order.getDestination();

        for (TruckAgent truck : trucks) {
            double distance = truck.getLocationPin().getDistance(deliveryLocation);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                assignedTruck = truck.getAID();
            }
        }

        return assignedTruck;
    }
}
