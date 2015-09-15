/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
import java.util.LinkedList;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.utils.NodeSet;


/**
 * Internal and global description of a task.
 * This class contains all informations about the task to launch.
 * It also provides a method to create its own launcher.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class ExecuterInformation implements Serializable {

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
    public ExecuterInformation(TaskLauncher launcher, Node node) {
        this.launcher = launcher;
        this.nodes = new NodeSet();
        this.nodes.add(node);
        this.nodeName = node.getNodeInformation().getName();
        this.hostName = node.getVMInformation().getHostName();
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
        if (nodes.getExtraNodes() != null) {
            if (this.nodes.getExtraNodes() == null) {
                this.nodes.setExtraNodes(new LinkedList<Node>());
            }
            this.nodes.getExtraNodes().addAll(nodes.getExtraNodes());
        }
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
