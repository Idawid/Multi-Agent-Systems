package mapUtils.locationPin;

public class WarehouseData implements AgentData, HasPercentage {
    private double stockPercentage = 0;

    public WarehouseData() {
        new WarehouseData(0);
    }

    public WarehouseData(double stockPercentage) {
        this.stockPercentage = stockPercentage;
    }

    public double getStockPercentage() {
        return stockPercentage;
    }

    public void setStockPercentage(double stockPercentage) {
        this.stockPercentage = stockPercentage;
    }

    @Override
    public double getPercentage() {
        return stockPercentage;
    }
}
