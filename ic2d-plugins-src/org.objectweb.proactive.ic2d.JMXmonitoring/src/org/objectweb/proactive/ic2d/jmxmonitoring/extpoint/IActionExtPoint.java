package org.objectweb.proactive.ic2d.jmxmonitoring.extpoint;

import org.eclipse.jface.action.IAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;


/**
 * This interface is used to export an action from an extension point.
 *
 * @author vbodnart
 *
 */
public interface IActionExtPoint extends IAction {

    /**
     * Sets the reference to an AbstractDataObject. This reference is provided
     * to the extension that plugs to an extension point.
     * @param object The AbstractDataObject that will be provided to the extension
     */
    public void setAbstractDataObject(AbstractData object);

    /**
     * Sets the selected active object.
     * @param The reference on the active object
     */
    public void setActiveSelect(ActiveObject ref);
}
