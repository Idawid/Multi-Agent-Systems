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
import simulationUtils.generators.OrderGenerator;

import java.util.List;
import java.util.Random;

public class RetailerAgent extends BaseAgent implements AgentTypeProvider {
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
            super(a, 1);
        }
        public SendDeliveryRequestBehaviour(Agent a, long period) {
            super(a, period);
        }
        @Override
        protected void onTick() {
            List<AID> mainHubAIDs = findAgentsByType(MainHub.class.getSimpleName());

            try {
                if (mainHubAIDs != null && mainHubAIDs.size() > 0) {
                    AID mainHubAID = mainHubAIDs.get(0); // Assuming only one MainHub agent // TODO: check
                    ACLMessage deliveryRequest = new ACLMessage(ACLMessage.REQUEST);
                    deliveryRequest.setConversationId(Constants.MSG_ID_DELIVERY_REQUEST);
                    deliveryRequest.addReceiver(mainHubAID);

                    Task request = OrderGenerator.generateRandomOrder(((RetailerAgent)myAgent).getLocationPin().getLocation());
                    deliveryRequest.setContentObject(request);

                    send(deliveryRequest);
                } else {
                    System.out.println("Main Hub agent not found.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // This is absolutely mandatory as the default period is 1 ms
            reset(getRandomDeliveryInterval());
        }
    }

    private class ReceiveDeliveryInstructionsBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchConversationId(Constants.MSG_ID_DELIVERY_INSTRUCTION);
            ACLMessage deliveryInstructions = receive(mt);

            if (deliveryInstructions != null) {
                try {
                    Task task = (Task) deliveryInstructions.getContentObject();
                    int quantity = task.getQuantity();
                    System.out.println("Received delivery instructions with quantity: " + quantity);
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }
    }

    private int getRandomDeliveryInterval() {
        return new Random().nextInt(MAX_DELIVERY_INTERVAL - MIN_DELIVERY_INTERVAL + 1) + MIN_DELIVERY_INTERVAL;
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.AGENT_RETAILER;
    }
}

