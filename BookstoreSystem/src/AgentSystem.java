import agents.ClientAgent;
import agents.DirectoryFacilitatorAgent;
import containers.BookstoreContainer;
import containers.MainContainer;
import utils.Book;
import utils.BookInitializer;
import utils.Constants;

import java.util.List;
import jade.core.Agent;
import java.util.List;

public class AgentSystem {
    public static void main(String[] args) {

        // Initialize the main container and agents
        MainContainer mainContainer = new MainContainer();

        // Set up the Directory Facilitator agent
        DirectoryFacilitatorAgent directoryFacilitatorAgent = new DirectoryFacilitatorAgent();
        mainContainer.addAgent("directoryFacilitatorAgent", directoryFacilitatorAgent);

        // Load books data for different genres
        List<Book> itBooks1 = BookInitializer.initializeBooks(Constants.GENRE_IT, 60);
        List<Book> itBooks2 = BookInitializer.initializeBooks(Constants.GENRE_IT, 10);
        List<Book> itBooks3 = BookInitializer.initializeBooks(Constants.GENRE_IT, 10);
        List<Book> scienceBooks1 = BookInitializer.initializeBooks(Constants.GENRE_SCIENCE, 10);
        List<Book> scienceBooks2 = BookInitializer.initializeBooks(Constants.GENRE_SCIENCE, 10);

        // Create bookstores with different genres and lists of books
        BookstoreContainer bookstore1 = new BookstoreContainer(Constants.CONTAINER_BOOKSTORE_PREFIX, 1, Constants.GENRE_IT, itBooks1);
        BookstoreContainer bookstore2 = new BookstoreContainer(Constants.CONTAINER_BOOKSTORE_PREFIX, 2, Constants.GENRE_IT, itBooks2);
        BookstoreContainer bookstore3 = new BookstoreContainer(Constants.CONTAINER_BOOKSTORE_PREFIX, 3, Constants.GENRE_IT, itBooks3);
        BookstoreContainer bookstore4 = new BookstoreContainer(Constants.CONTAINER_BOOKSTORE_PREFIX, 4, Constants.GENRE_SCIENCE, scienceBooks1);
        BookstoreContainer bookstore5 = new BookstoreContainer(Constants.CONTAINER_BOOKSTORE_PREFIX, 5, Constants.GENRE_SCIENCE, scienceBooks2);

        // Set up our Client
        List<Book> requestedBooks = BookInitializer.initializeBooks(Constants.GENRE_IT, 1);
        ClientAgent clientAgent = new ClientAgent(requestedBooks, 1000.0,"2024-01-01");
        mainContainer.addAgent("clientAgent", clientAgent);
    }
}
