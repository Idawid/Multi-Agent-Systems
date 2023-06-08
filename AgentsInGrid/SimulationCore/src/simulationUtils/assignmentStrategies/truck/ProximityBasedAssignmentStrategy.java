package simulationUtils.assignmentStrategies.truck;

import agents.TruckAgent;
import jade.core.AID;
import mapUtils.Location;
import simulationUtils.Task;

import java.io.Serializable;
import java.util.List;

public class ProximityBasedAssignmentStrategy implements TruckAssignmentStrategy, Serializable {
    // TODO trucks moves this has to be implemented differently
    @Override
    public AID assignTruckAgent(Task task, List<TruckAgent> trucks) {
        if (trucks == null || trucks.isEmpty()) {
            return null; // No available trucks
        }

        AID assignedTruck = null;
        double shortestDistance = Double.MAX_VALUE;
        Location deliveryLocation = task.getDestination();

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
