package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.Logger;
import mapUtils.LocationMap;
import mapUtils.locationPin.*;
import simulationUtils.Constants;
import simulationUtils.ProductType;
import simulationUtils.Task;
import simulationUtils.generators.OrderGenerator;

import java.rmi.Naming;
import java.util.List;
import java.util.Random;

public class RetailerAgent extends BaseAgent implements AgentTypeProvider, AgentDataProvider {
    // TODO [1] stock / profits:
    //  - retailers need to calculate profits (current profits)
    //  - quantity in task: quantity * profit per 1, truck: stock - quantity
    private static final int MIN_DELIVERY_INTERVAL = 5000; // Minimum delivery interval in milliseconds
    private static final int MAX_DELIVERY_INTERVAL = 15000; // Maximum delivery interval in milliseconds

    public RetailerAgent(Location location) {
        super(location);
    }

    public RetailerAgent() { }

    protected void setup() {
        super.setup();

        addBehaviour(new SendDeliveryRequestBehaviour(this));
        addBehaviour(new ReceiveDeliveryInstructionsBehaviour());
    }

    private class SendDeliveryRequestBehaviour extends TickerBehaviour {
        public SendDeliveryRequestBehaviour(Agent a) {
            super(a, 1000);
        }
        public SendDeliveryRequestBehaviour(Agent a, long period) {
            super(a, period);
        }
        @Override
        protected void onTick() {
            List<MainHub> mainHubAIDs = (List<MainHub>) findAgentsByClass(MainHub.class);

            try {
                if (mainHubAIDs != null && mainHubAIDs.size() > 0) {
                    AID mainHubAID = mainHubAIDs.get(0).getAID(); // Assuming only one MainHub agent // TODO: [2] more than one mainhub
                    ACLMessage deliveryRequest = new ACLMessage(ACLMessage.REQUEST);
                    deliveryRequest.setConversationId(Constants.MSG_ID_DELIVERY_REQUEST);
                    deliveryRequest.addReceiver(mainHubAID);

                    Task request = OrderGenerator.generateRandomOrder(((RetailerAgent)myAgent).getLocation(), myAgent.getAID());
                    deliveryRequest.setContentObject(request);

                    send(deliveryRequest);
                } else {
                    System.err.println("Main Hub agent not found.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Randomizes the delivery request intervals
            reset(getRandomDeliveryInterval());
        }
    }

    private class ReceiveDeliveryInstructionsBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchConversationId(Constants.MSG_ID_DELIVERY_INSTRUCTION);
            ACLMessage deliveryInstructions = receive(mt);

            if (deliveryInstructions != null) {
                try {
                    Task order = (Task) deliveryInstructions.getContentObject();
                    sell(order);
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }
    }

    private void sell(Task order) {
        ProductType productType = order.getProduct();
        int quantity = order.getQuantity();

        double rate = productType.getRate();
        double profits = rate * quantity;

        RetailerData retailerData = (RetailerData) this.getLocationPin().getAgentData();
        retailerData.setProfit(retailerData.getProfit() + profits);

        try {
            LocationMap locationMap = (LocationMap) Naming.lookup(LocationMap.REMOTE_LOCATION_MAP_ENDPOINT);
            locationMap.updateLocationPin(this.getLocalName(), this.getLocationPin());
        } catch (Exception e) {
            this.logger.log(Logger.WARNING, "Failed to update location on the remote map.");
        }
    }

    private int getRandomDeliveryInterval() {
        return new Random().nextInt(MAX_DELIVERY_INTERVAL - MIN_DELIVERY_INTERVAL + 1) + MIN_DELIVERY_INTERVAL;
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.AGENT_RETAILER;
    }
    @Override
    public AgentData getAgentData() {
        return new RetailerData();
    }
}

