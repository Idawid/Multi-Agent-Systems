package mapUtils.locationPin;

public interface HasStock {
    int getCurrentStock();
    int getMaxStock();
    void addCurrentStock(int quantity);
    void reserveStock(int quantity);
}
