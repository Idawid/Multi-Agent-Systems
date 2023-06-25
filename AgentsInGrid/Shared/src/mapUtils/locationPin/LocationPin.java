package mapUtils.locationPin;

import jade.core.Agent;

import java.io.Serializable;

public class LocationPin extends Location implements Serializable {
    private AgentType agentType;
    private AgentData agentData;

    public LocationPin(int x, int y, Class<? extends Agent> agentClass) {
        this(new Location(x, y), agentClass);
    }

    public LocationPin(int x, int y, AgentType agentType) {
        super(x, y);
        this.agentType = agentType;
        this.agentData = null;
    }

    public LocationPin(Location location, AgentType agentType) {
        super(location);
        this.agentType = agentType;
        this.agentData = null;
    }

    public LocationPin(Location location, Class<? extends Agent> agentClass) {
        super(location); // this is beautiful
        this.agentType = AgentType.getByAgentClass(agentClass);
        this.agentData = AgentData.getByAgentClass(agentClass);
    }

    public LocationPin(Location location, AgentType agentType, AgentData agentData) {
        super(location);
        this.agentType = agentType;
        this.agentData = agentData;
    }

    public LocationPin(LocationPin locationPin) {
        super(locationPin.getLocation());
        this.agentType = locationPin.getAgentType();
        this.agentData = locationPin.getAgentData();
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public AgentData getAgentData() {
        return agentData;
    }
}
