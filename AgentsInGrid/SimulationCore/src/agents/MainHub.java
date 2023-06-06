package agents;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mapUtils.AgentType;
import mapUtils.AgentTypeProvider;
import mapUtils.Location;
import simulationUtils.Constants;
import simulationUtils.Task;
import simulationUtils.allocators.ProximityBasedAssignmentStrategy;
import simulationUtils.allocators.WarehouseAssignmentStrategy;

import java.util.ArrayList;
import java.util.List;

public class MainHub extends BaseAgent implements AgentTypeProvider {
    private WarehouseAssignmentStrategy assignmentStrategy;

    public MainHub(Location location) {
        super(location);
    }
    public MainHub() {
        super();
    }
    protected void setup() {
        super.setup();
        assignmentStrategy = new ProximityBasedAssignmentStrategy();
        addBehaviour(new ReceiveDeliveryRequestBehaviour());
    }
    private class ReceiveDeliveryRequestBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate requestTemplate = MessageTemplate.MatchConversationId(Constants.MSG_ID_DELIVERY_REQUEST);
            ACLMessage deliveryRequest = myAgent.receive(requestTemplate);

            if (deliveryRequest != null) {
                try {
                    Task incomingRequest = (Task) deliveryRequest.getContentObject();

                    List<AID> warehouseAIDs = findAgentsByType(WarehouseAgent.class.getSimpleName());

                    List<WarehouseAgent> warehouseAgents = new ArrayList<>();
                    for (AID warehouseAID : warehouseAIDs) {
                        WarehouseAgent warehouseAgent = requestAgentInstance(warehouseAID, WarehouseAgent.class);
                        if (warehouseAgent != null) {
                            warehouseAgents.add(warehouseAgent);
                        }
                    }

                    AID warehouseAgent = assignmentStrategy.assignWarehouseAgent(incomingRequest, warehouseAgents);

                    ACLMessage deliveryInstruction = new ACLMessage(ACLMessage.INFORM);
                    deliveryInstruction.setConversationId(Constants.MSG_ID_DELIVERY_INFORM);
                    deliveryInstruction.addReceiver(warehouseAgent);
                    deliveryInstruction.setContentObject(incomingRequest);
                    // debug // TODO remove once tested
                    System.out.println("MainHub received request: " + incomingRequest.getQuantity());

                    send(deliveryInstruction);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                block();
            }
        }
    }
    protected void takeDown() {
        super.takeDown();
    }
    @Override
    public AgentType getAgentType() {
        return AgentType.AGENT_MAIN_HUB;
    }
}
