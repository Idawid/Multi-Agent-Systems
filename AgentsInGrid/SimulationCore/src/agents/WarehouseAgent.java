package agents;

import jade.core.AID;
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
import simulationUtils.assignmentStrategies.truck.RandomAssignmentStrategy;
import simulationUtils.assignmentStrategies.truck.TruckAssignmentStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WarehouseAgent extends BaseAgent implements AgentTypeProvider {
    // TODO handle delivery Tasks from MainHub:
    //  - assign received tasks further to TruckAgent's

    // TODO handle multiple delivery Tasks
    //  - round robin strategy to assign them to TruckAgent's
    private TruckAssignmentStrategy assignmentStrategy;
    private List<Task> tasks;

    public WarehouseAgent(Location location) {
        super(location);
    }

    public WarehouseAgent() { }

    protected void setup() {
        super.setup();

        this.tasks = new ArrayList<>();
        assignmentStrategy = new RandomAssignmentStrategy();

        addBehaviour(new ReceiveDeliveryInformBehaviour());
        addBehaviour(new AssignTrucksBehavior(this, 1000));
    }

    private class ReceiveDeliveryInformBehaviour extends CyclicBehaviour {
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
            if (!tasks.isEmpty()) {
                Task task = tasks.remove(0);

                List<TruckAgent> trucks = (List<TruckAgent>) findAgentsByClass(TruckAgent.class);
                if (trucks != null && !trucks.isEmpty()) {
                    trucks.removeIf(truckAgent -> !truckAgent.getContainerID().equals(((WarehouseAgent) myAgent).getContainerID()));
                    trucks.removeIf(truckAgent -> truckAgent.getCurrentTask() != null);
                    AID truckAID = assignmentStrategy.assignTruckAgent(task, trucks);

                    ACLMessage deliveryInstruction = new ACLMessage(ACLMessage.INFORM);
                    deliveryInstruction.setConversationId(Constants.MSG_ID_DELIVERY_INFORM);
                    deliveryInstruction.addReceiver(truckAID);

                    try {
                        deliveryInstruction.setContentObject(task);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    send(deliveryInstruction);
                }
            }
        }
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.AGENT_WAREHOUSE;
    }
}
