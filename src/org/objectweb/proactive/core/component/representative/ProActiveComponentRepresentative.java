/*
 * Created on Apr 17, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.objectweb.proactive.core.component.representative;

import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;


/**
 * Implementations of this interface provide (remote) references to components.
 * 
 * @author Matthieu Morel
 *
 */
public interface ProActiveComponentRepresentative extends ProActiveComponent {
	
	/**
	 * @return configuration parameters of the component
	 */
	public ComponentParameters getComponentParameters();
	
//	/**
//	 * @return a standard ProActive reference on the reified object
//	 */
//	public Object getStubOnReifiedObject();
	

}
