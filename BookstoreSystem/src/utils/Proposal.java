package utils;

import jade.core.AID;

public class Proposal {
    private AID sellerAgent;
    private double price;
    private boolean isAvailable;
    private boolean isPrintable;
    private String completionDate;

    public Proposal(AID sellerAgent, double price, boolean isAvailable, boolean isPrintable, String completionDate) {
        this.sellerAgent = sellerAgent;
        this.price = price;
        this.isAvailable = isAvailable;
        this.isPrintable = isPrintable;
        this.completionDate = completionDate;
    }

    public AID getSellerAgent() {
        return sellerAgent;
    }

    public void setSellerAgent(AID sellerAgent) {
        this.sellerAgent = sellerAgent;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean isPrintable() {
        return isPrintable;
    }

    public void setPrintable(boolean printable) {
        isPrintable = printable;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }

    @Override
    public String toString() {
        return "Proposal{" +
                "sellerAgent=" + sellerAgent +
                ", price=" + price +
                ", isAvailable=" + isAvailable +
                ", isPrintable=" + isPrintable +
                ", completionDate='" + completionDate + '\'' +
                '}';
    }
}
