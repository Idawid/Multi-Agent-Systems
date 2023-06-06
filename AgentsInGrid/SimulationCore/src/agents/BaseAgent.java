package agents;

import jade.core.Agent;
import utils.*;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import java.util.TimerTask;

public class BaseAgent extends Agent implements AgentTypeProvider, LocationMapObserver {
    private LocationPin locationPin;
    private Timer updateTimer;

    public BaseAgent(Location location) {
        super();
        this.locationPin = new LocationPin(location, this.getClass());
    }

    public BaseAgent() {
        super();
    }

    protected void init() {
        try {
            LocationMap locationMap = (LocationMap) Naming.lookup("rmi://localhost/locationMap");
            locationMap.addLocationPin(this.getLocalName(), locationPin);
            locationMap.registerObserver(new LocationMapObserverProxy(this));
            startPositionUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setup() {
        init();
    }

    protected void takeDown() {
        try {
            LocationMap locationMap = (LocationMap) Naming.lookup("rmi://localhost/locationMap");
            locationMap.removeLocationPin(this.getLocalName());
            locationMap.unregisterObserver(new LocationMapObserverProxy(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopPositionUpdate();
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

    private void startPositionUpdate() {
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    LocationMap locationMap = (LocationMap) Naming.lookup("rmi://localhost/locationMap");
                    locationMap.updateLocationPin(BaseAgent.this.getLocalName(), locationPin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000); // Update every 1 seconds (adjust as needed)
    }

    private void stopPositionUpdate() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }

    @Override
    public AgentType getAgentType() {
        return null;
    }

    public void locationUpdated(String agentName, LocationPin newLocationPin) throws RemoteException {

    }
}
