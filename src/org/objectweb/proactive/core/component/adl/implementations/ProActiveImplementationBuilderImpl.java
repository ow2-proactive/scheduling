/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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

import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.adl.RegistryManager;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;
import org.objectweb.proactive.core.component.adl.vnexportation.ExportedVirtualNodesList;
import org.objectweb.proactive.core.component.adl.vnexportation.LinkedVirtualNode;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author Matthieu Morel
 */
public class ProActiveImplementationBuilderImpl
    implements ProActiveImplementationBuilder, BindingController {
    public final static String REGISTRY_BINDING = "registry";
    public RegistryManager registry;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

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

    //  --------------------------------------------------------------------------
    // Implementation of the Implementation Builder and ProActiveImplementationBuilder interfaces
    // --------------------------------------------------------------------------
    public Object createComponent(Object arg0, String arg1, String arg2,
        Object arg3, Object arg4, Object arg5) throws Exception {
        return null;
    }

    public Object createComponent(Object type, String name, String definition,
        ControllerDescription controllerDesc, ContentDescription contentDesc,
        VirtualNode adlVN, Map context) throws Exception {
        Component bootstrap = null;
        if (context != null) {
            bootstrap = (Component) ((Map) context).get("bootstrap");
        }
        if (bootstrap == null) {
            bootstrap = Fractal.getBootstrapComponent();
        }

        if (adlVN != null) {
            // consider exported virtual nodes
            LinkedVirtualNode exported = ExportedVirtualNodesList.instance()
                                                                 .getNode(name,
                    adlVN.getName(), false);
            if (exported != null) {
                adlVN.setName(exported.getExportedVirtualNodeNameAfterComposition());
                adlVN.setCardinality(exported.isMultiple()
                    ? VirtualNode.MULTIPLE : VirtualNode.SINGLE);
            } else {
                // 	TODO add self exported virtual node ?
                // for the moment, just add a leaf to the linked vns
                ExportedVirtualNodesList.instance().addLeafVirtualNode(name,
                    adlVN.getName(), adlVN.getCardinality()); // TODO_M check this
            }
            if (context.get("deployment-descriptor") != null) {
                org.objectweb.proactive.core.descriptor.data.VirtualNode proactive_vn =
                    ((ProActiveDescriptor) context.get("deployment-descriptor")).getVirtualNode(adlVN.getName());
                if (proactive_vn == null) {
                    if (adlVN.getName().equals("null")) {
                        logger.info(name +
                            " will be instantiated in the current virtual machine (\"null\" was specified as the virtual node name)");
                    } else {
                        throw new ADLException("Could not find virtual node  " +
                            adlVN.getName() + " in the deployment descriptor",
                            null);
                    }
                } else {
                    if (proactive_vn.isMultiple() &&
                            (adlVN.getCardinality().equals(VirtualNode.SINGLE))) {
                        // there will be only one instance of the component, on one node of the virtual node 
                        contentDesc.forceSingleInstance();
                    } else if (!(proactive_vn.isMultiple()) &&
                            (adlVN.getCardinality().equals(VirtualNode.MULTIPLE))) {
                        throw new ADLException(
                            "Cannot deploy on a single virtual node when the cardinality of this virtual node named " +
                            adlVN.getName() + " in the ADL is set to multiple",
                            null);
                    }
                }
                contentDesc.setVirtualNode(proactive_vn);
            }
        }

        Component result = Fractal.getGenericFactory(bootstrap).newFcInstance((ComponentType) type,
                controllerDesc, contentDesc);
        registry.addComponent(result); // the registry can handle groups

        return result;
    }
}
