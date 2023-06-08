package agents;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import simulationUtils.Constants;
import simulationUtils.DeliveryTimeEstimator;
import mapUtils.AgentType;
import mapUtils.AgentTypeProvider;
import mapUtils.Location;
import simulationUtils.Task;
import simulationUtils.generators.OrderGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TruckAgent extends BaseAgent implements AgentTypeProvider {
    // TODO handle delivery Task:
    //  - performTask should move this agent (BaseAgent has incorrect implementation of move)
    //  - send msg of id Constants.MSG_ID_DELIVERY_INSTRUCTION to RetailerAgent

    // TODO interrupts:
    //  - how the move may be interrupted, slowed down

    // TODO statistics:
    //  - how to gather information about the move, and what can WarehouseAgent do with that (???)
    private Task currentTask = null;
    private List<Integer> pastDeliveryTimes;

    public TruckAgent(Location location) {
        super(location);
        this.pastDeliveryTimes = new ArrayList<>();
    }

    public TruckAgent() { }
    protected void setup() {
        super.setup();

        addBehaviour(new ReceiveDeliveryInformBehaviour());
    }

    private class ReceiveDeliveryInformBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchConversationId(Constants.MSG_ID_DELIVERY_INFORM);
            ACLMessage deliveryRequest = receive(mt);

            if (deliveryRequest != null) {
                try {
                    Task task = (Task) deliveryRequest.getContentObject();
                    currentTask = task;
                } catch (UnreadableException e) {
                    System.out.println("Failed to extract Task object from the received message.");
                }
                performTask();
            } else {
                block();
            }
        }
    }

    private void performTask() {
        Location startLocation = this.getLocation();
        if (currentTask != null) {
            double distance = startLocation.getDistance(currentTask.getDestination());
            int timeInSeconds = (int) (distance / 60); // Truck velocity

            CompletableFuture<Void> future = moveToPosition(currentTask.getDestination(), timeInSeconds);
            future.thenRun(() -> {
                ACLMessage deliveryInstruction = new ACLMessage(ACLMessage.INFORM);
                deliveryInstruction.setConversationId(Constants.MSG_ID_DELIVERY_INSTRUCTION);
                deliveryInstruction.addReceiver(currentTask.getRetailerAID());

                try {
                    deliveryInstruction.setContentObject(currentTask);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                send(deliveryInstruction);
            });
        }
        // move back lmao
        double distance = getLocation().getDistance(startLocation);
        int timeInSeconds = (int) (distance / 60); // Truck velocity

        CompletableFuture<Void> future = moveToPosition(startLocation, timeInSeconds);
        future.thenRun(() -> {
            currentTask = null;
        });
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.AGENT_TRUCK;
    }
}

