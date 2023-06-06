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
import simulationUtils.AgentFinder;
import simulationUtils.Task;
import simulationUtils.generators.OrderGenerator;

import java.io.IOException;

public class RetailerAgent extends BaseAgent implements AgentTypeProvider {

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
            super(a, 0);
        }
        public SendDeliveryRequestBehaviour(Agent a, long period) {
            super(a, period);
        }
        @Override
        protected void onTick() {
            AID[] mainHubAIDs = AgentFinder.findAgentsByType(myAgent, MainHub.class.getSimpleName());

            if (mainHubAIDs.length > 0) {
                AID mainHubAID = mainHubAIDs[0]; // Assuming only one MainHub agent
                ACLMessage deliveryRequest = new ACLMessage(ACLMessage.REQUEST);
                deliveryRequest.setConversationId("delivery-request");
                deliveryRequest.addReceiver(mainHubAID);

                Task request = OrderGenerator.generateRandomOrder(((RetailerAgent)myAgent).getLocationPin().getLocation());
                try {
                    deliveryRequest.setContentObject(request);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                send(deliveryRequest);
            } else {
                System.out.println("Main Hub agent not found.");
            }
        }
    }

    private class ReceiveDeliveryInstructionsBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchConversationId("delivery-instructions");
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

    @Override
    public AgentType getAgentType() {
        return AgentType.AGENT_RETAILER;
    }
}

