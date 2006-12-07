/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.component.adl.implementations;

import org.objectweb.deployment.scheduling.component.api.FactoryProviderTask;
import org.objectweb.deployment.scheduling.component.lib.AbstractInstanceProviderTask;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.TaskMap;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.implementations.Implementation;
import org.objectweb.fractal.adl.implementations.ImplementationCompiler;
import org.objectweb.fractal.adl.implementations.ImplementationContainer;
import org.objectweb.fractal.adl.nodes.VirtualNodeContainer;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;
import org.objectweb.proactive.core.component.type.Composite;

import java.util.List;
import java.util.Map;


/**
 * @author Matthieu Morel
 */
public class ProActiveImplementationCompiler extends ImplementationCompiler {
    private static int counter = 0;

    public void compile(final List path, final ComponentContainer container,
        final TaskMap tasks, final Map context) throws ADLException {
        counter++;

        String implementation = null;

        if (container instanceof ImplementationContainer) {
            ImplementationContainer ic = (ImplementationContainer) container;
            Implementation i = ic.getImplementation();

            if (i != null) {
                implementation = i.getClassName();
            }
        }

        String controller = null;

        if (container instanceof ControllerContainer) {
            ControllerContainer cc = (ControllerContainer) container;

            if (cc.getController() != null) {
                controller = cc.getController().getDescriptor();
            }
        }

        String name = null;

        if (container instanceof Definition) {
            name = ((Definition) container).getName();
        } else if (container instanceof Component) {
            name = ((Component) container).getName();
        }

        String definition = null;

        if (container instanceof Definition) {
            definition = name;
        } else {
            definition = (String) ((Node) container).astGetDecoration(
                    "definition");
        }

        //        Component[] comps = ((ComponentContainer) container).getComponents();
        VirtualNode n = null;

        if (container instanceof VirtualNodeContainer) {
            try {
                n = (VirtualNode) ((VirtualNodeContainer) container).getVirtualNode();
            } catch (ClassCastException e) {
                throw new ProActiveRuntimeException(
                    "DOCTYPE definition should be the following when using ProActive : \n" +
                    "<!DOCTYPE definition PUBLIC \"-//objectweb.org//DTD Fractal ADL 2.0//EN\" \"classpath://org/objectweb/proactive/core/component/adl/xml/proactive.dtd\">");
            }

            if (n == null) {
                // see Leclerq modification request : try to find a vn specified
                // in a parent component
                for (int i = path.size() - 1; i >= 0; --i) {
                    if (path.get(i) instanceof VirtualNodeContainer) {
                        try {
                            n = (VirtualNode) ((VirtualNodeContainer) path.get(i)).getVirtualNode();

                            if (n != null) {
                                break;
                            }
                        } catch (ClassCastException e) {
                            throw new ProActiveRuntimeException(
                                "DOCTYPE definition should be the following when using ProActive : \n" +
                                "<!DOCTYPE definition PUBLIC \"-//objectweb.org//DTD Fractal ADL 2.0//EN\" \"classpath://org/objectweb/proactive/core/component/adl/xml/proactive.dtd\">");
                        }
                    }
                }
            }
        }

        AbstractInstanceProviderTask createTask;

        ContentDescription contentDesc = null;
        ControllerDescription controllerDesc = null;

        if (implementation == null) {
            // a composite component 
            if ("composite".equals(controller) || (controller == null)) {
                controllerDesc = new ControllerDescription(name,
                        Constants.COMPOSITE);
                contentDesc = new ContentDescription(Composite.class.getName());
            } else {
                controllerDesc = new ControllerDescription(name,
                        Constants.COMPOSITE,
                        getClass().getResource(controller).getPath());
            }
        } else {
            // a primitive component
            //if (implementation instanceof String) {
            // that seems to be the case with the fractaladl
            // contentDesc = new ContentDescription((String) implementation);
            contentDesc = new ContentDescription(implementation);

            if ("primitive".equals(controller) || (controller == null)) {
                controllerDesc = new ControllerDescription(name,
                        Constants.PRIMITIVE);
            } else {
                // System.out.println("CONTROLLER = " + controller);
                controllerDesc = new ControllerDescription(name,
                        Constants.PRIMITIVE,
                        getClass().getResource(controller).getPath());
            }

            //}
        }

        createTask = new CreateTask((ProActiveImplementationBuilder) builder,
                container, name, definition, controllerDesc, contentDesc, n,
                context);

        FactoryProviderTask typeTask = (FactoryProviderTask) tasks.getTask("type",
                container);
        createTask.setFactoryProviderTask(typeTask);

        tasks.addTask("create", container, createTask);
    }

    // TODO change visibility of this inner class in ImplementationCompiler 
    static class CreateTask extends AbstractInstanceProviderTask {
        ProActiveImplementationBuilder builder;
        String name;
        String definition;
        ControllerDescription controllerDesc;
        ContentDescription contentDesc;
        VirtualNode vn;
        Map context;
        ComponentContainer container;

        public CreateTask(final ProActiveImplementationBuilder builder,
            final ComponentContainer container, final String name,
            final String definition,
            final ControllerDescription controllerDesc,
            final ContentDescription contentDesc, final VirtualNode vn,
            final Map context) {
            this.builder = builder;
            this.container = container;
            this.name = name;
            this.definition = definition;
            this.controllerDesc = controllerDesc;
            this.contentDesc = contentDesc;
            this.vn = vn;
            this.context = context;
        }

        public void execute(final Object context) throws Exception {
            if (getInstance() != null) {
                return;
            }

            Object type = getFactoryProviderTask().getFactory();
            Object result = builder.createComponent(type, name, definition,
                    controllerDesc, contentDesc, vn, (Map) context);
            setInstance(result);
        }

        public String toString() {
            return "T" + System.identityHashCode(this) + "[CreateTask(" + name +
            "," + controllerDesc + "," + contentDesc + ")]";
        }
    }
}
