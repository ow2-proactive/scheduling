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

import java.io.Serializable;


import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Interface;


/** Stores a binding between a client interface and a server interface.
 * @author Matthieu Morel
 */
public class Binding implements Serializable {
    protected static Logger logger = Logger.getLogger(Binding.class.getName());
    private final Interface clientInterface;
    private final Interface serverInterface;

    /**
     * @param clientInterface a reference on a client interface
     * @param serverInterface a reference on a server interface
     */    
    public Binding(final Interface clientInterface, final Interface serverInterface) {
        this.clientInterface = clientInterface;
        this.serverInterface = serverInterface;
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
}