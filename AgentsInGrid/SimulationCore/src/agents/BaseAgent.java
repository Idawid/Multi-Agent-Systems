package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import mapUtils.*;
import mapUtils.locationPin.AgentData;
import mapUtils.locationPin.Location;
import mapUtils.locationPin.LocationPin;
import simulationUtils.Constants;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serial;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public abstract class BaseAgent extends Agent implements LocationMapObserver, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getMyLogger(BaseAgent.class.getName());
    private LocationPin locationPin;
    private ContainerID containerID;
    private String address;
    private String port;

    public BaseAgent(Location location) {
        super();
        this.locationPin = new LocationPin(location, this.getClass());
    }
    public BaseAgent() { }
    protected void setup() {
        init();
        addBehaviour(new HandleRequestBehaviour());
    }
    private void init() {
        try {
            registerWithDF();
            registerWithLocationMap();
            initAgentNetworkAttributes();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    protected void takeDown() {
        try {
            deregisterFromDF();
            deregisterFromLocationMap();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    private void initAgentNetworkAttributes() {
        // Properties are transient, they have to be pulled up to BaseAgent
        String containerName = this.getProperty("container-name", null);
        String address = this.getProperty("host", null);
        String port = this.getProperty("port", null);

        ContainerID containerID = new ContainerID();
        containerID.setName(containerName);
        containerID.setAddress(address);
        containerID.setPort(port);

        this.containerID = containerID;
        this.address = address;
        this.port = port;
    }
    public void setAgentNetworkAttributes(BaseAgent networkAgent) {
        this.containerID = networkAgent.containerID;
        this.address = networkAgent.address;
        this.port = networkAgent.port;
    }
    public void doMove(BaseAgent targetAgent) {
        super.doMove(targetAgent.containerID);
        setAgentNetworkAttributes(targetAgent);
    }
    private void registerWithLocationMap() throws MalformedURLException, NotBoundException, RemoteException {
        LocationMap locationMap = (LocationMap) Naming.lookup(LocationMap.REMOTE_LOCATION_MAP_ENDPOINT);
        locationMap.addLocationPin(this.getLocalName(), locationPin);
        locationMap.registerObserver(new LocationMapObserverProxy(this));
    }
    private void deregisterFromLocationMap() throws MalformedURLException, NotBoundException, RemoteException {
        LocationMap locationMap = (LocationMap) Naming.lookup(LocationMap.REMOTE_LOCATION_MAP_ENDPOINT);
        locationMap.removeLocationPin(this.getLocalName());
        locationMap.unregisterObserver(new LocationMapObserverProxy(this));
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
                    response.setContentObject(myAgent);
                    send(response);
                } catch (IOException e) {
                    if (e instanceof NotSerializableException) {
                        logger.log(Logger.SEVERE, "Object of class " + myAgent.getClass().getSimpleName() +
                                "is not serializable! Make sure all fields of the object are serializable.", e);
                    }
                    throw new RuntimeException(e);
                }
            } else {
                block();
            }
        }
    }
    public List<? extends BaseAgent> findAgentsByClass(Class<? extends BaseAgent> agentClass) {
        List<BaseAgent> agentInstances = new ArrayList<>();

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(agentClass.getSimpleName());
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            for (DFAgentDescription dfd : result) {
                AID agentAID = dfd.getName();
                Object agentInstance = requestAgentInstance(agentAID);
                if (agentClass.isInstance(agentInstance)) {
                    agentInstances.add(agentClass.cast(agentInstance));
                }
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        return agentInstances;
    }
    public Object requestAgentInstance(AID agentAID) {
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(agentAID);
        request.setConversationId(Constants.MSG_ID_INSTANCE_REQUEST);
        send(request);
        System.out.println("Start block receive for: " + agentAID.getLocalName());
        ACLMessage response = blockingReceive(MessageTemplate.MatchConversationId(Constants.MSG_ID_INSTANCE_INFORM));
        System.out.println("End block receive for: " + agentAID.getLocalName());
        if (response != null) {
            try {
                return response.getContentObject();
            } catch (Exception e) { }
        }
        return null;
    }

    protected void moveToPosition(Location targetLocation) {
        LocationPin startLocation = new LocationPin(locationPin);
        LocationPin tempLocation = new LocationPin(locationPin);
        String keyName = getLocalName();

        int timeInMilliseconds = (int) (startLocation.getDistance(targetLocation) * 1000) / 60;
        int totalSteps = timeInMilliseconds * LocationMap.UPDATES_PER_SECOND / 1000 ;
        int currentSteps = 0;
        double stepX = ((double) targetLocation.getX() - startLocation.getX()) / totalSteps;
        double stepY = ((double) targetLocation.getY() - startLocation.getY()) / totalSteps;

        for (int i = 0; i < totalSteps; i++) {
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

    private void updateLocationPinNonBlocking(String agentName, LocationPin location) {
        CompletableFuture.runAsync(() -> {
            try {
                LocationMap locationMap = (LocationMap) Naming.lookup(LocationMap.REMOTE_LOCATION_MAP_ENDPOINT);
                locationMap.updateLocationPin(agentName, location);
            } catch (Exception e) {
                logger.log(Logger.WARNING, "Failed to update location on the remote map.");
            }
        });
    }

    public LocationPin getLocationPin() {
        return locationPin;
    }

    public void setLocationPin(LocationPin locationPin) {
        this.locationPin = locationPin;
    }

    public Location getLocation() {
        return locationPin;
    }

    public void setLocation(Location location) {
        this.locationPin = new LocationPin(location, this.getClass());
    }

    public ContainerID getContainerID() {
        return containerID;
    }

    public String getAddress() {
        return address;
    }

    public String getPort() {
        return port;
    }

    public void locationUpdated(String agentName, LocationPin newLocationPin) throws RemoteException {
        // Agents don't handle LocationMap updates for now
        return;
    }
}
