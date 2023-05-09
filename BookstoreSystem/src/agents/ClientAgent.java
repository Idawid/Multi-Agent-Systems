package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import utils.Book;
import utils.Constants;
import utils.MyListSerializer;

import java.io.IOException;
import java.util.*;

public class ClientAgent extends Agent {
    private List<Book> desiredBooks;
    private double desiredPrice;
    private String desiredDate;
    private List<AID> sellerAgents;

    public ClientAgent(List<Book> desiredBook, double desiredPrice, String desiredDate) {
        this.desiredBooks = desiredBook;
        this.desiredPrice = desiredPrice;
        this.desiredDate = desiredDate;
    }

    protected void setup() {
        addBehaviour(new RequestBookstoreList());
    }

    private class RequestBookstoreList extends OneShotBehaviour {
        @Override
        public void action() {
            // Send a request for SellerAgents for the specified genre
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.addReceiver(new AID(Constants.AGENT_DF, AID.ISLOCALNAME));
            try {
                request.setContentObject(desiredBooks.get(0).getGenre());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            send(request);

            // Handle the response
            ACLMessage response = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            if (response != null) {
                sellerAgents = MyListSerializer.deserializeList(response.getContent());
                if (sellerAgents == null) {
                    System.err.println("Client couldn't deserialize seller agents from response.");
                    return;
                }
                sendCFP();
            }
            else {
                // TODO handle race in a better way
                addBehaviour(new RequestBookstoreList());
            }
        }
    }
    private void sendCFP() {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        for (AID sellerAgent : sellerAgents) {
            cfp.addReceiver(sellerAgent);
        }
        cfp.setContent("request-books:" + MyListSerializer.serializeList(desiredBooks) + ";desiredPrice:" + desiredPrice + ";desiredDate:" + desiredDate);

        cfp.setReplyByDate(new Date(System.currentTimeMillis() + 10000)); // 10 seconds
        // Add the behavior responsible for this CFP
        addBehaviour(new SendCallForProposal(cfp));
    }
    private class SendCallForProposal extends ContractNetInitiator {
        double price;
        public SendCallForProposal(ACLMessage cfp) {
            super(ClientAgent.this, cfp);
        }

        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            ACLMessage bestProposal = chooseBestProposal(responses);
            if (bestProposal != null) {
                ACLMessage accept = bestProposal.createReply();
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                accept.setContent("money-sent:" + price);
                acceptances.addElement(accept);
            } else {
                System.out.println("No suitable proposal found for the desired books.");
            }
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            String name = inform.getSender().getLocalName();
            String agentId = name.substring(Constants.AGENT_SELLER_PREFIX.length());
            System.out.println("Going to the bookstore number " + agentId);

            ContainerID targetContainer = new ContainerID();
            targetContainer.setName(Constants.CONTAINER_BOOKSTORE_PREFIX + agentId);
            targetContainer.setAddress("localhost");
            targetContainer.setPort("1099");
            doMove(targetContainer);

            System.out.println("Transaction completed: " + inform.getContent() + " with " + name);
        }

        private ACLMessage chooseBestProposal(Vector responses) {
            ACLMessage bestProposal = null;
            double bestPrice = Double.MAX_VALUE;
            String bestDeliveryDate = "";

            for (Object response : responses) {
                ACLMessage proposal = (ACLMessage) response;
                // Iterate only through proposals
                if (proposal.getPerformative() == ACLMessage.PROPOSE) {
                    String[] parts = proposal.getContent().split(",");
                    double price = Double.parseDouble(parts[0].split(":")[1]);
                    String deliveryDate = parts[1].split(":")[1];

                    if (price <= desiredPrice && (bestProposal == null || price < bestPrice || (price == bestPrice && deliveryDate.compareTo(bestDeliveryDate) < 0))) {
                        bestProposal = proposal;
                        bestPrice = price;
                        bestDeliveryDate = deliveryDate;
                    }
                }
            }
            price = bestPrice;
            return bestProposal;
        }
    }
}
