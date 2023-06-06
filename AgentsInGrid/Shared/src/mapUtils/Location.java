package mapUtils;

import java.io.Serializable;

public class Location implements Serializable {
    private int x;
    private int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Location(Location location) {
        this.x = location.getX();
        this.y = location.getY();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getDistance(Location otherLocation) {
        int deltaX = otherLocation.getX() - x;
        int deltaY = otherLocation.getY() - y;

        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setLocation(Location location) {
        this.x = location.getX();
        this.y = location.getY();
    }

    public Location getLocation() {
        return this;
    }
}
