package agents;

import mapUtils.locationPin.*;

public class MechanicAgent extends BaseAgent implements AgentTypeProvider, AgentDataProvider {

    public MechanicAgent(Location location) {
        super(location);
    }

    public MechanicAgent() { }

    @Override
    public AgentData getAgentData() {
        return null;
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.AGENT_MECHANIC;
    }
}
