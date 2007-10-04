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
package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extra.scheduler.common.scripting.VerifyingScript;


/**
 * An interface Front-End for the User to communicate with
 * the Infrastructure Manager
 */
public interface IMUser {
    // for testing
    public StringWrapper echo();

    /**
     * Reserves nb nodes verifying the verifying script,
     * if the infrastructure manager (IM) don't have nb free nodes
     * then it returns the max of valid free nodes
     * @param nb the number of nodes
     * @param verifyingScript : script to be verified by the returned nodes
     * @return an arraylist of nodes
     */
    public NodeSet getAtMostNodes(IntWrapper nbNodes,
        VerifyingScript verifyingScript);

    /**
     * Reserves nb nodes verifying the verifying script,
     * if the infrastructure manager (IM) don't have nb free nodes
     * then it returns an empty node set.
     * @param nb the number of nodes
     * @param verifyingScript : script to be verified by the returned nodes
     * @return an arraylist of nodes
     */
    public NodeSet getExactlyNodes(IntWrapper nbNodes,
        VerifyingScript verifyingScript);

    /**
     * Release the node reserve by the user
     * @param node : the node to release
     * @param postScript : script to execute before releasing the node
     */
    public void freeNode(Node node);

    /**
     * Release the nodes reserve by the user
     * @param nodes : a table of nodes to release
     * @param postScript : script to execute before releasing the nodes
     */
    public void freeNodes(NodeSet nodes);
}
