package mapUtils.locationPin;

public class TruckData implements AgentData, HasPercentage {
    private double loadPercentage = 0;

    public TruckData() {
        new TruckData(0);
    }
    public TruckData(double loadPercentage) {
        this.loadPercentage = loadPercentage;
    }

    public double getLoadPercentage() {
        return loadPercentage;
    }

    public void setLoadPercentage(double loadPercentage) {
        this.loadPercentage = loadPercentage;
    }

    @Override
    public double getPercentage() {
        return loadPercentage;
    }
}
