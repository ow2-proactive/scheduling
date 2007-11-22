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
package org.objectweb.proactive.core.component.representative;

import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.component.request.ComponentRequest;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;


/**
 * A factory for component representatives.
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveComponentRepresentativeFactory {
    private static ProActiveComponentRepresentativeFactory INSTANCE = null;

    private ProActiveComponentRepresentativeFactory() {
    }

    /**
     * returns the unique instance in the jvm
     * @return the unique instance in the jvm
     */
    public static ProActiveComponentRepresentativeFactory instance() {
        if (INSTANCE == null) {
            return (INSTANCE = new ProActiveComponentRepresentativeFactory());
        } else {
            return INSTANCE;
        }
    }

    /**
     * Creates a component representative according to the type of the component
     * (it also generates the required functional interfaces), and connects the representative to
     * the given proxy. It also takes into account a controller config file for generating references to
     * the implementations of the controllers of this component.
     * @param componentType the type of the component
     * @param proxy the proxy to the active object
     * @param controllerConfigFileLocation location of a file that contains the description of the controllers for this component. null will load the default configuration
     * @return a corresponding component representative
     */
    public ProActiveComponentRepresentative createComponentRepresentative(
        ComponentType componentType, String hierarchicalType, Proxy proxy,
        String controllerConfigFileLocation) {
        ProActiveComponentRepresentative representative = new ProActiveComponentRepresentativeImpl(componentType,
                hierarchicalType, controllerConfigFileLocation);
        representative.setProxy(proxy);
        return representative;
    }

    /**
     * Creates a component representative according to the type of the non-functional component
     * (it also generates the required functional interfaces), and connects the representative to
     * the given proxy. It also takes into account a controller config file for generating references to
     * the implementations of the controllers of this component.
     * @param componentType the type of the component
     * @param proxy the proxy to the active object
     * @param controllerConfigFileLocation location of a file that contains the description of the controllers for this component. null will load the default configuration
     * @return a corresponding component representative
     */
    public ProActiveComponentRepresentative createNFComponentRepresentative(
        ComponentType componentType, String hierarchicalType, Proxy proxy,
        String controllerConfigFileLocation) {
        ProActiveComponentRepresentative representative = new ProActiveNFComponentRepresentativeImpl(componentType,
                hierarchicalType, controllerConfigFileLocation);
        representative.setProxy(proxy);
        return representative;
    }

    /**
     * The creation of a component representative from a proxy object implies a remote invocation (immediate service) for
     * getting the parameters of the component, necessary for the construction of the representative
     * @param proxy a reference on a proxy pointing to a component
     * @return a component representative for the pointed component
     * @throws Throwable an exception
     */
    public ProActiveComponentRepresentative createComponentRepresentative(
        Proxy proxy) throws Throwable {
        // set immediate service for getComponentParameters
        proxy.reify(MethodCall.getComponentMethodCall(
                ComponentParametersController.class.getDeclaredMethod(
                    "setImmediateServices", new Class[] {  }),
                new Object[] {  }, null,
                Constants.COMPONENT_PARAMETERS_CONTROLLER, null,
                ComponentRequest.STRICT_FIFO_PRIORITY));

        ComponentParameters componentParameters = (ComponentParameters) proxy.reify(MethodCall.getComponentMethodCall(
                    ComponentParametersController.class.getDeclaredMethod(
                        "getComponentParameters", new Class[] {  }),
                    new Object[] {  }, null,
                    Constants.COMPONENT_PARAMETERS_CONTROLLER, null,
                    ComponentRequest.STRICT_FIFO_PRIORITY));

        //remove immediate service for getComponentParameters
        proxy.reify(MethodCall.getComponentMethodCall(
                ComponentParametersController.class.getDeclaredMethod(
                    "removeImmediateServices", new Class[] {  }),
                new Object[] {  }, null,
                Constants.COMPONENT_PARAMETERS_CONTROLLER, null,
                ComponentRequest.STRICT_FIFO_PRIORITY));

        return ProActiveComponentRepresentativeFactory.instance()
                                                      .createComponentRepresentative(componentParameters.getComponentType(),
            componentParameters.getHierarchicalType(), proxy,
            componentParameters.getControllerDescription()
                               .getControllersConfigFileLocation());
    }

    /**
     * The creation of a component representative (for a non-functional component) from a proxy object implies a remote invocation (immediate service) for
     * getting the parameters of the component, necessary for the construction of the representative
     * @param proxy a reference on a proxy pointing to a component
     * @return a component representative for the pointed component
     * @throws Throwable an exception
     */
    public ProActiveComponentRepresentative createNFComponentRepresentative(
        Proxy proxy) throws Throwable {
        // set immediate service for getComponentParameters
        proxy.reify(MethodCall.getComponentMethodCall(
                ComponentParametersController.class.getDeclaredMethod(
                    "setImmediateServices", new Class[] {  }),
                new Object[] {  }, null,
                Constants.COMPONENT_PARAMETERS_CONTROLLER, null,
                ComponentRequest.STRICT_FIFO_PRIORITY));
        ComponentParameters componentParameters = (ComponentParameters) proxy.reify(MethodCall.getComponentMethodCall(
                    ComponentParametersController.class.getDeclaredMethod(
                        "getComponentParameters", new Class[] {  }),
                    new Object[] {  }, null,
                    Constants.COMPONENT_PARAMETERS_CONTROLLER, null,
                    ComponentRequest.STRICT_FIFO_PRIORITY));

        //remove immediate service for getComponentParameters
        proxy.reify(MethodCall.getComponentMethodCall(
                ComponentParametersController.class.getDeclaredMethod(
                    "removeImmediateServices", new Class[] {  }),
                new Object[] {  }, null,
                Constants.COMPONENT_PARAMETERS_CONTROLLER, null,
                ComponentRequest.STRICT_FIFO_PRIORITY));
        return ProActiveComponentRepresentativeFactory.instance()
                                                      .createNFComponentRepresentative(componentParameters.getComponentType(),
            componentParameters.getHierarchicalType(), proxy,
            componentParameters.getControllerDescription()
                               .getControllersConfigFileLocation());
    }
}
