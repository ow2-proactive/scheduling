package org.objectweb.proactive.examples.pi;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 
 * @author Matthieu Morel
 *
 */
public class Result implements Serializable{
    private BigDecimal bd;
    private long computedTime;
    
    public Result(){}

    public Result(BigDecimal bd, long computedTime) {
	this.bd = bd;
	this.computedTime = computedTime;
    }
    
    public BigDecimal getNumericalResult() {
        return bd;
    }
    
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
