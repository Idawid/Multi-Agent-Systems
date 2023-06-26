package simulationUtils.assignmentStrategies.truck;

import agents.TruckAgent;
import jade.core.AID;
import simulationUtils.Order;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

public class RandomAssignmentStrategy implements TruckAssignmentStrategy, Serializable {
    @Override
    public AID assignTruckAgent(Order order, List<TruckAgent> trucks) {
        if (trucks == null || trucks.isEmpty()) {
            return null; // No available trucks
        }

        Random random = new Random();
        int index = random.nextInt(trucks.size());
        return trucks.get(index).getAID();
    }
}
