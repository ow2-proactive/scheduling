package org.objectweb.proactive.ic2d.gui.components;

import org.objectweb.fractal.gui.TitlePane;

import org.objectweb.proactive.ic2d.gui.components.model.ProActiveComponent;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveConfigurationListener;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveTitlePane extends TitlePane
    implements ProActiveConfigurationListener {
    public void virtualNodeChanged(ProActiveComponent component, String oldValue) {
        // TOCHECK : update title ???
        updateTitle(component);
    }

    public void exportedVirtualNodeChanged(ProActiveComponent component,
        String virtualNodeName, String oldValue) {
        // TOCHECK : update title ???
        updateTitle(component);
    }
}
