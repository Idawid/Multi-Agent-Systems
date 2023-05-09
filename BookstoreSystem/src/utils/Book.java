package utils;

import java.io.Serializable;

public class Book implements Serializable {
    private String title;
    private String author;
    private BookType genre;
    private double price;
    private boolean availability;
    private String deliveryDate;

    public Book(String title, String author, BookType genre, double price, boolean availability, String deliveryDate) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.price = price;
        this.availability = availability;
        this.deliveryDate = deliveryDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public BookType getGenre() {
        return genre;
    }

    public void setGenre(BookType genre) {
        this.genre = genre;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getPrintingDate() {
        return deliveryDate;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%.2f,%s,%s",
                title, author, genre, price, availability, deliveryDate);
    }
}