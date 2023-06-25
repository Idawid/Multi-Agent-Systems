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
import simulationUtils.Task;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class TruckAgent extends BaseAgent implements AgentTypeProvider, AgentDataProvider {
    // TODO [1] stock:
    //  - truck needs to have stock (max stock, current load, percentage?)

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
    private boolean isBrokeDown = false;
    private boolean isMovingToMechanic = false;


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

                String pickUpName = deliveryRequest.getSender().getLocalName();
                String destinationName = currentTask.getDestinationAID().getLocalName();

                performTask(pickUpName, destinationName);
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

    private void performTask(String pickUpName, String destinationName) {
        Location originalLocation = new Location(getLocationPin());
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
        super.locationUpdated(agentName, newLocationPin);
        TruckData data = (TruckData)getLocationPin().getAgentData();
        this.load = (int) data.getLoadPercentage() * maxLoad;
    }

    private void moveToPosition(Location targetLocation) {
        LocationPin startLocation = new LocationPin(locationPin);
        LocationPin tempLocation = new LocationPin(locationPin);
        String keyName = getLocalName();

        int timeInMilliseconds = (int) (startLocation.getDistance(targetLocation) * 1000) / 60;
        int totalSteps = timeInMilliseconds * LocationMap.UPDATES_PER_SECOND / 1000 ;
        int currentSteps = 0;
        double stepX = ((double) targetLocation.getX() - startLocation.getX()) / totalSteps;
        double stepY = ((double) targetLocation.getY() - startLocation.getY()) / totalSteps;

        for (int i = 0; i < totalSteps; i++) {
//            if (isBrokeDown && !isMovingToMechanic) {
//                locationPin.setLocation(tempLocation);
//                handleBrakeDown();
//                return;
//            }
            tempLocation.setX(startLocation.getX() + (int) (currentSteps * stepX));
            tempLocation.setY(startLocation.getY() + (int) (currentSteps * stepY));
            currentSteps++;

            updateLocationPinNonBlocking(keyName, tempLocation);

            try {
                Thread.sleep(1000 / LocationMap.UPDATES_PER_SECOND);
            } catch (InterruptedException e) { }
        }
        locationPin.setLocation(tempLocation);
    }

    private void handleBrakeDown() {
        List<MechanicAgent> mechanics = (List<MechanicAgent>) findAgentsByClass(MechanicAgent.class);
        if (mechanics == null || mechanics.isEmpty()) {
            throw new RuntimeException("Truck cant find a mechanic");
        }
        if (currentTask != null) {
            ACLMessage informWarehouse = new ACLMessage(ACLMessage.INFORM);
            informWarehouse.setConversationId(Constants.MSG_ID_DELIVERY_INFORM);
            try {
                informWarehouse.setContentObject(currentTask);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //informWarehouse.addReceiver(currentTask.get);
            send(informWarehouse);
        }

        // Nice to have: introduce more mechanics
        MechanicAgent chosenMechanic = mechanics.get(0);

        moveToPosition(chosenMechanic.getLocation());
    }

}

