package org.objectweb.proactive.ic2d.gui.components.tree.model;

import org.objectweb.fractal.gui.tree.model.BasicTreeModel;

import org.objectweb.proactive.ic2d.gui.components.model.ProActiveComponent;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveConfigurationListener;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveTreeModel extends BasicTreeModel
    implements ProActiveConfigurationListener {
    public void virtualNodeChanged(ProActiveComponent component, String oldValue) {
        // does nothing
    }

    public void exportedVirtualNodeChanged(ProActiveComponent component,
        String virtualNodeName, String oldValue) {
        // does nothing
    }
}
