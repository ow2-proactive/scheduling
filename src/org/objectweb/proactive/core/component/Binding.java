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
package org.objectweb.proactive.core.component;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Interface;

import org.objectweb.proactive.core.util.ProActiveLogger;

import java.io.Serializable;


/**
 * Stores a binding between a client interface and a server interface.
 * Only composite components have their bindings stored in such manner.
 * Primitive component handle their bindings themselves.
 * @author Matthieu Morel
 */
public class Binding implements Serializable {
    private static Logger logger = ProActiveLogger.getLogger("components");
    private final ProActiveInterface clientInterface;
    private final ProActiveInterface serverInterface;
    private final String clientInterfaceName;

    /**
     * @param clientInterface a reference on a client interface
     * @param serverInterface a reference on a server interface
     */
    public Binding(final Interface clientInterface, String clientItfName,
        final Interface serverInterface) {
        this.clientInterface = (ProActiveInterface) clientInterface;
        this.clientInterfaceName = clientItfName;
        this.serverInterface = (ProActiveInterface) serverInterface;
    }

    /**
     * @return the client interface
     */
    public Interface getClientInterface() {
        return clientInterface;
    }

    /**
     * @return the server interface
     */
    public Interface getServerInterface() {
        if (logger.isDebugEnabled()) {
            logger.debug("returning " + serverInterface.getClass().getName());
        }
        return serverInterface;
    }

    /**
     * In the case of a collective interface, the names
     * of each member of the collection can be different.
     * @return the actual name of the client interface, within this collection
     */
    public String getClientInterfaceName() {
        return clientInterfaceName;
    }
}
