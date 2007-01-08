package org.objectweb.proactive.examples.pi;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * This class represents the result of a computation(performed on an interval or).
 * @author Matthieu Morel
 *
 */
public class Result implements Serializable{
    private BigDecimal bd;
    private long computedTime;
    
    /**
     * Empty constructor
     */
    public Result(){}

    /**
     * @param bd the initial BigDecimal value
     * @param computedTime time the computation took
     */
    public Result(BigDecimal bd, long computedTime) {
	this.bd = bd;
	this.computedTime = computedTime;
    }
    
    /**
     * @return the value of the result, as a BigDecimal
     */
    public BigDecimal getNumericalResult() {
        return bd;
    }
    
    /**
     * @param increment the big decimal to add
     */
    public void addNumericalResult(BigDecimal increment) {
        bd = bd.add(increment);
    }
    
    /**
     * @return Returns the computedTime.
     */
    public long getComputationTime() {
        return computedTime;
    }
    /**
     * @param computedTime The computedTime to add.
     */
    public void addComputationTime(long computedTime) {
        this.computedTime += computedTime;
    }
    
    public String toString () {
        return (bd != null ? bd.toString()+"" : null) ;}
}
