package agents;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import utils.Book;
import utils.BookType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class ClientAgent extends Agent {

    private BookType desiredBookType;
    private List<Book> booksToOrder;

    @Override
    protected void setup() {
        // Initialize desired book type and books to order
        desiredBookType = BookType.IT; // You can change this to the desired book type
        booksToOrder = new ArrayList<>();
        booksToOrder.add(new Book("Book Title 1", "Author 1", desiredBookType));

        // Search for SellerAgents offering the desired book type
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType(desiredBookType.toString());
                template.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    if (result.length > 0) {
                        System.out.println("Found " + result.length + " SellerAgents offering " + desiredBookType);
                        sendCallForProposals(result);
                    } else {
                        System.out.println("No SellerAgents found offering " + desiredBookType);
                    }
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendCallForProposals(DFAgentDescription[] sellerAgents) {
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (DFAgentDescription agent : sellerAgents) {
            msg.addReceiver(agent.getName());
        }

        msg.setContent(booksToOrder.toString()); // Set the content of the message as the list of books to order
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000)); // Set reply deadline

        addBehaviour(new ContractNetInitiator(this, msg) {
            @Override
            protected void handleAllResponses(Vector responses, Vector acceptances) {
                // Evaluate proposals and select the best one
                // You can implement your own selection criteria here
                ACLMessage bestOffer = null;
                double bestPrice = Double.MAX_VALUE;

                for (Object responseObj : responses) {
                    ACLMessage response = (ACLMessage) responseObj;
                    if (response.getPerformative() == ACLMessage.PROPOSE) {
                        double price = Double.parseDouble(response.getContent());
                        if (price < bestPrice) {
                            bestPrice = price;
                            bestOffer = response;
                        }
                    }
                }

                if (bestOffer != null) {
                    ACLMessage accept = bestOffer.createReply();
                    accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    acceptances.add(accept);
                }
            }

            @Override
            protected void handleInform(ACLMessage inform) {
                System.out.println("Successfully ordered the book(s) from " + inform.getSender().getLocalName());
            }
        });
    }
}