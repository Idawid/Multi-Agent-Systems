package containers;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class MainContainer {
    private AgentContainer mainContainer;

    public MainContainer(String containerName) {
        // Initialize main container
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.MAIN_PORT, "1099");
        p.setParameter(Profile.GUI, "true");
        p.setParameter(Profile.CONTAINER_NAME, containerName);
        mainContainer = rt.createMainContainer(p);
    }

    public void addAgent(String agentName, Agent agent) {
        try {
            AgentController agentController = mainContainer.acceptNewAgent(agentName, agent);
            agentController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
