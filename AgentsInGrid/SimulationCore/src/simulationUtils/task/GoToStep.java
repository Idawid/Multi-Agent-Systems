package simulationUtils.task;

import mapUtils.LocationMap;
import mapUtils.locationPin.*;
import simulationUtils.LocationMapUtils;

public class GoToStep extends Step {

    public GoToStep(String initiator, String subject) {
        super(initiator, subject);
    }

    public void perform() {
        moveToPosition();
    }

    private void moveToPosition() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        LocationPin initiatorPin = LocationMapUtils.getLocationPinBlocking(initiator);
        LocationPin oldInitiatorPin = new LocationPin(initiatorPin);
        LocationPin targetPin = LocationMapUtils.getLocationPinBlocking(subject);
        if (initiatorPin == null || targetPin == null) return;
        if (!(initiatorPin.getAgentData() instanceof IsMoveable ||
                ((IsMoveable)initiatorPin.getAgentData()).isMoving())) {
            System.err.println("Move is not allowed for this Pin or Pin is already moving.");
            return;
        }

        int timeInMilliseconds = (int) (initiatorPin.getDistance(targetPin) * 1000) / 60;
        int totalSteps = timeInMilliseconds * LocationMap.UPDATES_PER_SECOND / 1000 ;
        int currentSteps = 0;
        double stepX = ((double) targetPin.getX() - initiatorPin.getX()) / totalSteps;
        double stepY = ((double) targetPin.getY() - initiatorPin.getY()) / totalSteps;

        ((IsMoveable)initiatorPin.getAgentData()).startMoving();
        for (int i = 0; i < totalSteps; i++) {
            LocationMapUtils.updateLocationPinNonBlocking(initiator, initiatorPin);
            initiatorPin.setX(oldInitiatorPin.getX() + (int) (currentSteps * stepX));
            initiatorPin.setY(oldInitiatorPin.getY() + (int) (currentSteps * stepY));
            currentSteps++;

            try {
                Thread.sleep(1000 / LocationMap.UPDATES_PER_SECOND);
            } catch (InterruptedException e) { }
        }
        ((IsMoveable)initiatorPin.getAgentData()).stopMoving();
        LocationMapUtils.updateLocationPinNonBlocking(initiator, initiatorPin);
    }
}
