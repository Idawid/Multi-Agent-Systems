package agents;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mapUtils.locationPin.*;
import simulationUtils.Constants;
import simulationUtils.Order;
import simulationUtils.assignmentStrategies.warehouse.ProximityBasedAssignmentStrategy;
import simulationUtils.assignmentStrategies.warehouse.WarehouseAssignmentStrategy;

import java.util.List;

public class MainHub extends BaseAgent implements AgentTypeProvider, AgentDataProvider {
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
        addBehaviour(new HandleStockRequestBehaviour());
    }

    private class ReceiveDeliveryRequestBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            //System.out.println(myAgent.getLocalName() + " runs whatever he's not supposed to");
            MessageTemplate requestTemplate = MessageTemplate.MatchConversationId(Constants.MSG_ID_DELIVERY_REQUEST);
            ACLMessage deliveryRequest = myAgent.receive(requestTemplate);

            if (deliveryRequest != null) {
                try {
                    Order incomingRequest = (Order) deliveryRequest.getContentObject();

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
    public class HandleStockRequestBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate requestTemplate = MessageTemplate.MatchConversationId(Constants.MSG_ID_STOCK_REQUEST);
            ACLMessage stockRequest = myAgent.receive(requestTemplate);

            if (stockRequest != null) {
                try {
                    int requestedStock = Integer.parseInt(stockRequest.getContent());
                    // send back always
                    sendStock(requestedStock, stockRequest.getSender());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }

        private void sendStock(int requestedStock, AID sender) {
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.setConversationId(Constants.MSG_ID_STOCK);
            reply.setContent(String.valueOf(requestedStock));
            reply.addReceiver(sender);
            myAgent.send(reply);
        }
    }
    protected void takeDown() {
        super.takeDown();
    }
    @Override
    public AgentType getAgentType() {
        return AgentType.AGENT_MAIN_HUB;
    }
    @Override
    public AgentData getAgentData() {
        return null;
    }
}
