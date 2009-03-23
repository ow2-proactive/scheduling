/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task.internal;

import java.io.Serializable;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;
import org.ow2.proactive.utils.NodeSet;


/**
 * Internal and global description of a task.
 * This class contains all informations about the task to launch.
 * It also provides a method to create its own launcher.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class ExecuterInformations implements Serializable {

    /** Reference to the launcher of this task. */
    private TaskLauncher launcher;

    /** Reference to the node */
    private NodeSet nodes;

    /** Reference to the node name of this task. */
    private String nodeName;

    /** Reference to the node host of this task. */
    private String hostName;

    /**
     * Create a new executer informations with the given info.
     *
     * @param launcher the active object on which the task has been launched
     * @param node the node on which the active object has been launched.
     */
    public ExecuterInformations(TaskLauncher launcher, Node node) {
        this.launcher = launcher;
        this.nodes = new NodeSet();
        this.nodes.add(node);
        this.nodeName = node.getNodeInformation().getName();
        this.hostName = node.getNodeInformation().getVMInformation().getHostName();
    }

    /**
     * Returns the launcher.
     *
     * @return the launcher.s
     */
    public TaskLauncher getLauncher() {
        return launcher;
    }

    /**
     * Returns the nodes.
     *
     * @return the nodes.
     */
    public NodeSet getNodes() {
        return nodes;
    }

    /**
     * Add new nodes to the current nodeSet.
     * 
     * @param nodes the new nodes to add.
     */
    public void addNodes(NodeSet nodes) {
        this.nodes.addAll(nodes);
    }

    /**
     * Returns the node name
     *
     * @return the node name.
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Returns the host name
     *
     * @return the host name.
     */
    public String getHostName() {
        return hostName;
    }
}
