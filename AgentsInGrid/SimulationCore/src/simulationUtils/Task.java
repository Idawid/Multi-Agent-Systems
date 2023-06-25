package simulationUtils;

import jade.core.AID;
import mapUtils.locationPin.Location;

import java.io.Serializable;
import java.util.UUID;

public class Task implements Serializable {
    private final UUID id;
    private Location destination;
    private ProductType product;
    private int quantity;
    private AID retailerAID;

    public Task(Location destination, ProductType product, int quantity, AID retailerAID) {
        this.id = UUID.randomUUID();
        this.destination = destination;
        this.product = product;
        this.quantity = quantity;
        this.retailerAID = retailerAID;
    }

    public UUID getId() {
        return id;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public ProductType getProduct() {
        return product;
    }

    public void setProduct(ProductType product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public AID getRetailerAID() {
        return retailerAID;
    }

    public void setRetailerAID(AID retailerAID) {
        this.retailerAID = retailerAID;
    }
}
