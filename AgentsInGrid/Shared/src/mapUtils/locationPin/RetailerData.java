package mapUtils.locationPin;

import mapUtils.locationPin.AgentData;

public class RetailerData implements AgentData, HasLabel {
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
}