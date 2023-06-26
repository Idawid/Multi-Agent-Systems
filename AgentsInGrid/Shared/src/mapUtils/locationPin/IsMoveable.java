package mapUtils.locationPin;

public abstract class IsMoveable {
    protected MovementStatus moveStatus = MovementStatus.IDLE;

    public boolean isMoving() {
        return moveStatus == MovementStatus.MOVING;
    }

    public void startMoving() {
        moveStatus = MovementStatus.MOVING;
    }

    public void stopMoving() {
        moveStatus = MovementStatus.IDLE;
    }
}
