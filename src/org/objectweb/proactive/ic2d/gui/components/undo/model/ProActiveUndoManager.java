package org.objectweb.proactive.ic2d.gui.components.undo.model;

import org.objectweb.fractal.gui.undo.model.BasicUndoManager;

import org.objectweb.proactive.ic2d.gui.components.model.ProActiveComponent;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveConfigurationListener;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveUndoManager extends BasicUndoManager
    implements ProActiveConfigurationListener {
    public void virtualNodeChanged(final ProActiveComponent component,
        final String oldValue) {
        if (configuration.getRootComponent().contains(component)) {
            addAction(new Action(component, 1) {
                    protected void run() {
                        ((ProActiveComponent) target).setVirtualNode(oldValue);
                    }
                });
        }
    }

    public void exportedVirtualNodeChanged(final ProActiveComponent component,
        final String virtualNodeName, final String oldValue) {
        if (configuration.getRootComponent().contains(component)) {
            addAction(new Action(component, 1) {
                    protected void run() {
                        ((ProActiveComponent) target).setComposingVirtualNodes(virtualNodeName,
                            oldValue);
                    }
                });
        }
    }
}
