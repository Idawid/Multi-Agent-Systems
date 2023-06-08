package simulationUtils.assignmentStrategies.warehouse;

import agents.WarehouseAgent;
import jade.core.AID;
import simulationUtils.Task;

import java.util.List;

public interface WarehouseAssignmentStrategy {
    AID assignWarehouseAgent(Task task, List<WarehouseAgent> warehouses);
}

