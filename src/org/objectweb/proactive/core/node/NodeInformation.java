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
package org.objectweb.proactive.core.node;

import org.objectweb.proactive.core.runtime.DeployerTag;
import org.objectweb.proactive.core.runtime.VMInformation;


/**
 * <p>
 * A class implementing this interface provides information about the node it is attached to.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public interface NodeInformation extends VMInformation {

    /**
     * Returns the name of the node
     * @return the name of the node
     */
    public String getName();

    /**
     * Returns the protocol of the node
     * @return the protocol of the node
     */
    public String getProtocol();

    /**
     * Returns the complete URL of the node in the form <code>protocol://host/nodeName</code>
     * @return the complete URL of the node
     */
    public String getURL();

    /**
     * Returns the host where the node has been created
     * @return the host where the node has been created
     */
    public String getHostName();

    /**
     * Change the Job ID of this node. Used for P2P.
     * @param jobId The new JobID
     */
    public void setJobID(String jobId);

    /**
     * Returns the deployer tag of this node
     * @return the deployer tag of this node
     */
    public DeployerTag getDeployerTag();
}
