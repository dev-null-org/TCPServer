package robot_controller;

import java.util.Objects;

public class Point {

    public static final Point ZERO = new Point(0, 0);

    public final int x;
    public final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(String x, String y) {
        this.x = Integer.parseInt(x);
        this.y = Integer.parseInt(y);
    }

    public Point getVector(Point p) {
        return new Point(p.x - x, p.y - y);
    }

    public Direction toDirection() {
        if (Math.abs(x) > Math.abs(y)) {
            return x > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            return y > 0 ? Direction.UP : Direction.DOWN;
        }
    }

    public Direction toDirection(boolean secondBest) {
        if (Math.abs(x) <= Math.abs(y)) {
            return x > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            return y > 0 ? Direction.UP : Direction.DOWN;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "x=" + x +
                ", y=" + y;
    }
}