package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.BookType;

public class DirectoryFacilitator extends Agent {

    @Override
    protected void setup() {
        // Register as a Directory Facilitator with the default DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("directory-facilitator");
        sd.setName("bookstore-directory");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Handle incoming requests for bookstores
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    BookType requestedBookType = BookType.valueOf(msg.getContent());
                    sendBookstoreList(msg, requestedBookType);
                } else {
                    block();
                }
            }
        });
    }

    private void sendBookstoreList(ACLMessage request, BookType bookType) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(bookType.toString());
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            ACLMessage reply = request.createReply();
            if (result.length > 0) {
                StringBuilder builder = new StringBuilder();
                for (DFAgentDescription agent : result) {
                    builder.append(agent.getName().toString());
                    builder.append(";");
                }
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(builder.toString());
            } else {
                reply.setPerformative(ACLMessage.FAILURE);
                reply.setContent("No bookstores found for the requested book type.");
            }
            send(reply);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        // Deregister from the default DF
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}