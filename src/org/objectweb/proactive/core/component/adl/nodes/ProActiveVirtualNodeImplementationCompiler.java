/*
 * Created on Apr 20, 2004 author : Matthieu Morel
 */
package org.objectweb.proactive.core.component.adl.nodes;

import org.objectweb.deployment.scheduling.component.lib.AbstractInstanceProviderTask;

import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.implementations.ImplementationBuilder;
import org.objectweb.fractal.adl.implementations.ImplementationCompiler;
import org.objectweb.fractal.adl.nodes.VirtualNode;
import org.objectweb.fractal.adl.nodes.VirtualNodeContainer;

import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.core.component.type.ParallelComposite;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Matthieu Morel
 */
public class ProActiveVirtualNodeImplementationCompiler
    extends ImplementationCompiler {
    public AbstractInstanceProviderTask newCreateTask(final List path,
        final ComponentContainer container, final String name,
        final String definition, final Object controller,
        final Object implementation, final Map context) {
        VirtualNode n = null;
        if (container instanceof VirtualNodeContainer) {
            n = ((VirtualNodeContainer) container).getVirtualNode();
            if (n == null) {
                // see Leclerq modification request : try to find a vn specified
                // in a parent component
                for (int i = path.size() - 1; i >= 0; --i) {
                    if (path.get(i) instanceof VirtualNodeContainer) {
                        n = ((VirtualNodeContainer) path.get(i)).getVirtualNode();
                        if (n != null) {
                            break;
                        }
                    }
                }
            }
            ContentDescription contentDesc = null;
            ControllerDescription controllerDesc = null;
            if (implementation == null) {
                // a composite (or parallel) component 
                if (controller.equals("composite")) {
                    controllerDesc = new ControllerDescription(name,
                            Constants.COMPOSITE);
                    contentDesc = new ContentDescription(Composite.class.getName());
                } else if (controller.equals("parallel")) {
                    controllerDesc = new ControllerDescription(name,
                            Constants.PARALLEL);
                    contentDesc = new ContentDescription(ParallelComposite.class.getName());
                } else {
                    System.out.println(
                        "Error while parsing the ADL : if no implementation is specified, a component should have the controller value set either to composite or to parallel");
                    controllerDesc = null;
                }
            } else {
                // a primitive component
                if (implementation instanceof String) {
                    // that seems to be the case with the fractaladl
                    contentDesc = new ContentDescription((String) implementation);
                    controllerDesc = new ControllerDescription(name,
                            Constants.PRIMITIVE);
                }
            }
            if (n != null) {
                if (context.get("deployment-descriptor") != null) {
                    org.objectweb.proactive.core.descriptor.data.VirtualNode vn = ((ProActiveDescriptor) context.get(
                            "deployment-descriptor")).getVirtualNode(n.getName());
                    contentDesc.setVirtualNode(vn);
                    return new RemoteCreateTask(builder, name, definition,
                        controllerDesc, contentDesc, context.get(n.getName()));
                }
            }
        }
        return super.newCreateTask(path, container, name, definition,
            controller, implementation, context);
    }

    static class RemoteCreateTask extends AbstractInstanceProviderTask {
        ImplementationBuilder builder;
        String name;
        String definition;
        Object controllerDesc;
        Object contentDesc;
        Object node;

        public RemoteCreateTask(final ImplementationBuilder builder,
            final String name, final String definition,
            final Object controllerDesc, final Object contentDesc,
            final Object node) {
            this.builder = builder;
            this.name = name;
            this.definition = definition;
            this.controllerDesc = controllerDesc;
            this.contentDesc = contentDesc;
            this.node = node; // not useful apparently
        }

        public void execute(Object context) throws Exception {
            if (getInstance() != null) {
                return;
            }
            if ((node != null) && context instanceof Map) {
                context = new HashMap((Map) context);
                ((Map) context).put("bootstrap", node);
            }
            Object type = getFactoryProviderTask().getFactory();
            Object result = builder.createComponent(type, name, definition,
                    controllerDesc, contentDesc, context);
            setInstance(result);
        }

        public String toString() {
            return "T" + System.identityHashCode(this) + "[CreateTask(" + name +
            "," + controllerDesc + "," + contentDesc + ")]";
        }
    }
}
