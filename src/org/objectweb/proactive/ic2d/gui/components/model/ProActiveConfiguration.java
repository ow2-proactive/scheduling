package org.objectweb.proactive.ic2d.gui.components.model;

import org.objectweb.fractal.gui.model.BasicConfiguration;
import org.objectweb.fractal.gui.model.Component;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveConfiguration extends BasicConfiguration {

    /**
     *
     */
    public ProActiveConfiguration() {
        super();
        // override Fractal Status Manager
        listeners.put("", new ProActiveStatusManager());
    }

    public Component createComponent() {
        return new ProActiveComponentImpl(this);
    }
}
