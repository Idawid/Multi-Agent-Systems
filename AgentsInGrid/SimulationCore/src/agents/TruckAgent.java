package agents;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import simulationUtils.AgentFinder;
import simulationUtils.DeliveryTimeEstimator;
import mapUtils.AgentType;
import mapUtils.AgentTypeProvider;
import mapUtils.Location;
import simulationUtils.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TruckAgent extends BaseAgent implements AgentTypeProvider {
    private AID warehouseAgent;
    private Task currentTask = null;
    private List<Integer> pastDeliveryTimes;

    public TruckAgent(Location location) {
        super(location);
        this.pastDeliveryTimes = new ArrayList<>();
    }

    public TruckAgent() { }
    protected void setup() {
        super.setup();

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

    public void assignTask(Task task) {
        this.currentTask = task;
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.AGENT_TRUCK;
    }
}

