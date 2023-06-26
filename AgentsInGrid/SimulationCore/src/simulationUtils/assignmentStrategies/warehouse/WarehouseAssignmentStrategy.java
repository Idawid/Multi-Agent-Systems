package simulationUtils.assignmentStrategies.warehouse;

import agents.WarehouseAgent;
import jade.core.AID;
import simulationUtils.Order;

import java.util.List;

public interface WarehouseAssignmentStrategy {
    AID assignWarehouseAgent(Order order, List<WarehouseAgent> warehouses);
}

