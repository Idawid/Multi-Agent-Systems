package utils;

import jade.core.Agent;

import java.io.Serial;
import java.io.Serializable;
import java.rmi.Remote;

public class LocationPin extends Location implements Serializable {
    private AgentType agentType;

    public LocationPin(int x, int y, Class<? extends Agent> agentClass) {
        super(x, y);
        this.agentType = AgentType.getByAgentClass(agentClass);
    }

    public LocationPin(Location location, Class<? extends Agent> agentClass) {
        super(location.getX(), location.getY());
        this.agentType = AgentType.getByAgentClass(agentClass);
    }

    public LocationPin(int x, int y, AgentType agentType) {
        super(x, y);
        this.agentType = agentType;
    }

    public LocationPin(Location location, AgentType agentType) {
        super(location.getX(), location.getY());
        this.agentType = agentType;
    }

    public AgentType getAgentType() {
        return agentType;
    }
}
