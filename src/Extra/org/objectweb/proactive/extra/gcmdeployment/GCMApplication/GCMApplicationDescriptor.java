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
package org.objectweb.proactive.extra.gcmdeployment.GCMApplication;

import java.util.Map;
import java.util.NoSuchElementException;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNode;


/**
 * GCM Application Descriptor public interface
 *
 * This interface is exported to ProActive user and allow them to
 * control and manage a GCM Application Descriptor. For example, this
 * interface must be used to retrieve the Virtual Nodes.
 *
 * @author cmathieu
 *
 */
@PublicAPI
public interface GCMApplicationDescriptor {
    public void startDeployment();

    /**
     * Returns the Virtual Node associated to this name
     *
     * @param vnName a Virtual Node name declared inside the GCM Application Descriptor
     * @return the VirtualNode associated to vnName or null if the Virtual Node does not exist
     */
    public VirtualNode getVirtualNode(String vnName);

    /**
     * Returns all the Virtual Nodes declared inside a GCM Application Descriptor
     *
     * Keys are the Virtual Node names and values the Virtual Nodes.
     *
     * @return All the Virtual Nodes declared inside the GCM Application Descriptor.
     */
    public Map<String, ?extends VirtualNode> getVirtualNodes();

    /**
     * Kills all Nodes and JVMs(local or remote) created when activating the GCM Application Descriptor
     *
     * @param softly if false, all JVMs created when activating the descriptor are killed abruptely
     * if true a JVM that originates the creation of  a rmi registry waits until registry is empty before
     * dying. To be more precise a thread is created to ask periodically the registry if objects are still
     * registered.
     * @throws ProActiveException if a problem occurs when terminating all jvms
     */
    public void kill();

    /**
     *
     * @return true is returned if all processes started by GCM Deployment have
     * exited. false is returned otherwise
     */
    public boolean allProcessExited();

    /**
     * Wait for all process
     */
    public void awaitTermination();
}
