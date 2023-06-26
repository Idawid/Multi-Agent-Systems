package simulationUtils.task;

import mapUtils.locationPin.HasStock;
import mapUtils.locationPin.IsMoveable;
import mapUtils.locationPin.LocationPin;
import mapUtils.locationPin.MovementStatus;
import simulationUtils.LocationMapUtils;

public class TransferStockStep extends Step {
    private int quantity;
    private TransferType transferType;

    public TransferStockStep(String initiator, String subject, int quantity, TransferType transferType) {
        super(initiator, subject);
        this.quantity = quantity;
        this.transferType = transferType;
    }

    @Override
    public void perform()  {
        LocationPin initiatorPin = LocationMapUtils.getLocationPinBlocking(initiator);
        LocationPin targetPin = LocationMapUtils.getLocationPinBlocking(subject);
        if (targetPin == null || !(targetPin.getAgentData() instanceof HasStock))
            return;
        if (initiatorPin == null || !(initiatorPin.getAgentData() instanceof HasStock))
            return;
        if (initiatorPin.getAgentData() instanceof IsMoveable && ((IsMoveable) initiatorPin.getAgentData()).isMoving()) {
            return;
        }

        HasStock initiatorStock = (HasStock) initiatorPin.getAgentData();
        HasStock targetStock = (HasStock) targetPin.getAgentData();
        //System.out.println(quantity + " transfer " + transferType.toString());
        switch (transferType) {
            case RESERVE:
                targetStock.reserveStock(quantity);
                break;
            case TAKE:
                initiatorStock.addCurrentStock(quantity);
                targetStock.addCurrentStock(- quantity);
                break;
            case GIVE:
                initiatorStock.addCurrentStock(- quantity);
                targetStock.addCurrentStock(quantity);
                break;
        }

        LocationMapUtils.updateLocationPinNonBlocking(initiator, initiatorPin);
        LocationMapUtils.updateLocationPinNonBlocking(subject, targetPin);
    }

    public enum TransferType {
        RESERVE,
        TAKE,
        GIVE
    }
}
