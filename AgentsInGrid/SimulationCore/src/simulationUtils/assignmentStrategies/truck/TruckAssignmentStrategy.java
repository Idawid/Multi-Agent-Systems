package simulationUtils.assignmentStrategies.truck;

import agents.TruckAgent;
import jade.core.AID;
import simulationUtils.Order;

import java.util.List;

public interface TruckAssignmentStrategy {
    AID assignTruckAgent(Order order, List<TruckAgent> warehouses);
}