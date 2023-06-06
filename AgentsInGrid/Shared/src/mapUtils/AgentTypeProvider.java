package mapUtils;

public interface AgentTypeProvider {
    default AgentType getAgentType() {
        return null;
    }
}
