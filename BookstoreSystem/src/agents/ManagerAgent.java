package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Book;
import utils.MyListSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerAgent extends Agent {
    private Map<String, Book> books;

    public ManagerAgent(Map<String, Book> books) {
        this.books = books;
    }

    protected void setup() {
        addBehaviour(new HandleCallForProposal());
    }

    private class HandleCallForProposal extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                // Parse the CFP message content
                String content = msg.getContent();
                String[] parts = content.split(";");
                List<String> deserializedRequestedBooks = (List<String>)MyListSerializer.deserialize(parts[0].split(":")[1]);
                List<Book> requestedBooks = new ArrayList<>();
                for (String bookStr : deserializedRequestedBooks) {
                    requestedBooks.add(Book.fromString(bookStr));
                }
                double desiredPrice = Double.parseDouble(parts[1].split(":")[1]);
                String desiredDate = parts[2].split(":")[1];

                // Prepare the proposal
                ACLMessage reply = msg.createReply();
                List<Book> availableBooks = checkBooksAvailability(requestedBooks);
                double totalPrice = calculateTotalPrice(availableBooks);
                String estimatedDeliveryDate = estimateDeliveryDate(availableBooks);

                if (!availableBooks.isEmpty() && totalPrice <= desiredPrice && estimatedDeliveryDate.compareTo(desiredDate) <= 0) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent("price:" + totalPrice + ",deliveryDate:" + estimatedDeliveryDate);
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("book-unavailable");
                }

                myAgent.send(reply);
            } else {
                block();
            }
        }
        public String estimateDeliveryDate(List<Book> availableBooks) {
            String latestDeliveryDate = null;
            for (Book book : availableBooks) {
                String deliveryDate = book.getDeliveryDate();
                if (latestDeliveryDate == null || deliveryDate.compareTo(latestDeliveryDate) > 0) {
                    latestDeliveryDate = deliveryDate;
                }
            }
            return latestDeliveryDate;
        }

        public double calculateTotalPrice(List<Book> availableBooks) {
            double totalPrice = 0.0;
            for (Book book : availableBooks) {
                totalPrice += book.getPrice();
            }
            return totalPrice;
        }

        private List<Book> checkBooksAvailability(List<Book> requestedBooks) {
            List<Book> availableBooks = new ArrayList<>();
            for (Book requestedBook : requestedBooks) {
                Book book = books.get(requestedBook.getTitle());
                if (book != null && book.isAvailability()) {
                    availableBooks.add(book);
                }
            }
            return availableBooks;
        }
    }
}