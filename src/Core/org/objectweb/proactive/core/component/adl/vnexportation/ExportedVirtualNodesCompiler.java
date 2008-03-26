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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.component.adl.vnexportation;

import java.util.List;
import java.util.Map;

import org.objectweb.deployment.scheduling.component.api.InstanceProviderTask;
import org.objectweb.deployment.scheduling.component.lib.AbstractConfigurationTask;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.TaskMap;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.PrimitiveCompiler;
import org.objectweb.fractal.adl.nodes.VirtualNodeContainer;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;


/**
 * A {@link org.objectweb.fractal.adl.components.PrimitiveCompiler} to compile
 * {@link org.objectweb.proactive.core.component.adl.vnexportation.ExportedVirtualNodes}
 * in definitions.
 *
 *
 * @author The ProActive Team
 *
 */
public class ExportedVirtualNodesCompiler implements PrimitiveCompiler, BindingController {
    private ExportedVirtualNodesBuilder builder;
    public final static String BUILDER_BINDING = "builder";

    public String[] listFc() {
        return new String[] { BUILDER_BINDING };
    }

    public Object lookupFc(final String itf) {
        if (itf.equals(BUILDER_BINDING)) {
            return builder;
        }
        return null;
    }

    public void bindFc(final String itf, final Object value) {
        if (itf.equals(BUILDER_BINDING)) {
            builder = (ExportedVirtualNodesBuilder) value;
        }
    }

    public void unbindFc(final String itf) {
        if (itf.equals(BUILDER_BINDING)) {
            builder = null;
        }
    }

    public void compile(List path, ComponentContainer container, TaskMap tasks, Map context)
            throws ADLException {
        if (container instanceof ExportedVirtualNodesContainer) {
            ExportedVirtualNodes exported_vns = ((ExportedVirtualNodesContainer) container)
                    .getExportedVirtualNodes();
            if (exported_vns != null) {
                //                InstanceProviderTask c = (InstanceProviderTask) tasks.getTask("create",
                //                        container);
                String component_name = null;
                if (container instanceof Definition) {
                    component_name = ((Definition) container).getName();
                } else if (container instanceof Component) {
                    component_name = ((Component) container).getName();
                }

                //                else {
                //                    component_name = ((Definition) container).getName();
                //                }
                VirtualNode current_component_vn = (VirtualNode) ((VirtualNodeContainer) container)
                        .getVirtualNode();
                SetExportedVirtualNodesTask t = new SetExportedVirtualNodesTask(component_name, builder,
                    exported_vns.getExportedVirtualNodes(), current_component_vn);

                InstanceProviderTask createTask = (InstanceProviderTask) tasks.getTask("create", container);

                // exportations to be known *before* the creation of components.
                createTask.addPreviousTask(t);

                tasks.addTask("exportedVirtualNodes", container, t);
            }
        }
    }

    static class SetExportedVirtualNodesTask extends AbstractConfigurationTask {
        private ExportedVirtualNodesBuilder builder;
        private String componentName;
        private ExportedVirtualNode[] exported_vns;
        private VirtualNode currentComponentVN;

        public SetExportedVirtualNodesTask(String componentName, ExportedVirtualNodesBuilder builder,
                ExportedVirtualNode[] exported_vns, VirtualNode currentComponentVN) {
            this.componentName = componentName;
            this.builder = builder;
            this.exported_vns = exported_vns;
            this.currentComponentVN = currentComponentVN;
        }

        public void execute(final Object context) throws Exception {
            //Object component = getInstanceProviderTask().getResult();
            builder.compose(componentName, exported_vns, currentComponentVN);
        }

        public Object getResult() {
            return null;
        }

        public void setResult(Object result) {
        }
    }
}
