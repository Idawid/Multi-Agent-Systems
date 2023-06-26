package simulationUtils.task;

import java.io.Serializable;

public enum ProductType implements Serializable {
    OIL(5.0),
    ORE(3.0);

    private double rate;

    ProductType(double rate) {
        this.rate = rate;
    }

    public double getRate() {
        return rate;
    }
}