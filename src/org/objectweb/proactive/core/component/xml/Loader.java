/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2004 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.core.component.xml;

import org.objectweb.fractal.api.Component;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.runtime.RuntimeFactory;


/**
 * This class is used for automatic deployment of components with the ADL.
 * Virtual nodes specified in the ADL have to match with virtual nodes defined in the
 * deployment descriptor.
 * The deployment process is the following :
 * 1. instatiation of the components on virtual nodes defined in the deployment descriptor
 * 2. storing of component references in a cache
 * 3. assembly of the components
 * 4. binding of the components
 * Components are then accessible through the components cache.
 *
 * @author Matthieu Morel
 */
public class Loader {
    private ComponentsCache cache;
    ProActiveDescriptor deploymentDescriptor;

    public Loader() {
    }

    /**
     * Configures and instantiates a component system : creation of the components,
     * deployment on virtual nodes, assembly and binding.
     * Components are indexed by their name in a local cache, and can be retreived from there.
     * @param componentsDescriptorURL the location of the components descriptor
     * @param deploymentDescriptor an instance of ProActiveDescriptor, representing the deployment descriptor
     * @throws ProActiveException in case of a failure
     */
    public void loadComponentsConfiguration(String componentsDescriptorURL,
        ProActiveDescriptor deploymentDescriptor) throws ProActiveException {
        this.deploymentDescriptor = deploymentDescriptor;
        RuntimeFactory.getDefaultRuntime();
        try {
            cache = (ComponentsCache) ComponentsDescriptorHandler.createComponentsDescriptorHandler(componentsDescriptorURL,
                    deploymentDescriptor).getResultObject();
        } catch (org.xml.sax.SAXException e) {
            e.printStackTrace();
            ProActive.componentLogger.fatal(
                "a problem occured when getting the ProActive descriptor or the ComponentsDescriptor");
            throw new ProActiveException(e);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            ProActive.componentLogger.fatal(
                "a problem occured during the ProActiveDescriptor object creation");
            throw new ProActiveException(e);
        }
    }

    /**
     * Configures and instantiates a component system.
     * @param componentsDescriptorLocation the location of the components descriptor
     * @param deploymentDescriptorLocation the location of the deployment descriptor
     * @throws ProActiveException in case of a failure
     */
    public void loadComponentsConfiguration(
        String componentsDescriptorLocation, String deploymentDescriptorLocation)
        throws ProActiveException {
        ProActiveDescriptor deploymentDescriptor = ProActive.getProactiveDescriptor(deploymentDescriptorLocation);
        loadComponentsConfiguration(componentsDescriptorLocation,
            deploymentDescriptor);
    }

    /**
     * Returns an instantiated+deployed+assembled+bound component from the cache.
     * @param name the name of a component
     * @return the named component
     */
    public Component getComponent(String name) {
        return cache.getComponent(name);
    }

    public void killNodes(boolean softly) throws ProActiveException {
        deploymentDescriptor.killall(softly);
    }
}
