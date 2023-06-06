package utils;

import jade.core.Agent;

import java.io.Serializable;
import java.lang.reflect.Method;

public enum AgentType implements Serializable {
    AGENT_RETAILER,
    AGENT_TRUCK,
    AGENT_WAREHOUSE;

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
