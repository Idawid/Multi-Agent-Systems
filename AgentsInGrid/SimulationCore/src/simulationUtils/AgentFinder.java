package simulationUtils;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.Arrays;

public class AgentFinder {
    public static AID[] findAgentsByType(Agent agent, String type) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(agent, template);
            return Arrays.stream(result)
                    .map(DFAgentDescription::getName)
                    .toArray(AID[]::new);
        } catch (FIPAException e) {
            e.printStackTrace();
            return new AID[0];
        }
    }
}