package utils;

import jade.core.Agent;

public enum AgentType {
    AGENT_RETAILER,
    AGENT_TRUCK,
    AGENT_WAREHOUSE;

    private final Class<? extends Agent> agentClass;

    AgentType(Class<? extends Agent> agentClass) {
        this.agentClass = agentClass;
    }

    AgentType() { agentClass = null; }

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
