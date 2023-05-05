package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import utils.Book;
import utils.BookType;

import java.util.List;

public class SellerAgent extends Agent {

    private AID managerAgent;
    private BookType offeredBookType;

    @Override
    protected void setup() {
        // Initialize offered book type and manager agent
        offeredBookType = BookType.IT; // You can change this to the offered book type
        managerAgent = new AID("manager1", AID.ISLOCALNAME); // You can change this to the appropriate manager agent

        // Register the service with the Directory Facilitator
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(offeredBookType.toString());
        sd.setName("book-selling");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // Respond to Call for Proposals
        addBehaviour(new ContractNetResponder(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)) {

            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) {
                // Send request to ManagerAgent to check for book availability and price
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                request.addReceiver(managerAgent);
                request.setContent(cfp.getContent());
                send(request);

                // Receive ManagerAgent response
                ACLMessage managerResponse = blockingReceive();

                if (managerResponse != null && managerResponse.getPerformative() == ACLMessage.INFORM) {
                    ACLMessage propose = cfp.createReply();
                    propose.setPerformative(ACLMessage.PROPOSE);
                    propose.setContent(managerResponse.getContent());
                    return propose;
                } else {
                    ACLMessage refuse = cfp.createReply();
                    refuse.setPerformative(ACLMessage.REFUSE);
                    return refuse;
                }
            }

            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
            }

            @Override
            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                System.out.println("Proposal rejected by " + reject.getSender().getLocalName());
            }
        });
    }

    @Override
    protected void takeDown() {
        // Deregister the service from the Directory Facilitator
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}