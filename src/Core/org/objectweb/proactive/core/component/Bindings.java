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
package org.objectweb.proactive.core.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.ProActiveRuntimeException;


/**
 * A bindings container. This class stores the following bindings for a given
 * component :
 * <ul>
 * <li>thisComponent.clientInterface --> serverComponent.serverInterface (it
 * also takes in charge collective bindings, ie 1 client to several servers)
 * </li>
 * <li>thisParallelComponent.serverInterface -->
 * serverComponents.serverInterface (in the case of a parallel component,
 * requests on a server port are forwarded to the inner components)</li>
 * </ul>
 *
 * @author Matthieu Morel
 */
public class Bindings implements Serializable {
    //	 In case of collective bindings, the interfaces of the collection can be : 
    //	  1. named (as in Fractal 2.0 spec) : they are mapped in clientInterfaceBindings according to their name
    //	  2. anonymous : they are put in a list which is mapped in clientInterfaceBindings with the type name of the collective interface 
    private Map<String, Object> normalBindings; //values are Binding or List of Binding
    private Map<String, Object> exportBindings;

    // key = interfaceName ; value = binding
    // if collective binding : key = interfaceName ; value = Vector (Binding objects)
    public Bindings() {
        normalBindings = new HashMap<String, Object>();
        exportBindings = new HashMap<String, Object>();
    }

    /**
     * @param binding the binding to add
     */
    public void add(Binding binding) {
        try {
            InterfaceType client_itf_type = (InterfaceType) binding.getClientInterface().getFcItfType();

            // export bindings
            if (!client_itf_type.isFcClientItf()) {
                if (Fractive.getComponentParametersController(binding.getClientInterface().getFcItfOwner())
                        .getComponentParameters().getHierarchicalType().equals(Constants.PARALLEL)) {
                    addCollectiveBindingOnInternalClientItf(binding);
                } else {
                    exportBindings.put(binding.getClientInterfaceName(), binding);
                }
            } else {
                // normal bindings
                if (client_itf_type.isFcCollectionItf()) {
                    addCollectiveBindingOnExternalClientItf(binding);
                } else {
                    normalBindings.put(binding.getClientInterfaceName(), binding);
                }
            }
        } catch (NoSuchInterfaceException nsie) {
            throw new ProActiveRuntimeException("interface not found : " + nsie.getMessage());
        }
    }

    // returns either a Binding or a List of Binding objects (collection interface case)

    /**
     * removes the binding on the given client interface
     * @param clientItfName String name of the binding
     * @return Object binding that was removed
     */
    public Object remove(String clientItfName) {
        if (normalBindings.containsKey(clientItfName)) {
            return normalBindings.remove(clientItfName);
        }
        if (exportBindings.containsKey(clientItfName)) {
            return exportBindings.remove(clientItfName);
        }
        return null;
    }

    /**
     * Method get.
     * @param clientItfName String
     * @return Object
     */
    public Object get(String clientItfName) {
        if (normalBindings.containsKey(clientItfName)) {
            return normalBindings.get(clientItfName);
        }
        if (exportBindings.containsKey(clientItfName)) {
            return exportBindings.get(clientItfName);
        }
        return null;
    }

    /**
     * tests if binding exists on the given interface
     * @param clientItfName the client inteface to check
     * @return true if binding exists
     */
    public boolean containsBindingOn(String clientItfName) {
        return (normalBindings.containsKey(clientItfName) || exportBindings.containsKey(clientItfName));
    }

    /**
     * Returns the names of the external client bindings for this component.
     * In case of a collective interface, the names of each of its constituting interfaces are not returned ;
     * only the name of the collective interface is returned.
     */
    public String[] getExternalClientBindings() {
        return normalBindings.keySet().toArray(new String[normalBindings.keySet().size()]);
    }

    /**
     * @param bindingsTable Map map that stores the bindings
     * @param binding Binding the binding to add
     */
    private static void addCollectiveBinding(Map<String, Object> bindingsTable, Binding binding) {
        String client_itf_name = binding.getClientInterfaceName();
        if (binding.getClientInterface().getFcItfName().equals(client_itf_name)) {
            if (bindingsTable.containsKey(client_itf_name)) {
                // there should be a List for containing the bindings associated
                ((List<Binding>) bindingsTable.get(client_itf_name)).add(binding);
            } else { // we create a List for keeping the bindings
                ArrayList<Binding> bindings_collection = new ArrayList<Binding>();
                bindings_collection.add(binding);
                bindingsTable.put(client_itf_name, bindings_collection);
            }
        } else {
            bindingsTable.put(client_itf_name, binding);
        }
    }

    /**
     *
     * @param binding Binding the binding to add
     */
    private void addCollectiveBindingOnExternalClientItf(Binding binding) {
        addCollectiveBinding(normalBindings, binding);
    }

    /**
     * @param binding Binding
     */
    private void addCollectiveBindingOnInternalClientItf(Binding binding) {
        if (exportBindings == null) {
            exportBindings = new HashMap<String, Object>();
        }
        addCollectiveBinding(exportBindings, binding);
    }
}
