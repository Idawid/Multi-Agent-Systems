package simulationUtils.assignmentStrategies.truck;

import agents.TruckAgent;
import jade.core.AID;
import simulationUtils.Task;

import java.util.List;

public interface TruckAssignmentStrategy {
    AID assignTruckAgent(Task task, List<TruckAgent> warehouses);
}