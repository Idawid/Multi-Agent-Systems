package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import mapUtils.LocationMap;
import mapUtils.locationPin.*;
import simulationUtils.Constants;
import simulationUtils.task.Task;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class TruckAgent extends BaseAgent implements AgentTypeProvider, AgentDataProvider {
    // TODO [1] road events:
    //  - the move may be interrupted, slowed down

    // TODO [1] time estimate:
    //  - instead of t=s/v from the based on the distance from truck to retailer

    // TODO [3] better time estimate:
    //  - instead of t=s/v
    //  - how to gather information about the move, and what can WarehouseAgent do with that (???)

    private Task currentTask = null;
    private CopyOnWriteArrayList<Integer> pastDeliveryTimes;
    private boolean isBrokeDown = false;
    private boolean isMovingToMechanic = false;

    public TruckAgent(Location location) {
        super(location);
        this.pastDeliveryTimes = new CopyOnWriteArrayList<>();
    }

    public TruckAgent() { }

    protected void setup() {
        super.setup();

        addBehaviour(new ReceiveDeliveryInformBehaviour());
        addBehaviour(new RaiseRoadEventBehaviour(this, 1000, 0.5));
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
                performTask();
            } catch (UnreadableException e) {
                System.err.println("Failed to extract Task object from the received message.");
            }
        }
    }

    private class RaiseRoadEventBehaviour extends TickerBehaviour {
        private final double eventProbability;
        private final Random random;
        public RaiseRoadEventBehaviour(Agent a, long period, double eventProbabilityDenominator) {
            super(a, period);
            this.eventProbability = eventProbabilityDenominator;
            this.random = new Random();
        }

        @Override
        protected void onTick() {
            if (random.nextDouble() <= eventProbability && currentTask != null) {
                isBrokeDown = true;
                setLocationPin(new LocationPin(locationPin.getLocation(), getAgentType(), getAgentData()));
            }
        }
    }

    private void performTask() {
        if (currentTask != null) {
            CompletableFuture.runAsync(() -> {
                currentTask.execute();
                currentTask = null;
            });
        }
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    @Override
    public AgentType getAgentType() {
        return isBrokeDown ? AgentType.AGENT_TRUCK_BROKEN : AgentType.AGENT_TRUCK;
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

        if (newLocationPin != null) {
            this.setLocationPin(newLocationPin);
        } else {
            this.doDelete();
        }
    }
}

