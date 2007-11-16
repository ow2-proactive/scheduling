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
package org.objectweb.proactive.extra.gcmdeployment.core;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.FileTransferBlock;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;


public interface VirtualNodeInternal extends VirtualNode {

    /**
     * Adds a File Transfer Block to be executed before a node
     * is returned to the appplication
     *
     * @param ftb A File Transfer Block
     */
    public void addFileTransfertBlock(FileTransferBlock ftb);

    public void addProvider(GCMDeploymentDescriptor provider, long capacity);

    /**
     * Checks that all required informations are here.
           *
     * Checked things are notably:
     * <ul>
     *         <li>At least one resourceProvider at root level</li>
     *         <li>At least one resourceProvider inside each VirtualNode</li>
     * </ul>
     *
     * @throws IllegalStateException If something is missing
     */
    public void checkDirectMode() throws IllegalStateException;

    public boolean doesNodeProviderNeed(Node Node,
        GCMDeploymentDescriptor nodeProvider);

    public boolean doYouNeed(Node node, GCMDeploymentDescriptor nodeProvider);

    public boolean doYouWant(Node node, GCMDeploymentDescriptor nodeProvider);

    public boolean isGreedy();

    public boolean needNode();
}
