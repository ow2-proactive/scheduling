package org.objectweb.proactive.ic2d.gui.components.model;

import org.objectweb.fractal.gui.model.Component;
import org.objectweb.fractal.gui.model.VetoableConfigurationListener;

/**
 * @author Matthieu Morel
 *
 */
public interface ProActiveVetoableConfigurationListener extends
        VetoableConfigurationListener {
    
    void canChangeVirtualNode(Component component, String virtualNode);
    
    void canChangeExportedVirtualNode(ProActiveComponent component, String exportedVirtualNodes);


}
