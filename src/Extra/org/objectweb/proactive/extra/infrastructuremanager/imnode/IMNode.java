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
package org.objectweb.proactive.extra.infrastructuremanager.imnode;

import java.util.HashMap;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMNodeEvent;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptResult;
import org.objectweb.proactive.extra.scheduler.common.scripting.SelectionScript;


/**
 * The <I>IMNode</I> is a object containing a node and its description.<BR/>
 */
public interface IMNode extends Comparable<IMNode> {
    // nodes states
    public static final int FREE = 0;
    public static final int BUSY = 1;
    public static final int DOWN = 2;
    public static final int TO_BE_RELEASED = 3;

    /**
     * The script has been executed on this node, and the result was negative.
     */
    public static final int NOT_VERIFIED_SCRIPT = 0;

    /**
     * The script has already responded by the negative,
     * but something has been executed on the node since.
     */
    public static final int NO_LONGER_VERIFIED_SCRIPT = 1;

    /**
     * The script has never been tested on this {@link IMNode}.
     */
    public static final int NEVER_TESTED = 2;

    /**
     * The script is verified on this node,
     * but something has been executed since the time it has been tested.
     */
    public static final int ALREADY_VERIFIED_SCRIPT = 3;

    /**
     * The script is verified, and nothing
     * has been executed since the verification.
     */
    public static final int VERIFIED_SCRIPT = 4;

    // SCRIPTING
    /**
     * Execute a {@link SelectionScript} on this {@link IMNode}
     * @return the {@link ScriptResult} corresponding to the script execution.
     */
    public ScriptResult<Boolean> executeScript(SelectionScript script);

    /**
     * Get a map of all selection script already tested on this node,
     * and the responses given.
     * @return the map of Script and status
     */
    public HashMap<SelectionScript, Integer> getScriptStatus();

    // ----------------------------------------------------------------------//
    // GET

    /**
     * @return the name of the node
     */
    public String getNodeName();

    /**
     * @return Node
     */
    public Node getNode() throws NodeException;

    /**
     * @return the node information, describing this node
     * @see org.objectweb.proactive.core.node.NodeInformation
     */
    public NodeInformation getNodeInformation() throws NodeException;

    /**
     * @return the name of the virtual node
     */
    public String getVNodeName();

    /**
     * @return the name of the ProActive descriptor
     */
    public String getPADName();

    /**
     * This method call nodeInformation.getHostName();
     *
     * @return the name of the host machine
     */
    public String getHostName();

    /**
     * This method call nodeInformation.getDescriptorVMName();
     *
     * @return the name of the virtual machine
     */
    public String getDescriptorVMName();

    public String getNodeSourceId();

    public String getNodeURL();

    // ----------------------------------------------------------------------//
    // STATE
    public boolean isFree();

    public boolean isDown();

    public boolean isToRelease();

    public boolean isBusy();

    // ----------------------------------------------------------------------//
    // SET
    public void setFree() throws NodeException;

    public void setBusy() throws NodeException;

    public void setToRelease() throws NodeException;

    public void setDown();

    // OTHER SET in the case of the node can migrate.
    // For example if the node migrate from other jvm, you must change
    // the attribute JVM, VNode, ...

    // ----------------------------------------------------------------------//
    // TOSTRING

    /**
     * @return a string describing the IMNode (status, vnode, host, pad, ...)
     */
    public String toString();

    /**
     * Cleaning method : remove all active objects on this node.
     */
    public void clean();

    // NODE SOURCES
    /**
     * The {@link IMNodeSource} from where the IMNode is issued.
     */
    public NodeSource getNodeSource();

    /**
     * Change the {@link IMNodeSource} from where the node is.
     * @param nodeSource
     */
    public void setNodeSource(NodeSource nodeSource);

    public void setVerifyingScript(SelectionScript script);

    public void setNotVerifyingScript(SelectionScript script);

    /**
     *  build NodeEvent object for the IMNode */
    public IMNodeEvent getNodeEvent();
}
