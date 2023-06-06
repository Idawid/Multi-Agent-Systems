package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.ArrayList;
import java.util.List;

public class MainHub extends BaseAgent {
    private List<AID> warehouseAgents;

    protected void setup() {
        super.setup();
        warehouseAgents = new ArrayList<>();;
    }
    protected void takeDown() {
        super.takeDown();
    }
}
