package agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import utils.AgentType;
import utils.AgentTypeProvider;
import utils.Location;
import simulationUtils.Task;
import simulationUtils.TaskAllocator;

import java.util.ArrayList;
import java.util.List;

public class WarehouseAgent extends BaseAgent implements AgentTypeProvider {
    private List<TruckAgent> trucks;
    private List<Task> tasks;

    public WarehouseAgent(Location location, List<TruckAgent> trucks) {
        super(location);
        this.trucks = trucks ;
    }

    public WarehouseAgent() { }

    protected void setup() {
        super.setup();

        this.trucks = new ArrayList<>();
        this.tasks = new ArrayList<>();

        addBehaviour(new TickerBehaviour(this, 1000) {
            protected void onTick() {
                assignTasks();
            }
        });
    }

    private void assignTasks() {
        if (!tasks.isEmpty() && !trucks.isEmpty()) {
            TaskAllocator.assignTasksRoundRobin(tasks, trucks);
            tasks.clear();
        }
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void addTruck(TruckAgent truck) {
        trucks.add(truck);
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.AGENT_WAREHOUSE;
    }
}
