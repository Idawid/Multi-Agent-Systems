package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import utils.BookType;
import utils.Constants;

public class SellerAgent extends Agent {
    private AID managerAgentAID;
    private BookType genre;

    public SellerAgent(AID managerAgentAID, BookType genre) {
        this.managerAgentAID = managerAgentAID;
        this.genre = genre;
    }

    protected void setup() {
        // Register itself
        addBehaviour(new RegisterWithDirectoryFacilitator());

        // Handle CFP from ClientAgent
        MessageTemplate cfpTemplate = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        addBehaviour(new HandleCallForProposal(cfpTemplate));
    }

    private class RegisterWithDirectoryFacilitator extends OneShotBehaviour {
        @Override
        public void action() {
            ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
            inform.addReceiver(getAID(Constants.AGENT_DF));
            try {
                inform.setContentObject(genre);
            }
            catch (Exception e) {
                e.printStackTrace();
                return;
            }
            myAgent.send(inform);
        }
    }

    private class HandleCallForProposal extends ContractNetResponder {
        public HandleCallForProposal(MessageTemplate cfpTemplate) {
            super(SellerAgent.this, cfpTemplate);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) {
            // Forward the CFP to the ManagerAgent and handle the response
            ACLMessage request = new ACLMessage(ACLMessage.CFP);
            request.addReceiver(managerAgentAID);
            request.setContent(cfp.getContent());
            send(request);

            ACLMessage managerReply = blockingReceive();
            ACLMessage reply = cfp.createReply();

            if (managerReply.getPerformative() == ACLMessage.PROPOSE) {
                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContent(managerReply.getContent());
            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("Book not available");
            }
            return reply;
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            inform.setContent("Transaction completed");
            send(inform);
            return null;
        }

        @Override
        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            // Do nothing
        }
    }
}
