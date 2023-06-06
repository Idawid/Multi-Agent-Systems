package simulationUtils;

import mapUtils.Location;

import java.io.Serializable;

public class Task implements Serializable {
    private Location destination;
    private ProductType product;
    private int quantity;

    public Task(Location destination, ProductType product, int quantity) {
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
}
