package agents;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import mapUtils.locationPin.*;
import simulationUtils.Constants;
import simulationUtils.Task;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class TruckAgent extends BaseAgent implements AgentTypeProvider, AgentDataProvider {
    // TODO [1] stock:
    //  - truck needs to have stock (max stock, current load, percentage?)

    // TODO [1] road events:
    //  - the move may be interrupted, slowed down

    // TODO [1] time estimate:
    //  - instead of t=s/v from the based on the distance from truck to retailer

    // TODO [3] better time estimate:
    //  - instead of t=s/v
    //  - how to gather information about the move, and what can WarehouseAgent do with that (???)

    // TODO [3] task chains:
    //  - multiple tasks, varied tasks, all use the same stock

    private Task currentTask = null;
    private CopyOnWriteArrayList<Integer> pastDeliveryTimes;
    private int maxLoad;
    private int load;

    public TruckAgent(Location location, int maxLoad) {
        super(location);
        this.pastDeliveryTimes = new CopyOnWriteArrayList<>();
        this.load = 0;
        this.maxLoad = maxLoad;
    }

    public TruckAgent() { }

    protected void setup() {
        super.setup();

        addBehaviour(new ReceiveDeliveryInformBehaviour());
    }

    private class ReceiveDeliveryInformBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            if (currentTask != null) {
                block();
                return;
            }

            MessageTemplate mt = MessageTemplate.MatchConversationId(Constants.MSG_ID_DELIVERY_INFORM);
            ACLMessage deliveryRequest = receive(mt);
            if (deliveryRequest == null) {
                block();
                return;
            }

            try {
                currentTask = (Task) deliveryRequest.getContentObject();

                String pickUpName = deliveryRequest.getSender().getLocalName();
                String destinationName = currentTask.getDestinationAID().getLocalName();

                performTask(pickUpName, destinationName);
            } catch (UnreadableException e) {
                System.err.println("Failed to extract Task object from the received message.");
            }
        }
    }

    private void performTask(String pickUpName, String destinationName) {
        if (currentTask != null) {
            CompletableFuture.runAsync(() -> {
                LocationPin pickUpLocation = getLocationPinBlocking(pickUpName);
                LocationPin destinationLocation = getLocationPinBlocking(destinationName);

//                // go to pickup location
//                pickUpLocation = getLocationPinBlocking(pickUpName);
//                moveToPosition(pickUpLocation);
//                // take it from the pick up location
//
//                // load it onto truck
//                load += currentTask.getQuantity();
//                double loadPercentage = (double) load / maxLoad;
//                ((TruckData)getLocationPin().getAgentData()).setLoadPercentage(loadPercentage);
//                updateLocationPinNonBlocking(this.getLocalName(), this.getLocationPin());
//
//                if (pickUpLocation.getAgentData() instanceof HasPercentage) {
//                    ((HasPercentage) pickUpLocation.getAgentData()).(loadPercentage);
//                }

                moveToPosition(pickUpLocation);

                moveToPosition(destinationLocation);
                performDelivery();
                //load -= currentTask.getQuantity();

                moveToPosition(pickUpLocation);
                currentTask = null;
            });
        }
    }

    private void performDelivery() {
        ACLMessage deliveryInstruction = new ACLMessage(ACLMessage.INFORM);
        deliveryInstruction.setConversationId(Constants.MSG_ID_DELIVERY_INSTRUCTION);
        deliveryInstruction.addReceiver(currentTask.getDestinationAID());

        try {
            deliveryInstruction.setContentObject(currentTask);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        send(deliveryInstruction);
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.AGENT_TRUCK;
    }
    @Override
    public AgentData getAgentData() {
        return new TruckData();
    }
    @Override
    public void locationUpdated(String agentName, LocationPin newLocationPin) {
        if (agentName != this.getLocalName()) {
            return;
        }
        super.locationUpdated(agentName, newLocationPin);
        TruckData data = (TruckData)getLocationPin().getAgentData();
        this.load = (int) data.getLoadPercentage() * maxLoad;
    }
}

