package containers;

import agents.WarehouseAgent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.List;

public class WarehouseContainer {
    private ContainerController container;
    private List<WarehouseAgent> agents = new ArrayList<>();

    public WarehouseContainer(ContainerController container) {
        this.container = container;
    }

    public void createAgents(int numAgents) {
        for (int i = 0; i < numAgents; i++) {
            try {
                Object[] args = {};  // arguments for agent initialization
                AgentController agent = container.createNewAgent("WarehouseAgent" + i, "com.yourcompany.agents.WarehouseAgent", args);
                agents.add((WarehouseAgent) agent);
                agent.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }
}
