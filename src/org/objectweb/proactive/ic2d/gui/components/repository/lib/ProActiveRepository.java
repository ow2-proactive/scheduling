package org.objectweb.proactive.ic2d.gui.components.repository.lib;

import org.objectweb.fractal.gui.graph.model.GraphModel;
import org.objectweb.fractal.gui.model.Component;
import org.objectweb.fractal.gui.repository.lib.BasicRepository;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveRepository extends BasicRepository {
    public String storeComponent(final Component component,
        final GraphModel graph, final Object hints) throws Exception {
        ProActiveAdlWriter writer = new ProActiveAdlWriter();
        writer.setStorage(storage);
        writer.setForceInternal("inline".equals(hints));
        return writer.saveTemplate(component, graph);
    }
}
