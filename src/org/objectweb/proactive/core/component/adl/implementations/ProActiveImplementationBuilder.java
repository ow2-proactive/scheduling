/*
 * Created on Apr 20, 2004
 * author : Matthieu Morel
 */
package org.objectweb.proactive.core.component.adl.implementations;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.implementations.ImplementationBuilder;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.util.Fractal;

import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.adl.RegistryManager;

import java.util.Map;


/**
 * @author Matthieu Morel
 */
public class ProActiveImplementationBuilder implements ImplementationBuilder,
    BindingController {
    public final static String REGISTRY_BINDING = "registry";
    public RegistryManager registry;

    // --------------------------------------------------------------------------
    // Implementation of the BindingController interface
    // --------------------------------------------------------------------------
    public String[] listFc() {
        return new String[] { REGISTRY_BINDING };
    }

    public Object lookupFc(final String itf) {
        if (itf.equals(REGISTRY_BINDING)) {
            return registry;
        }
        return null;
    }

    public void bindFc(final String itf, final Object value) {
        if (itf.equals(REGISTRY_BINDING)) {
            registry = (RegistryManager) value;
        }
    }

    public void unbindFc(final String itf) {
        if (itf.equals(REGISTRY_BINDING)) {
            registry = null;
        }
    }

    public Object createComponent(final Object type, final String name,
        final String definition, final Object controllerDesc,
        final Object contentDesc, final Object context)
        throws Exception {
        Component bootstrap = null;
        if (context != null) {
            bootstrap = (Component) ((Map) context).get("bootstrap");
        }
        if (bootstrap == null) {
            bootstrap = Fractal.getBootstrapComponent();
        }

        Component result = null;
        if (type instanceof ComponentType &&
                controllerDesc instanceof ControllerDescription &&
                contentDesc instanceof ContentDescription) {
            result = Fractal.getGenericFactory(bootstrap).newFcInstance((ComponentType)type,
                    controllerDesc, contentDesc);
        } else {
            if (!(controllerDesc instanceof String)) {
                throw new ADLException("the controller description should either be primitive, composite or parallel",
                    null);
            }

            // the following differs from the standard fractal adl because in the case of a cyclic virtual
            // node, the name of the component needs to be known at instantiation
            // Moreover, it also saves 1 method call (naming through the NameController) 
            String hierarchical_type = null;
            if ("primitive".equals(controllerDesc)) {
                hierarchical_type = Constants.PRIMITIVE;
            } else if ("composite".equals(controllerDesc)) {
                hierarchical_type = Constants.COMPOSITE;
            } else if ("parallel".equals(controllerDesc)) {
                hierarchical_type = Constants.PARALLEL;
            }
            result = Fractal.getGenericFactory(bootstrap).newFcInstance((ComponentType) type,
                    new ControllerDescription(name, hierarchical_type),
                    contentDesc);
        }

        registry.addComponent(result); // the registry can handle groups
        return result;
    }
}
