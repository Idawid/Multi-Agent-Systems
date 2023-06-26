package mapUtils.locationPin;

public class MainHubData implements AgentData, HasStock {

    private final int maxWarehouseCapacity;

    public MainHubData(int maxWarehouseCapacity) {
        this.maxWarehouseCapacity = maxWarehouseCapacity;
    }

    @Override
    public int getCurrentStock() {
        return 5 * maxWarehouseCapacity;
    }

    @Override
    public int getMaxStock() {
        return 5 * maxWarehouseCapacity;
    }

    @Override
    public void addCurrentStock(int quantity) {

    }

    @Override
    public void reserveStock(int quantity) {

    }
}
