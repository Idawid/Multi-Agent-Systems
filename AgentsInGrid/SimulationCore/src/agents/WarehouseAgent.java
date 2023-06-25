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
import simulationUtils.assignmentStrategies.truck.RandomAssignmentStrategy;
import simulationUtils.assignmentStrategies.truck.TruckAssignmentStrategy;
import simulationUtils.assignmentStrategies.warehouse.ProximityBasedAssignmentStrategy;
import simulationUtils.assignmentStrategies.warehouse.WarehouseAssignmentStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WarehouseAgent extends BaseAgent implements AgentTypeProvider {
    // TODO [1] stock:
    //  - warehouses need to have stock (max stock, current stock, percentage?)
    //  - request stock from MainHub agent
    //  - quantity in task: warehouse: stock - quantity, truck: stock + quantity
    // TODO [1] exchanging stock:
    //  - if over max stock try to exchange request negative, if 0 stock request positive

    // TODO [1] exchanging tasks:
    //  - pass the task to another warehouse (maybe request task-> check if more than 3? tasks-> pass the task)

    private TruckAssignmentStrategy truckAssignmentStrategy;
    private WarehouseAssignmentStrategy warehouseAssignmentStrategy;
    private List<Task> tasks;
    private HashMap<Task, List<WarehouseAgent>> taskTakeoverAcceptingWarehouses;
    private HashMap<Task, Integer> taskTakeoverCounter;

    public WarehouseAgent(Location location) {
        super(location);
    }

    public WarehouseAgent() { }

    protected void setup() {
        super.setup();

        this.tasks = new ArrayList<>();
        truckAssignmentStrategy = new RandomAssignmentStrategy();
        warehouseAssignmentStrategy = new ProximityBasedAssignmentStrategy();
        taskTakeoverAcceptingWarehouses = new HashMap<>();
        taskTakeoverCounter = new HashMap<>();

        addBehaviour(new ReceiveDeliveryInformBehaviour());
        addBehaviour(new AssignTrucksBehavior(this, 1000));
        addBehaviour(new ReceiveRequestForTakeoverBehaviour());
        addBehaviour(new ReceiveResponseForTakeoverBehaviour());
    }

    private class ReceiveDeliveryInformBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchConversationId(Constants.MSG_ID_DELIVERY_INFORM);
            ACLMessage deliveryRequest = receive(mt);

            if (deliveryRequest != null) {
                try {
                    Task task = (Task) deliveryRequest.getContentObject();
                    tasks.add(task);
                    System.out.println(myAgent.getLocalName() + " got a request for a delivery from: " +
                            task.getRetailerAID().getLocalName());
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
            if (!tasks.isEmpty()) {
                List<TruckAgent> trucks = (List<TruckAgent>) findAgentsByClass(TruckAgent.class);
                if (trucks == null) {
                    return;
                }

                trucks.removeIf(truckAgent -> !truckAgent.getContainerID().equals(((WarehouseAgent) myAgent).getContainerID()));
                trucks.removeIf(truckAgent -> truckAgent.getCurrentTask() != null);
                if (trucks.isEmpty()) {
                    Task task = tasks.remove(0);
                    askOtherWarehousesToTakeoverTask(task);
                    return;
                }
                System.out.println("debug - warehouse tick has trucks " + myAgent.getLocalName());
                Task task = tasks.remove(0);
                AID truckAID = truckAssignmentStrategy.assignTruckAgent(task, trucks);
                ACLMessage deliveryInstruction = new ACLMessage(ACLMessage.INFORM);
                deliveryInstruction.setConversationId(Constants.MSG_ID_DELIVERY_INFORM);
                deliveryInstruction.addReceiver(truckAID);

                try {
                    deliveryInstruction.setContentObject(task);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(myAgent.getLocalName() + " asked truck: " + truckAID.getLocalName() +
                        " for a delivery to: " + task.getRetailerAID().getLocalName());
                send(deliveryInstruction);
            }
        }

        // Assumes the task was removed from the tasks list so if the tasks can't be taken over it will be delayed
        private void askOtherWarehousesToTakeoverTask(Task task) {
            System.out.println("debug - askOtherStart " + myAgent.getLocalName());
            List<WarehouseAgent> warehouses = (List<WarehouseAgent>) findAgentsByClass(WarehouseAgent.class);
            if (warehouses == null) {
                System.out.println("debug - askOtherStart warehouses is null " + myAgent.getLocalName());
                tasks.add(task);
                return;
            }
            System.out.println("debug - askOtherStart warehouses not null " + myAgent.getLocalName());
            warehouses.remove((WarehouseAgent) myAgent);
            if (warehouses.isEmpty()) {
                tasks.add(task);
                return;
            }
            System.out.println("debug - askOther send propose " + myAgent.getLocalName());
            taskTakeoverCounter.put(task, warehouses.size());
            taskTakeoverAcceptingWarehouses.put(task, new ArrayList<>());

            ACLMessage askForTakeover = new ACLMessage(ACLMessage.PROPOSE);
            askForTakeover.setConversationId(Constants.MSG_ID_TASK_TAKEOVER_REQUEST);
            warehouses.forEach(warehouseAgent -> askForTakeover.addReceiver(warehouseAgent.getAID()));
            try {
                askForTakeover.setContentObject(task);
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
                    Task task = (Task) takeoverRequest.getContentObject();
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
                            response.setContentObject(task);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println(myAgent.getLocalName() + " rejecting takeover");
                        send(response);
                        return;
                    }
                    ACLMessage response = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    response.setConversationId(Constants.MSG_ID_TASK_TAKEOVER_RESPONSE);
                    response.addReceiver(takeoverRequest.getSender());
                    try {
                        response.setContentObject(task);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(myAgent.getLocalName() + " accepting takeover");
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
                    Task task = (Task) takeoverResponse.getContentObject();
                    if (!taskTakeoverCounter.containsKey(task)) {
                        throw new RuntimeException("Got a takeover response when a counter for given task doesn't exist");
                    }
                    taskTakeoverCounter.put(task, taskTakeoverCounter.get(task)-1);
                    if (!taskTakeoverAcceptingWarehouses.containsKey(task)) {
                        taskTakeoverAcceptingWarehouses.put(task, new ArrayList<>());
                    }
                    if (takeoverResponse.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        taskTakeoverAcceptingWarehouses.get(task).add((WarehouseAgent) (
                                (WarehouseAgent) myAgent).requestAgentInstance(takeoverResponse.getSender()));
                    }

                    if (taskTakeoverCounter.get(task) == 0) {
                        if (taskTakeoverAcceptingWarehouses.get(task).isEmpty()) {
                            System.out.println(myAgent.getLocalName() + " couldn't find any warehouse to takeover it's task");
                            tasks.add(task);
                            return;
                        }
                        WarehouseAgent chosenWarehouse = getClosestAgent(taskTakeoverAcceptingWarehouses.get(task),
                                task.getDestination());
                        System.out.println(myAgent.getLocalName() + " gave away his task to: " +
                                chosenWarehouse.getLocalName());
                        sendGiveawayMessage(chosenWarehouse.getAID(), task);
                        taskTakeoverAcceptingWarehouses.remove(task);
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

        private void sendGiveawayMessage(AID receiverAID, Task task) {
            ACLMessage giveawayTaskMessage = new ACLMessage(ACLMessage.INFORM);
            giveawayTaskMessage.setConversationId(Constants.MSG_ID_DELIVERY_INFORM);
            try {
                giveawayTaskMessage.setContentObject(task);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            giveawayTaskMessage.addReceiver(receiverAID);

            send(giveawayTaskMessage);
        }
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.AGENT_WAREHOUSE;
    }
}
