package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import utils.Location;
import utils.Task;

public class RetailerAgent extends Agent {
    private Location location;

    public RetailerAgent(Location location) {
        this.location = location;
    }

    protected void setup() {
        // Add behaviour to listen for deliveries from TruckAgent
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    // We received a message from a TruckAgent
                    try {
                        // Assume that the content of the message is a serialized Task object
                        Task deliveredTask = (Task) msg.getContentObject();

                        // Acknowledge receipt of the delivery
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.CONFIRM);
                        reply.setContent("Received delivery of " + deliveredTask.getProduct()
                                + " in quantity: " + deliveredTask.getQuantity());
                        send(reply);
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                } else {
                    block();
                }
            }
        });
    }

    public Location getLocation() {
        return location;
    }
}

