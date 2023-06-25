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
            MessageTemplate mt = MessageTemplate.MatchConversationId(Constants.MSG_ID_DELIVERY_INFORM);
            ACLMessage deliveryRequest = receive(mt);

            if (deliveryRequest == null) {
                block();
                return;
            }
            try {
                currentTask = (Task) deliveryRequest.getContentObject();
                //System.out.println(myAgent.getLocalName() + " got assigned a task!");
                performTask();
            } catch (UnreadableException e) {
                System.err.println("Failed to extract Task object from the received message.");
            }
        }
    }

    private void performTask() {
        Location originalLocation = new Location(getLocationPin());
        if (currentTask != null) {
            CompletableFuture.runAsync(() -> {
                moveToPosition(currentTask.getDestination());
                performDelivery();
                moveToPosition(originalLocation);
                currentTask = null;
            });
        }
    }

    private void performDelivery() {
        ACLMessage deliveryInstruction = new ACLMessage(ACLMessage.INFORM);
        deliveryInstruction.setConversationId(Constants.MSG_ID_DELIVERY_INSTRUCTION);
        deliveryInstruction.addReceiver(currentTask.getRetailerAID());

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
}

