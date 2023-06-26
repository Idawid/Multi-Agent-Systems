package mapUtils.locationPin;

public class WarehouseData implements AgentData, HasStock {
    private int currentStock = 0;
    private int maxStock = 50;
    private int reservedStock = 0;

    public int getOrderedStock() {
        return orderedStock;
    }

    public void setOrderedStock(int orderedStock) {
        this.orderedStock = orderedStock;
    }

    private int orderedStock = 0;

    public WarehouseData() {
        new TruckData(0);
    }
    public WarehouseData(int maxStock) {
        this.reservedStock = 0;
        this.currentStock = 0;
        this.maxStock = maxStock;
    }

    @Override
    public int getCurrentStock() {
        return currentStock;
    }

    @Override
    public int getMaxStock() {
        return maxStock;
    }

    @Override
    public void addCurrentStock(int quantity) {
        if (quantity < 0) {
            int remainingQuantity = quantity;

            if (reservedStock >= Math.abs(quantity)) {
                reservedStock += quantity;
                return;
            } else {
                remainingQuantity += reservedStock;
                reservedStock = 0;
            }

            // Deduct from currentStock
            currentStock += remainingQuantity;
        } else {
            currentStock += quantity;
        }
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
    }

    @Override
    public void reserveStock(int quantity) {
        reservedStock += quantity;
        currentStock -= quantity;
    }
}
