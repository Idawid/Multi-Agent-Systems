package agents;

import jade.core.behaviours.TickerBehaviour;
import mapUtils.AgentType;
import mapUtils.AgentTypeProvider;
import mapUtils.Location;
import simulationUtils.Task;
import simulationUtils.TaskAllocator;

import java.util.ArrayList;
import java.util.List;

public class WarehouseAgent extends BaseAgent implements AgentTypeProvider {
    // TODO handle delivery Tasks from MainHub:
    //  - match msg of id Constants.MSG_ID_DELIVERY_INFORM
    //  - assign received tasks further to TruckAgent's

    // TODO handle multiple delivery Tasks
    //  - round robin strategy to assign them to TruckAgent's
    private List<TruckAgent> trucks;
    private List<Task> tasks;

    public WarehouseAgent(Location location, List<TruckAgent> trucks) {
        super(location);
        this.trucks = trucks;
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
