package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import mapUtils.AgentType;
import mapUtils.AgentTypeProvider;
import mapUtils.Location;
import simulationUtils.Constants;
import simulationUtils.Task;
import simulationUtils.TaskAllocator;

import java.util.ArrayList;
import java.util.List;

public class WarehouseAgent extends BaseAgent implements AgentTypeProvider {
    // TODO handle delivery Tasks from MainHub:
    //  - assign received tasks further to TruckAgent's

    // TODO handle multiple delivery Tasks
    //  - round robin strategy to assign them to TruckAgent's
    private List<Task> tasks;

    public WarehouseAgent(Location location) {
        super(location);
    }

    public WarehouseAgent() { }

    protected void setup() {
        super.setup();

        this.tasks = new ArrayList<>();

        addBehaviour(new ReceiveDeliveryRequestBehaviour());
        addBehaviour(new AssignTrucksBehavior(this, 1000));
    }

    private class ReceiveDeliveryRequestBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchConversationId(Constants.MSG_ID_DELIVERY_INFORM);
            ACLMessage deliveryRequest = receive(mt);

            if (deliveryRequest != null) {
                try {
                    Task task = (Task) deliveryRequest.getContentObject();
                    tasks.add(task);
                } catch (UnreadableException e) {
                    System.out.println("Failed to extract Task object from the received message.");
                }
            } else {
                block();
            }
        }
    }

    private class AssignTrucksBehavior extends TickerBehaviour {

        public AssignTrucksBehavior(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            assignTasks();
        }
    }

    private void assignTasks() {
//        if (!tasks.isEmpty() && !trucks.isEmpty()) {
//            TaskAllocator.assignTasksRoundRobin(tasks, trucks);
//            tasks.clear();
//        }
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.AGENT_WAREHOUSE;
    }
}
