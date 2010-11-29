package org.ow2.proactive.scheduler.ext.matsci.client;

/**
 * Pair
 *
 * @author The ProActive Team
 */
public class ComparablePair implements Comparable {
    private int x;
    private int y;

    public ComparablePair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ComparablePair))
            return false;

        ComparablePair comparablePair = (ComparablePair) o;

        if (x != comparablePair.x)
            return false;
        if (y != comparablePair.y)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    public int compareTo(Object o) {
        if (this == o)
            return 0;
        if (!(o instanceof ComparablePair))
            throw new IllegalArgumentException();

        ComparablePair comparablePair = (ComparablePair) o;

        if (x < comparablePair.x)
            return -1;
        if (x > comparablePair.x)
            return 1;
        if (y < comparablePair.y)
            return -1;
        if (y > comparablePair.y)
            return -1;
        return 0;

    }
}
