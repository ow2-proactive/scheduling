package org.objectweb.proactive.core.component.identity;

import org.objectweb.fractal.api.Component;

import org.objectweb.proactive.core.component.request.ComponentRequestQueue;


/**
 * This class extends Component, in order to provide access to some ProActive
 * functionalities (the parameters of the component, the request queue, the reified object)
 * 
  * @author Matthieu Morel
  */
public interface ProActiveComponent extends Component {
	
//    /**
//     * @return the hierarchical type of the component (see ComponentParameters)
//     */    
//    public String getHierarchicalType();

    /**
     * @return the reified object at the base of the active object
     */    
    public Object getReifiedObject();

//    /**
//     * This method's implementation is only valid in the component meta-objects.
//     * @return the configuration parameters of this component
//     */    
//    public ComponentParameters getComponentParameters();

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
	public boolean equals(Component componentIdentity);
}