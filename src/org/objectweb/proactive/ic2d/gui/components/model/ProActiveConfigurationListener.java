package org.objectweb.proactive.ic2d.gui.components.model;

import org.objectweb.fractal.gui.model.ConfigurationListener;


/**
 * @author Matthieu Morel
 *
 */
public interface ProActiveConfigurationListener extends ConfigurationListener {
    // -------------------------------------------------------------------------
    // virtual nodes  specific informations
    // -------------------------------------------------------------------------
    void virtualNodeChanged(ProActiveComponent component, String oldValue);

    void exportedVirtualNodeChanged(ProActiveComponent component,
        String virtualNode, String oldValue);
}
