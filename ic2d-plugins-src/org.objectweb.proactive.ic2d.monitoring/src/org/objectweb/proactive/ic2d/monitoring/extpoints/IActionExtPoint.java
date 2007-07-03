package org.objectweb.proactive.ic2d.monitoring.extpoints;

import org.eclipse.jface.action.IAction;
import org.objectweb.proactive.ic2d.monitoring.data.AOObject;
import org.objectweb.proactive.ic2d.monitoring.data.AbstractDataObject;

/**
 * This interface is used to export an action from an extension point.
 * 
 * @author vbodnart
 *
 */
public interface IActionExtPoint extends IAction{	
	
	/**
	 * Sets the reference to an AbstractDataObject. This reference is provided
	 * to the extension that plugs to an extension point. 
	 * @param object The AbstractDataObject that will be provided to the extension
	 */
	public void setAbstractDataObject(AbstractDataObject object);
	
	/**
	 * Sets the selected active object.
	 * @param The reference on the active object 
	 */
	public void setActiveSelect(AOObject ref);
	
}