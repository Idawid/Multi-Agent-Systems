package containers;

import agents.RetailerAgent;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import simulationUtils.Constants;
import utils.Location;

import java.util.List;

public class RetailerContainer {
    private static int idCounter = 0;
    private ContainerController retailerContainer;

    public RetailerContainer(String containerName, List<Location> retailerLocations) {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.MAIN_PORT, "1098");
        p.setParameter(Profile.CONTAINER_NAME, containerName);
        retailerContainer = rt.createAgentContainer(p);

        // Retailers
        for (Location location : retailerLocations) {
            RetailerAgent retailerAgent = new RetailerAgent(location);
            addAgent(Constants.AGENT_RETAIL_PREFIX + idCounter, retailerAgent);
            idCounter++;
        }
    }

    public void addAgent(String agentName, Agent agent) {
        try {
            AgentController agentController = retailerContainer.acceptNewAgent(agentName, agent);
            agentController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
