package mapUtils.locationPin;

public class TruckData extends IsMoveable implements AgentData, HasStock {
    private int currentLoad = 0;
    private int maxLoad = 10;

    public TruckData() {
        new TruckData(0);
    }
    public TruckData(int maxLoad) {
        this.currentLoad = 0;
        this.maxLoad = maxLoad;
    }

    @Override
    public int getCurrentStock() {
        return currentLoad;
    }

    @Override
    public int getMaxStock() {
        return maxLoad;
    }

    @Override
    public void addCurrentStock(int quantity) {
        this.currentLoad += quantity;
    }

    @Override
    public void reserveStock(int quantity) {
        return;
    }
}
