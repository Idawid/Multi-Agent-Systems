package containers;

import agents.ManagerAgent;
import agents.SellerAgent;
import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import utils.Book;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookstoreContainer {
    private AgentContainer bookstoreContainer;
    public BookstoreContainer(String containerName, int id, String genre, List<Book> books) {
        // Initialize bookstore container
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.MAIN_PORT, "1099");
        p.setParameter(Profile.CONTAINER_NAME, containerName + id);
        p.setParameter(Profile.GUI, "true");
        bookstoreContainer = rt.createAgentContainer(p);

        // Manager
        Map<String, Book> bookMap = new HashMap<>();
        for (Book book : books) {
            bookMap.put(book.getTitle(), book);
        }
        ManagerAgent managerAgent = new ManagerAgent(bookMap);
        addAgent("managerAgent" + id, managerAgent);

        // Seller
        AID managerAgentAID = new AID("managerAgent" + id, AID.ISLOCALNAME);
        SellerAgent sellerAgent = new SellerAgent(managerAgentAID, genre);
        addAgent("sellerAgent" + id, sellerAgent);
    }

    public void addAgent(String agentName, Agent agent) {
        try {
            AgentController agentController = bookstoreContainer.acceptNewAgent(agentName, agent);
            agentController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
