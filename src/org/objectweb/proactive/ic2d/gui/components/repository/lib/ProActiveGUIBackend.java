package org.objectweb.proactive.ic2d.gui.components.repository.lib;

import org.objectweb.fractal.adl.implementations.ImplementationBuilder;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.gui.model.Component;

import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.adl.implementations.ProActiveImplementationBuilder;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;
import org.objectweb.proactive.core.component.adl.vnexportation.ExportedVirtualNodesList;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveComponent;

import java.util.List;
import java.util.Map;


/**
 *
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveGUIBackend implements ProActiveImplementationBuilder,
    BindingController {
    private ImplementationBuilder fractalImplementationBuilder;
    public final static String FRACTAL_IMPLEMENTATION_BUILDER_BINDING = "implementation-builder";

    public String[] listFc() {
        return new String[] { FRACTAL_IMPLEMENTATION_BUILDER_BINDING };
    }

    public Object lookupFc(final String itf) {
        if (itf.equals(FRACTAL_IMPLEMENTATION_BUILDER_BINDING)) {
            return fractalImplementationBuilder;
        }
        return null;
    }

    public void bindFc(final String itf, final Object value) {
        if (itf.equals(FRACTAL_IMPLEMENTATION_BUILDER_BINDING)) {
            fractalImplementationBuilder = (ImplementationBuilder) value;
        }
    }

    public void unbindFc(final String itf) {
        if (itf.equals(FRACTAL_IMPLEMENTATION_BUILDER_BINDING)) {
            fractalImplementationBuilder = null;
        }
    }

    public Object createComponent(Object type, String name, String definition,
        Object controllerDesc, Object contentDesc, Object context)
        throws Exception {
        return fractalImplementationBuilder.createComponent(type, name,
            definition, controllerDesc, contentDesc, context);
    }

    /**
     *
     */
    public Object createComponent(Object type, String name, String definition,
        ControllerDescription controllerDesc, ContentDescription contentDesc,
        VirtualNode adlVN, Map context) throws Exception {
        String string_vn = "";
        if (adlVN != null) {
            string_vn = adlVN.getName() +
                (adlVN.getCardinality().equals(VirtualNode.MULTIPLE) ? "*" : "");
        }

        // convert to Julia's format
        Component c = (Component) createComponent(type, name, definition,
                (controllerDesc.getHierarchicalType().equals(Constants.PARALLEL)
                ? "composite" : controllerDesc.getHierarchicalType()),
                contentDesc.getClassName(), context);

        if (c instanceof ProActiveComponent) {
            // set parallel
            if (controllerDesc.getHierarchicalType().equals(Constants.PARALLEL)) {
                ((ProActiveComponent) c).setParallel();
            }

            // add the virtual node
            if (string_vn != null) {
                ((ProActiveComponent) c).setVirtualNode(string_vn);
            }
            List exported_vns_list = ExportedVirtualNodesList.instance()
                                                             .getExportedVirtualNodes(name);
        }
        return c;
    }
}
