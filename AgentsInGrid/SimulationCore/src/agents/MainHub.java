package agents;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mapUtils.locationPin.*;
import simulationUtils.Constants;
import simulationUtils.Order;
import simulationUtils.assignmentStrategies.truck.RandomAssignmentStrategy;
import simulationUtils.assignmentStrategies.truck.TruckAssignmentStrategy;
import simulationUtils.assignmentStrategies.warehouse.ProximityBasedAssignmentStrategy;
import simulationUtils.assignmentStrategies.warehouse.WarehouseAssignmentStrategy;
import simulationUtils.task.GoToStep;
import simulationUtils.task.Task;
import simulationUtils.task.TransferStockStep;

import java.io.IOException;
import java.util.List;

public class MainHub extends BaseAgent implements AgentTypeProvider, AgentDataProvider {
    private WarehouseAssignmentStrategy assignmentStrategy;
    private TruckAssignmentStrategy truckAssignmentStrategy;

    public MainHub(Location location) {
        super(location);
        assignmentStrategy = new ProximityBasedAssignmentStrategy();
        truckAssignmentStrategy = new RandomAssignmentStrategy();
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

        private void sendStock(int requestedStock, AID receiverAID) {
            List<TruckAgent> trucks = (List<TruckAgent>) findAgentsByClass(TruckAgent.class);
            if (trucks == null) {
                ACLMessage reply = new ACLMessage(ACLMessage.REFUSE);
                reply.setConversationId(Constants.MSG_ID_STOCK);
                reply.setContent(String.valueOf(requestedStock));
                reply.addReceiver(receiverAID);
                myAgent.send(reply);
                return;
            }

            trucks.removeIf(truckAgent -> truckAgent.getCurrentTask() != null);
            if (trucks.isEmpty()) {
                ACLMessage reply = new ACLMessage(ACLMessage.REFUSE);
                reply.setConversationId(Constants.MSG_ID_STOCK);
                reply.setContent(String.valueOf(requestedStock));
                reply.addReceiver(receiverAID);
                myAgent.send(reply);
                return;
            }
            //System.out.println("debug - warehouse tick has trucks " + myAgent.getLocalName());
            AID truckAID = truckAssignmentStrategy.assignTruckAgent(null, trucks);
            ACLMessage deliveryInstruction = new ACLMessage(ACLMessage.INFORM);
            deliveryInstruction.setConversationId(Constants.MSG_ID_DELIVERY_INFORM);
            deliveryInstruction.addReceiver(truckAID);

            try {
                AID truck = truckAID;
                AID giver = myAgent.getAID();
                AID receiver = receiverAID;
                WarehouseAgent receiverWarehouse = (WarehouseAgent) requestAgentInstance(receiverAID);
                Task task = new Task();
                task.addStep(new GoToStep(truck, giver));
                task.addStep(new TransferStockStep(truck, giver, requestedStock, TransferStockStep.TransferType.TAKE, receiverWarehouse));
                task.addStep(new GoToStep(truck, receiver));
                task.addStep(new TransferStockStep(truck, receiver, requestedStock, TransferStockStep.TransferType.GIVE, receiverWarehouse));

                deliveryInstruction.setContentObject(task);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(myAgent.getLocalName() + " sending " + requestedStock + " stock to " + receiverAID.getLocalName());
            ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
            reply.setConversationId(Constants.MSG_ID_STOCK);
            reply.setContent(String.valueOf(requestedStock));
            reply.addReceiver(receiverAID);
            myAgent.send(reply);
            send(deliveryInstruction);
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
        return new MainHubData(Constants.WAREHOUSE_CAPACITY);
    }
}
