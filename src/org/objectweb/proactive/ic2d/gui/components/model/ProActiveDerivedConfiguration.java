package org.objectweb.proactive.ic2d.gui.components.model;

import java.util.Iterator;

import org.objectweb.fractal.gui.model.DerivedConfiguration;

/**
 * @author Matthieu Morel
 *
 */
public class ProActiveDerivedConfiguration extends DerivedConfiguration implements ProActiveConfigurationListener {
    
	/* (non-Javadoc)
	 * @see org.objectweb.fractal.gui.model.ConfigurationListener#virtualNodeChanged(org.objectweb.fractal.gui.model.Component, java.lang.String)
	 */
	public void virtualNodeChanged(ProActiveComponent component, String oldValue) {
		if (root.contains(component)) {
			Iterator i = listeners.values().iterator();
			while (i.hasNext()) {
			    ProActiveConfigurationListener l = (ProActiveConfigurationListener) i.next();
				l.virtualNodeChanged(component, oldValue);
			}
		}
	}

	public void exportedVirtualNodeChanged(ProActiveComponent component, String virtualNodeName, String oldValue) {
		if (root.contains(component)) {
			Iterator i = listeners.values().iterator();
			while (i.hasNext()) {
			    ProActiveConfigurationListener l = (ProActiveConfigurationListener) i.next();
				l.exportedVirtualNodeChanged(component, virtualNodeName, oldValue);
			}
		}
	}


}
