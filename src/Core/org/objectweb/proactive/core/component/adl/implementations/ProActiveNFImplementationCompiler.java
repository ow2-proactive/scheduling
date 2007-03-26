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
package org.objectweb.proactive.core.component.adl.implementations;

import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.TaskMap;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.NFControllerDescription;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;
import org.objectweb.proactive.core.component.type.Composite;


/**
 * @author Paul Naoumenko
 */
public class ProActiveNFImplementationCompiler
    extends ProActiveImplementationCompiler {
    @Override
    public void compile(final List path, final ComponentContainer container,
        final TaskMap tasks, final Map context) throws ADLException {
        ObjectsContainer obj = init(path, container, tasks, context);
        nfControllers(obj.getImplementation(), obj.getController(),
            obj.getName(), obj);
        end(tasks, container, context, obj.getName(), obj.getDefinition(),
            obj.getControllerDesc(), obj.getContentDesc(), obj.getVn(), false);
    }

    private void nfControllers(String implementation, String controller,
        String name, ObjectsContainer obj) {
        ContentDescription contentDesc = null;
        NFControllerDescription controllerDesc = null;

        if (implementation == null) {
            // a composite component 
            if ("composite".equals(controller) || (controller == null)) {
                controllerDesc = new NFControllerDescription(name,
                        Constants.COMPOSITE);
                contentDesc = new ContentDescription(Composite.class.getName());
            } else {
                controllerDesc = new NFControllerDescription(name,
                        Constants.COMPOSITE, getControllerPath(controller, name));
            }
        } else {
            // a primitive component
            contentDesc = new ContentDescription(implementation);

            if ("primitive".equals(controller) || (controller == null)) {
                controllerDesc = new NFControllerDescription(name,
                        Constants.PRIMITIVE);
            } else {
                controllerDesc = new NFControllerDescription(name,
                        Constants.PRIMITIVE, getControllerPath(controller, name));
            }
        }

        obj.setContentDesc(contentDesc);
        obj.setControllerDesc(controllerDesc);
    }

    static class CreateNFTask extends CreateTask {
        ProActiveNFImplementationBuilder builder;
        NFControllerDescription controllerDesc;

        public CreateNFTask(final ProActiveImplementationBuilder builder,
            final ComponentContainer container, final String name,
            final String definition,
            final ControllerDescription controllerDesc,
            final ContentDescription contentDesc, final VirtualNode vn,
            final Map context) {
            super(null, container, name, definition, controllerDesc,
                contentDesc, vn, context);
            this.builder = (ProActiveNFImplementationBuilder) builder;
            this.controllerDesc = (NFControllerDescription) controllerDesc;
        }

        @Override
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
