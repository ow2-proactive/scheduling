package org.objectweb.proactive.ic2d.monitoring.filters;

import org.objectweb.proactive.ic2d.monitoring.data.AOObject;
import org.objectweb.proactive.ic2d.monitoring.data.AbstractDataObject;
import org.objectweb.proactive.ic2d.monitoring.spy.Spy;

public class SpyFilter extends Filter {

	
	//
	// -- PUBLIC METHOD -----------------------------------------------
	//
	
	/**
	 * Returns true if the object is an AOObject representing a spy.
	 */
	public boolean filter(AbstractDataObject object) {
		return (object instanceof AOObject) && 
				(((AOObject)object).getName().compareTo(Spy.class.getSimpleName()) == 0);
	}

}
