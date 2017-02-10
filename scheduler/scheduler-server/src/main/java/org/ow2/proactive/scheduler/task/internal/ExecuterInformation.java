/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.task.internal;

import java.io.Serializable;
import java.util.LinkedList;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.utils.NodeSet;


/**
 * Internal and global description of a task.
 * This class contains all information about the task to launch.
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
