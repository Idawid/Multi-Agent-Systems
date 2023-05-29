package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import simulationUtils.AgentFinder;
import simulationUtils.DeliveryTimeEstimator;
import utils.AgentType;
import utils.AgentTypeProvider;
import utils.Location;
import simulationUtils.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TruckAgent extends Agent implements AgentTypeProvider {
    private AID warehouseAgent;
    private Task currentTask = null;
    private Location location;
    private List<Integer> pastDeliveryTimes;

    public TruckAgent(Location location) {
        this.location = location;
        this.pastDeliveryTimes = new ArrayList<>();
    }

    public TruckAgent() { }

    protected void setup() {
        warehouseAgent = AgentFinder.findAgentsByType(this, "WarehouseAgent")[0];

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getSender().equals(warehouseAgent)) {
                    try {
                        currentTask = (Task) msg.getContentObject();
                        performTask();
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void performTask() {
        if (currentTask != null) {
            double estimatedTime = DeliveryTimeEstimator.estimateDeliveryTime(pastDeliveryTimes);

            addBehaviour(new WakerBehaviour(this, (long) estimatedTime) {
                protected void onWake() {
                    pastDeliveryTimes.add((int) estimatedTime);
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(warehouseAgent);
                    try {
                        msg.setContentObject(currentTask);
                        send(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    currentTask = null;
                }
            });
        }
    }

    public Location getLocation() {
        return location;
    }

    public void assignTask(Task task) {
        this.currentTask = task;
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.AGENT_RETAILER;
    }
}

