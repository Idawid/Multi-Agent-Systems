package utils;

public class Location {
    private int x;
    private int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
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
}
