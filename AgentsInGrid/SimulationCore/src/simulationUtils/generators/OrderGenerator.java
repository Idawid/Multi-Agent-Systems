package simulationUtils.generators;

import jade.core.AID;
import mapUtils.locationPin.Location;
import simulationUtils.task.ProductType;
import simulationUtils.Order;

import java.util.Random;

public class OrderGenerator {
    private static final ProductType[] PRODUCTS = ProductType.values();
    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 10;

    public static Order generateRandomOrder(Location destination, AID retailerAID) {
        Random random = new Random();

        ProductType randomProduct = getRandomProduct(random);
        int randomQuantity = getRandomQuantity(random);

        Order order = new Order(destination, randomProduct, randomQuantity, retailerAID);

        return order;
    }

    private static ProductType getRandomProduct(Random random) {
        int index = random.nextInt(PRODUCTS.length);
        return PRODUCTS[index];
    }

    private static int getRandomQuantity(Random random) {
        return random.nextInt(MAX_QUANTITY - MIN_QUANTITY + 1) + MIN_QUANTITY;
    }
}
