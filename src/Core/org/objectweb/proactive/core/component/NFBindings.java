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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


/**
 * Extended bindings class, containing more information about bindings inside the mmebrane
 * @author The ProActive Team
 *
 */
public class NFBindings {

    private Map<String, NFBinding> serverAliasBindings;
    private Map<String, NFBinding> clientAliasBindings;
    private Map<String, NFBinding> normalBindings;

    public NFBindings() {
        serverAliasBindings = new HashMap<String, NFBinding>();
        clientAliasBindings = new HashMap<String, NFBinding>();
        normalBindings = new HashMap<String, NFBinding>();
    }

    public void addServerAliasBinding(NFBinding b) {
        serverAliasBindings.put(b.getClientInterfaceName(), b);

    }

    public void addClientAliasBinding(NFBinding b) {
        clientAliasBindings.put(b.getClientInterfaceName(), b);

    }

    public void addNormalBinding(NFBinding b) {
        normalBindings.put(b.getClientInterfaceName(), b);
    }

    public Object remove(String clientItfName) {

        if (serverAliasBindings.containsKey(clientItfName)) {
            return serverAliasBindings.remove(clientItfName);
        }
        if (clientAliasBindings.containsKey(clientItfName)) {
            return clientAliasBindings.remove(clientItfName);
        }
        if (normalBindings.containsKey(clientItfName)) {
            return normalBindings.remove(clientItfName);
        }

        return null;
    }

    public Object get(String clientItfName) {

        if (serverAliasBindings.containsKey(clientItfName)) {
            return serverAliasBindings.get(clientItfName);
        }
        if (clientAliasBindings.containsKey(clientItfName)) {
            return clientAliasBindings.get(clientItfName);
        }
        if (normalBindings.containsKey(clientItfName)) {
            return normalBindings.get(clientItfName);
        }

        return null;
    }

    public boolean containsBindingOn(String clientItfName) {

        return (serverAliasBindings.containsKey(clientItfName) ||
            clientAliasBindings.containsKey(clientItfName) || normalBindings.containsKey(clientItfName));
    }

    public boolean hasServerAliasBindingOn(String component, String itf) {
        Vector<NFBinding> v = new Vector<NFBinding>(serverAliasBindings.values());
        for (NFBinding val : v) {
            if (val.getServerComponentName().equals(component) && val.getServerInterface().equals(itf)) {
                return true;
            }

        }
        return false;
    }

    public boolean hasServerAliasBindingOn(String component) {//Returns true when there is an alias binding on the component, the name of which is passed as an argument
        Vector<NFBinding> v = new Vector<NFBinding>(serverAliasBindings.values());
        for (NFBinding val : v) {
            if (val.getServerComponentName().equals(component)) {
                return true;
            }
        }
        return false;
    }

    public void removeNormalBinding(String component, String itf) {
        Vector<NFBinding> v = new Vector<NFBinding>(normalBindings.values());
        for (NFBinding val : v) {
            if (val.getServerComponentName().equals(component) && val.getClientInterface().equals(itf)) {
                normalBindings.remove(val.getClientInterfaceName());
            }
        }

    }

    public void removeClientAliasBinding(String component, String itf) {//Removes a client alias binding. The component name and the interface name belong to the component on the client side.

        Vector<NFBinding> v = new Vector<NFBinding>(clientAliasBindings.values());
        for (NFBinding val : v) {
            if (val.getServerComponentName().equals(component) && val.getClientInterface().equals(itf)) {
                clientAliasBindings.remove(val.getClientInterfaceName());
            }
        }

    }

    public void removeServerAliasBindingsOn(String component) {
        Vector<NFBinding> v = new Vector<NFBinding>(serverAliasBindings.values());
        for (NFBinding val : v) {
            if (val.getServerComponentName().equals(component)) {
                serverAliasBindings.remove(val.getClientInterfaceName());
            }
        }

    }

    public boolean hasBinding(String clientComponent, String clientItf, String serverComponent,
            String serverItf) {

        Vector<NFBinding> v = new Vector<NFBinding>(serverAliasBindings.values());

        for (NFBinding val : v) {
            if (val.getClientComponentName().equals(clientComponent) &&
                val.getClientInterfaceName().equals(clientItf) &&
                val.getServerComponentName().equals(serverComponent) &&
                val.getServerInterface().equals(serverItf)) {
                return true;
            }

        }

        return false;
    }

    public Vector<NFBinding> getServerAliasBindingsOn(String component, String itf) {
        Vector<NFBinding> v = new Vector<NFBinding>(serverAliasBindings.values());
        Vector<NFBinding> result = new Vector<NFBinding>();
        for (NFBinding val : v) {
            if (val.getServerComponentName().equals(component) && val.getServerInterface().equals(itf)) {
                result.add(val);
            }

        }
        return result;
    }

}
