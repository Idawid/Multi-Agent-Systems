package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Book;
import utils.BookType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManagerAgent extends Agent {

    private List<Book> availableBooks;

    @Override
    protected void setup() {
        // Initialize the list of available books
        initializeBooks();

        // Handle requests for book availability and price
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage request = myAgent.receive(mt);

                if (request != null) {
                    String requestedTitle = request.getContent();
                    List<Book> matchingBooks = findMatchingBooks(requestedTitle);

                    if (!matchingBooks.isEmpty()) {
                        // Assume the first matching book is the one requested
                        Book requestedBook = matchingBooks.get(0);
                        double price = calculatePrice(requestedBook);

                        ACLMessage response = request.createReply();
                        response.setPerformative(ACLMessage.INFORM);
                        response.setContent("Title: " + requestedBook.getTitle() + ", Price: " + price);
                        send(response);
                    } else {
                        ACLMessage response = request.createReply();
                        response.setPerformative(ACLMessage.FAILURE);
                        response.setContent("Book not available");
                        send(response);
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void initializeBooks() {
        availableBooks = new ArrayList<>();
        availableBooks.add(new Book("Java for Beginners", "John Doe", BookType.IT));
        availableBooks.add(new Book("Advanced Java", "Jane Smith", BookType.IT));
        // Add more books as needed
    }

    private List<Book> findMatchingBooks(String title) {
        return availableBooks.stream()
                .filter(book -> book.getTitle().equalsIgnoreCase(title))
                .collect(Collectors.toList());
    }

    private double calculatePrice(Book book) {
        // Implement your price calculation logic here
        // This is a simple example that sets a fixed price based on the book type
        if (book.getBookType() == BookType.IT) {
            return 25.0;
        } else {
            return 20.0;
        }
    }
}