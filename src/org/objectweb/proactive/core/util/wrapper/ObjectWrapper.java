/*
 * Created on Jun 3, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.core.util.wrapper;

import java.io.Serializable;

/**
 * @author vlegrand
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ObjectWrapper <T extends Object >implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 6165789939643190366L;
	private T  o;
    
    public ObjectWrapper () {}
     
    public ObjectWrapper(T o) {
        this.o=o;
    }
    
    public T  getObject() {
        return this.o;
    }
    
    public boolean equals (Object o ) {
    	return this.hashCode() == o.hashCode();
    }
}
