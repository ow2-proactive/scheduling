package org.objectweb.proactive.examples.dynamicdispatch.nqueens;

import java.io.Serializable;


public class Result implements Serializable {

    long computationTime;
    long computedValue;

    public Result() {

    }

    public Result(long computedValue, long computationTime) {
        this.computedValue = computedValue;
        this.computationTime = computationTime;
    }

    public long getComputationTime() {
        return computationTime;
    }

    public long getComputedValue() {
        return computedValue;
    }
}
