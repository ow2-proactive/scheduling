/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.gui.components.repository.lib;

import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.implementations.ImplementationBuilder;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.gui.model.Component;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.adl.implementations.ProActiveImplementationBuilder;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;
import org.objectweb.proactive.core.component.adl.vnexportation.ExportedVirtualNodesList;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveComponent;


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
        Component c = (Component) createComponent(type, name, definition,controllerDesc.getHierarchicalType(),
                contentDesc.getClassName(), context);

        if (c instanceof ProActiveComponent) {

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
