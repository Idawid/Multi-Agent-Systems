package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mapUtils.*;
import simulationUtils.Constants;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BaseAgent extends Agent implements LocationMapObserver, Serializable {
    // TODO needs refactor
    // TODO move doesn't work :(
    // TODO this class and all its fields HAVE TO be serializable for setContentObject(this) to work
    private LocationPin locationPin;
    // private Timer updateTimer; // Not serializable - will crash

    public BaseAgent(Location location) {
        super();
        this.locationPin = new LocationPin(location, this.getClass());
    }
    public BaseAgent() {
        super();
    }
    protected void setup() {
        registerWithDF();
        try {
            LocationMap locationMap = (LocationMap) Naming.lookup("rmi://localhost/locationMap");
            locationMap.addLocationPin(this.getLocalName(), locationPin);
            locationMap.registerObserver(new LocationMapObserverProxy(this));
            // startPositionUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        addBehaviour(new HandleRequestBehaviour());
    }

    protected void takeDown() {
        deregisterFromDF();
        try {
            LocationMap locationMap = (LocationMap) Naming.lookup("rmi://localhost/locationMap");
            locationMap.removeLocationPin(this.getLocalName());
            locationMap.unregisterObserver(new LocationMapObserverProxy(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // stopPositionUpdate();
    }

    private void registerWithDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(this.getClass().getSimpleName());
        sd.setName(getLocalName());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private void deregisterFromDF() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private class HandleRequestBehaviour extends CyclicBehaviour {
        public void action() {
            ACLMessage request = receive(MessageTemplate.MatchConversationId(Constants.MSG_ID_INSTANCE_REQUEST));

            if (request != null) {
                try {
                    ACLMessage response = new ACLMessage(ACLMessage.INFORM);
                    response.addReceiver(request.getSender());
                    response.setConversationId(Constants.MSG_ID_INSTANCE_INFORM);
                    response.setContentObject(this);
                    send(response);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                block();
            }
        }
    }
    public <T extends BaseAgent> T requestAgentInstance(AID agentAID, Class<T> agentClass) {
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(agentAID);
        request.setConversationId(Constants.MSG_ID_INSTANCE_REQUEST);
        send(request);
        ACLMessage response = blockingReceive(MessageTemplate.MatchConversationId(Constants.MSG_ID_INSTANCE_INFORM));
        if (response != null) {
            try {
                return agentClass.cast(response.getContentObject());
            } catch (Exception e) { }
        }
        return null;
    }
    public List<AID> findAgentsByType(String type) {
        // I really hate having it here
        List<AID> agentDescriptions = new ArrayList<>();

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            for (DFAgentDescription dfd : result) {
                agentDescriptions.add(dfd.getName());
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        return agentDescriptions;
    }

    // Position is continuously updated in the background.
    protected void moveToPosition(Location location, int timeInSeconds) {

        double distance = locationPin.getDistance(location);
        double speed = distance / timeInSeconds;

        // update interval
        long intervalInMillis = (long) (1000 / speed);
        double distancePerUpdate = speed * intervalInMillis / 1000;

        int numUpdates = (int) (speed / distancePerUpdate);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int updateCount = 0;

            public void run() {
                if (updateCount >= numUpdates) {
                    // Stop the timer after reaching the target position
                    timer.cancel();
                } else {
                    // Calculate the new position based on the distance covered per update
                    double deltaX = (location.getX() - locationPin.getX()) / (double) numUpdates;
                    double deltaY = (location.getY() - locationPin.getY()) / (double) numUpdates;
                    Location newLocation = new Location(locationPin.getX() + (int) deltaX, locationPin.getY() + (int) deltaY);

                    // Update the agent's location
                    locationPin.setLocation(newLocation);

                    //System.out.println(getLocalName() + ": Moving to (" + currentX + ", " + currentY + ")");

                    updateCount++;
                }
            }
        }, intervalInMillis, intervalInMillis);
    }

//    private void startPositionUpdate() {
//        updateTimer = new Timer();
//        updateTimer.scheduleAtFixedRate(new TimerTask() {
//            public void run() {
//                try {
//                    LocationMap locationMap = (LocationMap) Naming.lookup("rmi://localhost/locationMap");
//                    locationMap.updateLocationPin(BaseAgent.this.getLocalName(), locationPin);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 0, 1000); // Update every 1 seconds (adjust as needed)
//    }
//
//    private void stopPositionUpdate() {
//        if (updateTimer != null) {
//            updateTimer.cancel();
//        }
//    }

    public LocationPin getLocationPin() {
        return locationPin;
    }

    public void setLocationPin(LocationPin locationPin) {
        this.locationPin = locationPin;
    }

    public void locationUpdated(String agentName, LocationPin newLocationPin) throws RemoteException {
        return;
    }
}
