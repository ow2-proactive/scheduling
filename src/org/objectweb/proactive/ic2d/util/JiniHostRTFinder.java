/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ic2d.util;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;


//import org.objectweb.proactive.core.node.jini.JiniNode;

/**
 * This class talks to ProActive nodes
 */
public class JiniHostRTFinder implements HostRTFinder {
    private IC2DMessageLogger logger;
	private DefaultListModel skippedObjects;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public JiniHostRTFinder(IC2DMessageLogger logger, DefaultListModel skippedObjects) {
        this.logger = logger;
        this.skippedObjects= skippedObjects;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * @see org.objectweb.proactive.ic2d.util.HostRTFinder#findPARuntimes(java.lang.String, int)
     */
    public ArrayList findPARuntimes(String host, int port)
        throws IOException {
        //we guess here that the port is fixed in Jini implementation
        JiniRTListener RTlist = new JiniRTListener(host, logger, skippedObjects );
        try {
            //          stay around long enough to receice replies
            Thread.sleep(10000L);
        } catch (java.lang.InterruptedException e) {
            // do nothing
        }
        return RTlist.getRuntimes();
    }
    //
    // -- implements NodeFinder -----------------------------------------------
    //
    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
}
