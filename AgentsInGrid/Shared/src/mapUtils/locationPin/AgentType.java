package mapUtils.locationPin;

import jade.core.Agent;

import java.io.Serializable;

public enum AgentType implements Serializable {
    AGENT_RETAILER,
    AGENT_TRUCK,
    AGENT_WAREHOUSE,
    AGENT_MAIN_HUB;

    public static AgentType getByAgentClass(Class<? extends Agent> agentClass) {
        try {
            if (AgentTypeProvider.class.isAssignableFrom(agentClass)) {
                AgentTypeProvider agentInstance = (AgentTypeProvider) agentClass.getDeclaredConstructor().newInstance();
                return agentInstance.getAgentType();
            } else {
                throw new IllegalArgumentException("Agent class does not implement AgentTypeProvider");
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Failed to invoke getAgentType method", e);
        }
    }
}
