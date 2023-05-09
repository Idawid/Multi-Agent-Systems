package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BookInitializer {
    private static final Random random = new Random();

    public static List<Book> initializeBooks(BookType genre, int num) {
        List<Book> books = new ArrayList<>();
        String[] titles = {"The Great Gatsby", "1984", "The Elements of Style", "To Kill a Mockingbird", "Pride and Prejudice",
                "One Hundred Years of Solitude", "Moby-Dick", "Wuthering Heights", "The Catcher in the Rye",
                "The Lord of the Rings", "Animal Farm", "The Picture of Dorian Gray", "Brave New World",
                "The Adventures of Huckleberry Finn", "The Scarlet Letter", "Heart of Darkness",
                "The Count of Monte Cristo", "Frankenstein", "Dracula", "Alice's Adventures in Wonderland"};
        String[] authors = {"F. Scott Fitzgerald", "George Orwell", "William Strunk Jr.", "Harper Lee", "Jane Austen",
                "Gabriel García Márquez", "Herman Melville", "Emily Brontë", "J.D. Salinger", "J.R.R. Tolkien",
                "George Orwell", "Oscar Wilde", "Aldous Huxley", "Mark Twain", "Nathaniel Hawthorne",
                "Joseph Conrad", "Alexandre Dumas", "Mary Shelley", "Bram Stoker", "Lewis Carroll"};
        for (int i = 0; i < num; i++) {
            String title = titles[random.nextInt(titles.length)];
            String author = authors[random.nextInt(authors.length)];
            double price = 9.99 + random.nextInt(20);
            boolean availability = random.nextBoolean();
            String deliveryDate = "2023-" + (random.nextInt(11) + 1) + "-" + (random.nextInt(27) + 1);
            Book book = new Book(title, author, genre, price, availability, deliveryDate);
            books.add(book);
        }
        return books;
    }
}
