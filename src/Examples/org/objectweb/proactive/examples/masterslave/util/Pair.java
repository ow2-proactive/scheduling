package org.objectweb.proactive.examples.masterslave.util;

import java.io.Serializable;


public class Pair<P extends Serializable, R extends Serializable>
    implements Serializable {
    private P first;
    private R second;

    public Pair(P first, R second) {
        this.first = first;
        this.second = second;
    }

    public P getFirst() {
        return first;
    }

    public R getSecond() {
        return second;
    }
}
