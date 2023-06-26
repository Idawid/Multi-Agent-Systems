package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import mapUtils.locationPin.*;
import simulationUtils.Constants;
import simulationUtils.LocationMapUtils;
import simulationUtils.Order;
import simulationUtils.assignmentStrategies.truck.RandomAssignmentStrategy;
import simulationUtils.assignmentStrategies.truck.TruckAssignmentStrategy;
import simulationUtils.task.GoToStep;
import simulationUtils.task.Task;
import simulationUtils.task.TransferStockStep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WarehouseAgent extends BaseAgent implements AgentTypeProvider, AgentDataProvider {
    // TODO sync [1]:
    //  - something between agents and locationMap doesn't work, the warehouse stock gradually gets lower without any reasons
    private final int LOW_STOCK_THRESHOLD = (int) (0.5 * Constants.WAREHOUSE_CAPACITY);
    private final int HIGH_STOCK_THRESHOLD = (int) (0.95 * Constants.WAREHOUSE_CAPACITY);

    private TruckAssignmentStrategy truckAssignmentStrategy;
    private CopyOnWriteArrayList<Order> orders;
    private ConcurrentHashMap<UUID, List<WarehouseAgent>> taskTakeoverAcceptingWarehouses;
    private ConcurrentHashMap<UUID, Integer> taskTakeoverCounter;

    public WarehouseAgent(Location location) {
        super(location);
    }

    public WarehouseAgent() { }

    protected void setup() {
        super.setup();

        this.orders = new CopyOnWriteArrayList<>();
        truckAssignmentStrategy = new RandomAssignmentStrategy();
        taskTakeoverAcceptingWarehouses = new ConcurrentHashMap<>();
        taskTakeoverCounter = new ConcurrentHashMap<>();

        addBehaviour(new ReceiveDeliveryInformBehaviour());
        addBehaviour(new AssignTrucksBehavior(this, 1000));
        addBehaviour(new ReceiveRequestForTakeoverBehaviour());
        addBehaviour(new ReceiveResponseForTakeoverBehaviour());
        addBehaviour(new HandleStockMessagesBehaviour());
        addBehaviour(new RequestStockBehaviour(this, 1000));
        addBehaviour(new HandleStockRequestBehaviour());
    }

    private class ReceiveDeliveryInformBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchConversationId(Constants.MSG_ID_DELIVERY_INFORM);
            ACLMessage deliveryRequest = receive(mt);

            if (deliveryRequest != null) {
                try {
                    Order order = (Order) deliveryRequest.getContentObject();
                    orders.add(order);
                    System.out.println(myAgent.getLocalName() + " got a request for a delivery from: " +
                            order.getDestinationAID().getLocalName());
                } catch (UnreadableException e) {
                    System.err.println("Failed to extract Task object from the received message.");
                }
            } else {
                block();
            }
        }
    }

    private class AssignTrucksBehavior extends TickerBehaviour {

        public AssignTrucksBehavior(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            if (!orders.isEmpty()) {
                List<TruckAgent> trucks = (List<TruckAgent>) findAgentsByClass(TruckAgent.class);
                if (trucks == null) {
                    return;
                }

                trucks.removeIf(truckAgent -> !truckAgent.getContainerID().equals(((WarehouseAgent) myAgent).getContainerID()));
                trucks.removeIf(truckAgent -> truckAgent.getCurrentTask() != null);
                if (trucks.isEmpty()) {
                    Order order = orders.remove(0);
                    askOtherWarehousesToTakeoverTask(order);
                    return;
                }
                //System.out.println("debug - warehouse tick has trucks " + myAgent.getLocalName());
                Order order = orders.remove(0);
                AID truckAID = truckAssignmentStrategy.assignTruckAgent(order, trucks);
                ACLMessage deliveryInstruction = new ACLMessage(ACLMessage.INFORM);
                deliveryInstruction.setConversationId(Constants.MSG_ID_DELIVERY_INFORM);
                deliveryInstruction.addReceiver(truckAID);

                try {
                    String truck = truckAID.getLocalName();
                    String warehouse = myAgent.getLocalName();
                    String retailer = order.getDestinationAID().getLocalName();
                    int quantity = order.getQuantity();

                    Task task = new Task();
                    task.addStep(new TransferStockStep(truck, warehouse, quantity, TransferStockStep.TransferType.RESERVE));
                    task.addStep(new GoToStep(truck, warehouse));
                    task.addStep(new TransferStockStep(truck, warehouse, quantity, TransferStockStep.TransferType.TAKE));
                    task.addStep(new GoToStep(truck, retailer));
                    task.addStep(new TransferStockStep(truck, retailer, quantity, TransferStockStep.TransferType.GIVE));
                    task.addStep(new GoToStep(truck, warehouse));

                    deliveryInstruction.setContentObject(task);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(myAgent.getLocalName() + " asked truck: " + truckAID.getLocalName() +
                        " for a delivery");
                send(deliveryInstruction);
            }
        }

        // Assumes the task was removed from the tasks list so if the tasks can't be taken over it will be delayed
        private void askOtherWarehousesToTakeoverTask(Order order) {
            //System.out.println("debug - askOtherStart " + myAgent.getLocalName());
            List<WarehouseAgent> warehouses = (List<WarehouseAgent>) findAgentsByClass(WarehouseAgent.class);
            if (warehouses == null) {
                //System.out.println("debug - askOtherStart warehouses is null " + myAgent.getLocalName());
                orders.add(order);
                return;
            }
            //System.out.println("debug - askOtherStart warehouses not null " + myAgent.getLocalName());
            warehouses.removeIf(warehouseAgent -> warehouseAgent.getAID().equals(myAgent.getAID()));
            if (warehouses.isEmpty()) {
                orders.add(order);
                return;
            }
//            System.out.println("debug - put counter on key " + task.getId());
            taskTakeoverCounter.put(order.getId(), warehouses.size());
            taskTakeoverAcceptingWarehouses.put(order.getId(), new ArrayList<>());

            ACLMessage askForTakeover = new ACLMessage(ACLMessage.PROPOSE);
            askForTakeover.setConversationId(Constants.MSG_ID_TASK_TAKEOVER_REQUEST);
            warehouses.forEach(warehouseAgent -> askForTakeover.addReceiver(warehouseAgent.getAID()));
            try {
                askForTakeover.setContentObject(order);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(myAgent.getLocalName() + " is asking " + warehouses.size() + " other warehouses to takeover his task");
            send(askForTakeover);
        }
    }

    private class ReceiveRequestForTakeoverBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchConversationId(Constants.MSG_ID_TASK_TAKEOVER_REQUEST);
            ACLMessage takeoverRequest = receive(mt);

            if (takeoverRequest != null) {
                try {
                    // TODO: Increment logic for takeover acceptance
                    Order order = (Order) takeoverRequest.getContentObject();
                    List<TruckAgent> trucks = (List<TruckAgent>) findAgentsByClass(TruckAgent.class);
                    if (trucks == null) {
                        return;
                    }
                    trucks.removeIf(truckAgent -> !truckAgent.getContainerID().equals(((WarehouseAgent) myAgent).getContainerID()));
                    trucks.removeIf(truckAgent -> truckAgent.getCurrentTask() != null);
                    if (trucks.isEmpty()) {
                        ACLMessage response = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                        response.setConversationId(Constants.MSG_ID_TASK_TAKEOVER_RESPONSE);
                        response.addReceiver(takeoverRequest.getSender());
                        try {
                            response.setContentObject(order);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println(myAgent.getLocalName() + " rejecting takeover from: "
                                + takeoverRequest.getSender().getLocalName());
                        send(response);
                        return;
                    }
                    ACLMessage response = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    response.setConversationId(Constants.MSG_ID_TASK_TAKEOVER_RESPONSE);
                    response.addReceiver(takeoverRequest.getSender());
                    try {
                        response.setContentObject(order);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(myAgent.getLocalName() + " accepting takeover from: "
                            + takeoverRequest.getSender().getLocalName());
                    send(response);
                } catch (UnreadableException e) {
                    System.err.println("Failed to extract Task object from the received takeover request.");
                }
            } else {
                block();
            }
        }
    }

    private class ReceiveResponseForTakeoverBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchConversationId(Constants.MSG_ID_TASK_TAKEOVER_RESPONSE);
            ACLMessage takeoverResponse = receive(mt);

            if (takeoverResponse != null) {
                try {
                    Order order = (Order) takeoverResponse.getContentObject();
                    if (!taskTakeoverCounter.containsKey(order.getId())) {
                        System.err.println(myAgent.getLocalName() + " got a takeover response from "
                                + takeoverResponse.getSender().getLocalName()
                                + " when a counter for given task doesn't exist");
                        return;
                    }
//                    System.out.println("debug - decrementing from value " );
                    taskTakeoverCounter.put(order.getId(), taskTakeoverCounter.get(order.getId())-1);
                    if (!taskTakeoverAcceptingWarehouses.containsKey(order.getId())) {
                        taskTakeoverAcceptingWarehouses.put(order.getId(), new ArrayList<>());
                    }
                    if (takeoverResponse.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        taskTakeoverAcceptingWarehouses.get(order.getId()).add((WarehouseAgent) (
                                (WarehouseAgent) myAgent).requestAgentInstance(takeoverResponse.getSender()));
                    }

                    if (taskTakeoverCounter.get(order.getId()) == 0) {
                        if (taskTakeoverAcceptingWarehouses.get(order.getId()).isEmpty()) {
                            System.out.println(myAgent.getLocalName() + " couldn't find any warehouse to takeover it's task");
                            orders.add(order);
                            return;
                        }
                        WarehouseAgent chosenWarehouse = getClosestAgent(taskTakeoverAcceptingWarehouses.get(order.getId()),
                                order.getDestination());
                        System.out.println(myAgent.getLocalName() + " gave away his task to: " +
                                chosenWarehouse.getLocalName());
                        sendGiveawayMessage(chosenWarehouse.getAID(), order);
                        taskTakeoverAcceptingWarehouses.remove(order.getId());
                        taskTakeoverCounter.remove(order.getId());
                    }

                } catch (UnreadableException e) {
                    System.err.println("Failed to extract Task object from the received takeover request.");
                }
            } else {
                block();
            }
        }

        private WarehouseAgent getClosestAgent(List<WarehouseAgent> warehouseAgents, Location destination) {
            if (warehouseAgents.isEmpty()) {
                throw new IllegalArgumentException("Cant find closest agent in empty list");
            }
            WarehouseAgent closestAcceptingWarehouse = warehouseAgents.get(0);
            double closestDistance = Double.MAX_VALUE;
            for (WarehouseAgent warehouseAgent : warehouseAgents) {
                double distance = warehouseAgent.getLocationPin().getDistance(destination);
                if (closestDistance > distance) {
                    closestDistance = distance;
                    closestAcceptingWarehouse = warehouseAgent;
                }
            }
            return closestAcceptingWarehouse;
        }

        private void sendGiveawayMessage(AID receiverAID, Order order) {
            ACLMessage giveawayTaskMessage = new ACLMessage(ACLMessage.INFORM);
            giveawayTaskMessage.setConversationId(Constants.MSG_ID_DELIVERY_INFORM);
            try {
                giveawayTaskMessage.setContentObject(order);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            giveawayTaskMessage.addReceiver(receiverAID);

            send(giveawayTaskMessage);
        }
    }

    public class HandleStockMessagesBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchConversationId(Constants.MSG_ID_STOCK);
            ACLMessage stockMsg = receive(mt);

            if (stockMsg != null) {
                try {
                    int incomingStock = Integer.parseInt(stockMsg.getContent());
                    int excessStock = checkAndUpdateStock(incomingStock);
                    if (excessStock > 0) {
                        ACLMessage reply = stockMsg.createReply();
                        reply.setConversationId(Constants.MSG_ID_STOCK);
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(String.valueOf(excessStock));
                        myAgent.send(reply);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }

        private int checkAndUpdateStock(int incomingStock) {
            WarehouseData warehouseData = (WarehouseData) ((WarehouseAgent) myAgent).getLocationPin().getAgentData();
            int newStockLevel = warehouseData.getCurrentStock() + incomingStock;
            System.out.println(newStockLevel);
            int excessStock = newStockLevel - warehouseData.getMaxStock();

            if (excessStock > 0) {
                warehouseData.setCurrentStock(warehouseData.getMaxStock());
                LocationMapUtils.updateLocationPinNonBlocking(myAgent.getLocalName(), getLocationPin());
                return excessStock;
            } else {
                warehouseData.setCurrentStock(newStockLevel);
                LocationMapUtils.updateLocationPinNonBlocking(myAgent.getLocalName(), getLocationPin());
                return 0;
            }
        }
    }

    public class RequestStockBehaviour extends TickerBehaviour {

        public RequestStockBehaviour(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            WarehouseData warehouseData = (WarehouseData) ((WarehouseAgent) myAgent).getLocationPin().getAgentData();
            if (warehouseData.getCurrentStock() < LOW_STOCK_THRESHOLD) {
                requestStock();
            }
        }

        private void requestStock() {
            AID[] agentAIDs = findOtherAgents();
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.setConversationId(Constants.MSG_ID_STOCK_REQUEST);
            for (AID agentAID : agentAIDs) {
                request.addReceiver(agentAID);
            }
            // ask for random portion of the missing stock
            WarehouseData warehouseData = (WarehouseData) ((WarehouseAgent) myAgent).getLocationPin().getAgentData();
            double randomNumber = 0.05 + Math.random() * (0.3 - 0.05);
            int missingStockPortion = (int) ((Constants.WAREHOUSE_CAPACITY - warehouseData.getCurrentStock()) * randomNumber);
            request.setContent(String.valueOf(missingStockPortion));
            System.out.println("Requesting: " + missingStockPortion);
            myAgent.send(request);
        }

        private AID[] findOtherAgents() {
            List<WarehouseAgent> warehouseAgents = (List<WarehouseAgent>) findAgentsByClass(WarehouseAgent.class);
            List<MainHub> mainHubs = (List<MainHub>) findAgentsByClass(MainHub.class);

            List<AID> combinedAIDList = Stream.concat(
                            warehouseAgents.stream().map(BaseAgent::getAID),
                            mainHubs.stream().map(BaseAgent::getAID)
                    )
                    .collect(Collectors.toList());

            return combinedAIDList.toArray(new AID[0]);
        }
    }

    public class HandleStockRequestBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchConversationId(Constants.MSG_ID_STOCK_REQUEST);
            ACLMessage stockRequest = receive(mt);

            if (stockRequest != null) {
                try {
                    int requestedStock = Integer.parseInt(stockRequest.getContent());

                    WarehouseData warehouseData = (WarehouseData) ((WarehouseAgent) myAgent).getLocationPin().getAgentData();
                    int newStockLevel = warehouseData.getCurrentStock() - requestedStock;
                    // send back stock if it doesn't put you dangerously low
                    if (newStockLevel > LOW_STOCK_THRESHOLD) {
                        System.out.println(myAgent.getLocalName() + ": Received stock request, sending.");
                        warehouseData.setCurrentStock(newStockLevel);
                        LocationMapUtils.updateLocationPinNonBlocking(myAgent.getLocalName(), ((WarehouseAgent) myAgent).getLocationPin());

                        sendStock(requestedStock, stockRequest.getSender());
                    }

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

    @Override
    public AgentType getAgentType() {
        return AgentType.AGENT_WAREHOUSE;
    }
    @Override
    public AgentData getAgentData() {
        return new WarehouseData();
    }
}
