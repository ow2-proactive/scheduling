package org.objectweb.proactive.core.component.identity;

import org.objectweb.fractal.api.Component;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.component.request.ComponentRequestQueue;


/**
 * This class extends Component, in order to provide access to some ProActive
 * functionalities (the parameters of the component, the request queue, the reified object)
 * 
  * @author Matthieu Morel
  */
public interface ProActiveComponent extends Component {
	

    /**
     * accessor to the base object : either a direct reference or a stub
     * @return a reference on the base object. If called from the meta-objects,
     * it returns a direct reference on the base object. If called from the representative, 
     * it returns a stub on the base object (standard ProActive stub, same type than 
     * the base object) 
     */    
    public Object getReferenceOnBaseObject();

    /**
     * This method's implementation is only valid in the component meta-objects.
     * @return a reference to the queue of request of this active object
     */    
	public ComponentRequestQueue getRequestQueue();
	
	/**
	 * comparison between components
	 * @param componentIdentity another component to compare to
	 * @return true if both components are equals
	 */
	public boolean equals(Object object);
	
	
	/**
	 * getter for a unique identifier
	 * @return a unique identifier of the component (of the active object) accross virtual machines
	 */
	public UniqueID getID();
	
	

}