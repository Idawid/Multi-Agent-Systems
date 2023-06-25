package mapUtils.locationPin;

import jade.core.Agent;

import java.io.Serializable;

public interface AgentData extends Serializable {
    static AgentData getByAgentClass(Class<? extends Agent> agentClass) {
        try {
            if (AgentDataProvider.class.isAssignableFrom(agentClass)) {
                AgentDataProvider agentInstance = (AgentDataProvider) agentClass.getDeclaredConstructor().newInstance();
                return agentInstance.getAgentData();
            } else {
                throw new IllegalArgumentException("Agent class does not implement AgentDataProvider");
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Failed to invoke getAgentData method", e);
        }
    }
}


