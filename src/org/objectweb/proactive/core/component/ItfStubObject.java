package org.objectweb.proactive.core.component;

import org.objectweb.proactive.core.component.representative.ItfID;

/**
 * Component interfaces may have specific and generic methods which are defined here.
 * 
 * 
 * @author Matthieu Morel
 *
 */
public interface ItfStubObject {
    
    /**
     * Indicates the sender of invocation on an interface
     * @param id id of sender interface
     */
	public void setSenderItfID(ItfID id);
    


}
