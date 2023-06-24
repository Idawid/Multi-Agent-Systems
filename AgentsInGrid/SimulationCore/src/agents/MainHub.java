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
import simulationUtils.assignmentStrategies.warehouse.ProximityBasedAssignmentStrategy;
import simulationUtils.assignmentStrategies.warehouse.WarehouseAssignmentStrategy;

import java.util.List;

public class MainHub extends BaseAgent implements AgentTypeProvider {
    // TODO [1] stock:
    //  - mainhub has infinite stock, handle stock requests of warehouses
    private WarehouseAssignmentStrategy assignmentStrategy;

    public MainHub(Location location) {
        super(location);
        assignmentStrategy = new ProximityBasedAssignmentStrategy();
    }
    public MainHub() {
        super();
    }
    protected void setup() {
        super.setup();
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

                    List<WarehouseAgent> warehouseAgents = (List<WarehouseAgent>) findAgentsByClass(WarehouseAgent.class);

                    AID warehouseAgent = assignmentStrategy.assignWarehouseAgent(incomingRequest, warehouseAgents);

                    ACLMessage deliveryInstruction = new ACLMessage(ACLMessage.INFORM);
                    deliveryInstruction.setConversationId(Constants.MSG_ID_DELIVERY_INFORM);
                    deliveryInstruction.addReceiver(warehouseAgent);
                    deliveryInstruction.setContentObject(incomingRequest);

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
