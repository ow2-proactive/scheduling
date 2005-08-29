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
package org.objectweb.proactive.core.component.representative;

import java.io.File;

import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.mop.Proxy;


/**
 * This is a factory for component representatives.
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
     * @param controllerConfigFile a file that contains the description of the controllers for this component. null will load the default configuration
     * @return a corresponding component representative
     */
    public ProActiveComponentRepresentative createComponentRepresentative(
        ComponentType componentType, String hierarchicalType, Proxy proxy,
        File controllerConfigFile) {
        if (controllerConfigFile == null) {
            controllerConfigFile = new File(getClass()
                                                .getResource(ControllerDescription.DEFAULT_COMPONENT_CONFIG_FILE_LOCATION)
                                                .getFile());
        }
        ProActiveComponentRepresentative representative = new ProActiveComponentRepresentativeImpl(componentType,
                hierarchicalType, controllerConfigFile);
        representative.setProxy(proxy);
        return representative;
    }
}
