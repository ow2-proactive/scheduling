package org.objectweb.proactive.examples.pi;

import java.io.Serializable;

/**
 * 
 * This class reprensents an interval sent to a worker. Thanks to this class, a worker knows from which to which decimal it has to perform  pi computation.
 * @author Matthieu Morel
 *
 */
public class Interval implements Serializable
{
    private Integer beginning;
    private Integer end;
    
    public Interval() {}
	
    public Interval(int beginning, int end){
	this.beginning = new Integer(beginning);
	this.end = new Integer(end);
    }
    
    public Integer getEnd() {
        return end;
    }


    public Integer getBeginning() {
        return beginning;
    }
}
