package mapUtils.locationPin;

public class RetailerData implements AgentData, HasLabel, HasStock {
    private double profits;

    public double getProfit() {
        return profits;
    }

    public void setProfit(double profits) {
        this.profits = profits;
    }

    @Override
    public String getLabel() {
        return "Profits: $" + String.format("%.2f", profits);
    }

    @Override
    public int getCurrentStock() {
        return 0;
    }

    @Override
    public int getMaxStock() {
        return 0;
    }

    @Override
    public void addCurrentStock(int quantity) {
        profits += quantity;
    }

    @Override
    public void reserveStock(int quantity) {
        return;
    }
}