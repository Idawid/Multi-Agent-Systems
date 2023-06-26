package simulationUtils.task;

import agents.WarehouseAgent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import mapUtils.locationPin.HasStock;
import mapUtils.locationPin.IsMoveable;
import mapUtils.locationPin.LocationPin;
import simulationUtils.Constants;
import simulationUtils.LocationMapUtils;

import java.io.IOException;

public class TransferStockStep extends Step {
    private int quantity;
    private TransferType transferType;
    private final WarehouseAgent isReplenishment;

    public TransferStockStep(AID initiator, AID subject, int quantity, TransferType transferType, WarehouseAgent isReplenishment) {
        super(initiator, subject);
        this.quantity = quantity;
        this.transferType = transferType;
        this.isReplenishment = isReplenishment;
    }

    @Override
    public void perform()  {
        LocationPin initiatorPin = LocationMapUtils.getLocationPinBlocking(initiator.getLocalName());
        LocationPin targetPin = LocationMapUtils.getLocationPinBlocking(subject.getLocalName());
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
                if (isReplenishment != null) {
                    isReplenishment.finalizeOrder(quantity);
                }
                break;
        }

        LocationMapUtils.updateLocationPinNonBlocking(initiator.getLocalName(), initiatorPin);
        LocationMapUtils.updateLocationPinNonBlocking(subject.getLocalName(), targetPin);
    }

    public enum TransferType {
        RESERVE,
        TAKE,
        GIVE
    }
}
