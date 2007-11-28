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
package org.objectweb.proactive.core.node;

import java.io.Serializable;

import org.objectweb.proactive.Job;
import org.objectweb.proactive.annotation.PublicAPI;
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
 * @See {@link VMInformation}
 */
@PublicAPI
public interface NodeInformation extends Serializable, Job {

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
     * Change the Job ID of this node.
     * @param jobId The new JobID
     *
     * TODO: Describe what Job ID is !
     */
    public void setJobID(String jobId);

    /**
     * Returns informations about the Runtime running this node
     * @see VMInformation
     */
    public VMInformation getVMInformation();
}
