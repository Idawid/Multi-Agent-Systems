package utils;

public interface AgentTypeProvider {
    default AgentType getAgentType() {
        return null;
    }
}
