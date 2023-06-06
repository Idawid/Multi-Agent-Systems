package simulationUtils;

import agents.RetailerAgent;
import utils.Location;

import java.io.Serializable;

public class Task implements Serializable {
    private Location destination;
    private String product;
    private int quantity;

    public Task(Location destination, String product, int quantity) {
        this.destination = destination;
        this.product = product;
        this.quantity = quantity;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
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
