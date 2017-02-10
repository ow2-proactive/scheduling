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
package org.ow2.proactive.resourcemanager.selection.policies;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.selection.SelectionPolicy;


/**
 * The selection policy that shuffles nodes to balance the load
 * between them.
 * 
 */
public class ShufflePolicy implements SelectionPolicy {

    private final static Logger logger = Logger.getLogger(ShufflePolicy.class);

    /**
     * Do shuffling here.
     * 
     * @return the same list but shuffled (allowed by interface specification).
     */
    public List<RMNode> arrangeNodes(int number, List<RMNode> nodes, Client client) {
        logger.debug("Shuffling nodes before selection");
        Collections.shuffle(nodes);
        return nodes;
    }
}
