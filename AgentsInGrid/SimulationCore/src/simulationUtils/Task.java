package simulationUtils;

import jade.core.AID;
import mapUtils.Location;

import java.io.Serializable;

public class Task implements Serializable {
    private Location destination;
    private ProductType product;
    private int quantity;
    private AID retailerAID;

    public Task(Location destination, ProductType product, int quantity, AID retailerAID) {
        this.destination = destination;
        this.product = product;
        this.quantity = quantity;
        this.retailerAID = retailerAID;
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
