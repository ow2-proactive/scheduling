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
package org.ow2.proactive.resourcemanager.cleaning;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;


/**
 * Cleans a single node by killing all active objects on the node.
 *
 */
public class NodeCleaner implements Callable<Boolean> {

    private final static Logger logger = Logger.getLogger(NodeCleaner.class);

    private RMNode rmnode;

    /**
     * Construct the new instance of the class
     *
     * @param rmnode node to be cleaned
     */
    public NodeCleaner(RMNode rmnode) {
        this.rmnode = rmnode;
    }

    /**
     * Performs the cleaning (for now just killing of all active objects).
     * @return true if successfully cleaned, false in case of any exception (node will be marked as down in this case)
     */
    public Boolean call() throws Exception {
        // killing all active objects on the node
        try {
            rmnode.clean();
        } catch (Throwable t) {
            logger.warn("Exception while cleaning the node " + rmnode.getNodeURL() + ": " + t.getMessage(), t);
            logger.warn("Checking if the node " + rmnode.getNodeURL() + " is alive");
            rmnode.getNodeSource().pingNode(rmnode.getNode());
            return false;
        }

        return true;
    }
}
