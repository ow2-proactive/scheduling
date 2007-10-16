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
package org.objectweb.proactive.core.component.adl.implementations;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author Matthieu Morel
 */
public class ProActiveImplementationCompiler extends ImplementationCompiler {
    protected static int counter = 0;
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

    @Override
    public void compile(final List path, final ComponentContainer container,
        final TaskMap tasks, final Map context) throws ADLException {
        ObjectsContainer obj = init(path, container, tasks, context);
        controllers(obj.getImplementation(), obj.getController(),
            obj.getName(), obj);
        end(tasks, container, context, obj.getName(), obj.getDefinition(),
            obj.getControllerDesc(), obj.getContentDesc(), obj.getVn(), true);
    }

    protected static String getControllerPath(String controller, String name) {
        URL controllerURL = ProActiveImplementationCompiler.class.getResource(controller);
        if (controllerURL != null) {
            return controllerURL.getPath();
        } else {
            logger.warn("Can't retrieve controller description \"" +
                controller + "\" for component " + name);
            return null;
        }
    }

    protected ObjectsContainer init(final List path,
        final ComponentContainer container, final TaskMap tasks,
        final Map context) {
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

        return new ObjectsContainer(implementation, controller, name,
            definition, n);
    }

    protected void end(final TaskMap tasks, final ComponentContainer container,
        final Map context, String name, String definition,
        ControllerDescription controllerDesc, ContentDescription contentDesc,
        VirtualNode n, boolean isFunctional) {
        AbstractInstanceProviderTask createTask = null;

        createTask = new CreateTask((ProActiveImplementationBuilder) builder,
                container, name, definition,
                (ControllerDescription) controllerDesc, contentDesc, n, context);

        FactoryProviderTask typeTask = (FactoryProviderTask) tasks.getTask("type",
                container);
        createTask.setFactoryProviderTask(typeTask);

        tasks.addTask("create", container, createTask);
    }

    private void controllers(String implementation, String controller,
        String name, ObjectsContainer obj) {
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
                        Constants.COMPOSITE, getControllerPath(controller, name));
            }
        } else {
            // a primitive component
            contentDesc = new ContentDescription(implementation);

            if ("primitive".equals(controller) || (controller == null)) {
                controllerDesc = new ControllerDescription(name,
                        Constants.PRIMITIVE);
            } else {
                controllerDesc = new ControllerDescription(name,
                        Constants.PRIMITIVE, getControllerPath(controller, name));
            }
        }

        obj.setContentDesc(contentDesc);
        obj.setControllerDesc(controllerDesc);
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

        @Override
        public String toString() {
            return "T" + System.identityHashCode(this) + "[CreateTask(" + name +
            "," + controllerDesc + "," + contentDesc + ")]";
        }
    }

    protected class ObjectsContainer {
        private String implementation;
        private String controller;
        private String name;
        private String definition;
        private VirtualNode n;
        ContentDescription contentDesc = null;
        ControllerDescription controllerDesc = null;

        public ObjectsContainer(String implementation, String controller,
            String name, String definition, VirtualNode n) {
            this.implementation = implementation;
            this.controller = controller;
            this.name = name;
            this.definition = definition;
            this.n = n;
        }

        public String getController() {
            return controller;
        }

        public String getDefinition() {
            return definition;
        }

        public String getImplementation() {
            return implementation;
        }

        public VirtualNode getVn() {
            return n;
        }

        public String getName() {
            return name;
        }

        public ContentDescription getContentDesc() {
            return contentDesc;
        }

        public void setContentDesc(ContentDescription contentDesc) {
            this.contentDesc = contentDesc;
        }

        public ControllerDescription getControllerDesc() {
            return controllerDesc;
        }

        public void setControllerDesc(ControllerDescription controllerDesc) {
            this.controllerDesc = controllerDesc;
        }
    }
}
