package utils;

import agents.RetailerAgent;
import agents.TruckAgent;
import agents.WarehouseAgent;
import jade.core.Agent;

public enum AgentType {
    AGENT_RETAILER(RetailerAgent.class),
    AGENT_TRUCK(TruckAgent.class),
    AGENT_WAREHOUSE(WarehouseAgent.class);

    private final Class<? extends Agent> agentClass;

    AgentType(Class<? extends Agent> agentClass) {
        this.agentClass = agentClass;
    }

    public Class<? extends Agent> getAgentClass() {
        return agentClass;
    }

    public static AgentType getByAgentClass(Class<? extends Agent> agentClass) {
        for (AgentType agentType : AgentType.values()) {
            if (agentType.getAgentClass().equals(agentClass)) {
                return agentType;
            }
        }
        throw new IllegalArgumentException("No matching AgentType found for the provided agentClass");
    }
}
