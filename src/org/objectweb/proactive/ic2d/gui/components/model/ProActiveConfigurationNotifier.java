package org.objectweb.proactive.ic2d.gui.components.model;

import org.objectweb.fractal.gui.model.ConfigurationNotifier;

import java.util.Iterator;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveConfigurationNotifier extends ConfigurationNotifier
    implements ProActiveConfigurationListener {
    public void virtualNodeChanged(ProActiveComponent component, String oldValue) {
        Iterator i = listeners.values().iterator();
        while (i.hasNext()) {
            ProActiveConfigurationListener l = (ProActiveConfigurationListener) i.next();
            l.virtualNodeChanged(component, oldValue);
        }
    }

    public void exportedVirtualNodeChanged(ProActiveComponent component,
        String virtualNodeName, String oldValue) {
        Iterator i = listeners.values().iterator();
        while (i.hasNext()) {
            ProActiveConfigurationListener l = (ProActiveConfigurationListener) i.next();
            l.exportedVirtualNodeChanged(component, virtualNodeName, oldValue);
        }
    }
}
