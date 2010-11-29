package org.ow2.proactive.scheduler.ext.matsci.client;

/**
 * Pair
 *
 * @author The ProActive Team
 */
public class Pair<X, Y> implements java.io.Serializable {

    X x;
    Y y;

    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public X getX() {
        return x;
    }

    public Y getY() {
        return y;
    }

}
