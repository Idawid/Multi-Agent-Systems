package utils;

import agents.RetailerAgent;
import agents.TruckAgent;

import java.io.Serializable;

public class Task implements Serializable {
    private RetailerAgent destination;
    private String product;
    private int quantity;

    public Task(RetailerAgent destination, String product, int quantity) {
        this.destination = destination;
        this.product = product;
        this.quantity = quantity;
    }

    public RetailerAgent getDestination() {
        return destination;
    }

    public void setDestination(RetailerAgent destination) {
        this.destination = destination;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
