package org.objectweb.proactive.ic2d.gui.components.model;

import org.objectweb.fractal.gui.model.StatusManager;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveStatusManager extends StatusManager
    implements ProActiveConfigurationListener {

    /**
     *
     */
    public void exportedVirtualNodeChanged(ProActiveComponent component,
        String virtualNode, String oldValue) {
        long status = component.getStatus();
        component.setStatus(status);
        saysItsModified(component);
    }

    /**
     *
     */
    public void virtualNodeChanged(ProActiveComponent component, String oldValue) {
        long status = component.getStatus();
        String virtualNode = component.getVirtualNode();
        //System.out.println("CHANGED VN : " + oldValue + " TO : " +
        //    component.getVirtualNode());
        component.setStatus(status);
        saysItsModified(component);
    }
}
