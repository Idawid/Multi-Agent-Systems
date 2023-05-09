package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.BookType;
import utils.MyListSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DirectoryFacilitatorAgent extends Agent {
    private Map<BookType, List<AID>> genreToBookstores;

    protected void setup() {
        genreToBookstores = new HashMap<>();

        addBehaviour(new RegisterBookstores());
        addBehaviour(new HandleClientRequests());
    }

    private class RegisterBookstores extends CyclicBehaviour {
        private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                BookType genre;
                try {
                    genre = (BookType) msg.getContentObject();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                AID sellerAgent = msg.getSender();

                genreToBookstores.putIfAbsent(genre, new ArrayList<>());
                genreToBookstores.get(genre).add(sellerAgent);
            } else {
                block();
            }
        }
    }

    private class HandleClientRequests extends CyclicBehaviour {
        private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                BookType requestedGenre;
                try {
                    requestedGenre = (BookType) msg.getContentObject();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                List<AID> sellerAgents = genreToBookstores.get(requestedGenre);

                ACLMessage reply = msg.createReply();
                if (sellerAgents != null && !sellerAgents.isEmpty()) {
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(MyListSerializer.serializeList(sellerAgents));
                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("No bookstores found for the requested genre.");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }
}
