package org.objectweb.proactive.core.component.adl.components;

import org.objectweb.fractal.adl.components.ComponentBuilder;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;


/**
 * A ProActive based implementation of the {@link ComponentBuilder} interface.
 * This implementation uses the Fractal API to add and start components.
 * It slightly differs from the FractalComponentBuilder class : the name of the component
 * is not specified in this addition operation, but when the component is instantiated.
 * 
 */
public class ProActiveComponentBuilder implements ComponentBuilder {
    // --------------------------------------------------------------------------
    // Implementation of the ComponentBuilder interface
    // --------------------------------------------------------------------------
    public void addComponent(final Object superComponent,
        final Object subComponent, final String name, final Object context)
        throws Exception {
        Fractal.getContentController((Component) superComponent)
               .addFcSubComponent((Component) subComponent);
        // contrary to the standard fractal implementation, we do not set
        // the name of the component here because :
        // 1. it is already name at instantiation time
        // 2. it could be a group of components, and we do not want to give the 
        // same name to all the elements of the group
        //    try {
        //      Fractal.getNameController((Component)subComponent).setFcName(name);
        //    } catch (NoSuchInterfaceException ignored) {
        //    }
    }

    public void startComponent(final Object component, final Object context)
        throws Exception {

        /*
           try {
             Fractal.getLifeCycleController((Component)component).startFc();
           } catch (NoSuchInterfaceException ignored) {
           } catch (NullPointerException ignored) {
           }
         */
    }
}
