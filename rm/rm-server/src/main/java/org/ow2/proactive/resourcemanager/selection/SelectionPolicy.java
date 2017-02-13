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
package org.ow2.proactive.resourcemanager.selection;

import java.util.List;

import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;


/**
 * This interface represents pluggable selection policies that are used
 * after we filtered out all nodes that cannot be used by given client
 * and before we execute scripts.
 * 
 * In combination with selection scripts it makes sense only for unknown scripts.
 * For all others node will be resorted according to previous execution results.
 * 
 * Every subclass of this interface must have a default constructor.
 * 
 * Examples:
 *  - node shuffling for balancing load among nodes
 *  - prioritizing node sources for using local nodes first
 * 
 */
public interface SelectionPolicy {

    /**
     * Arranges nodes before selection by the resource manager.
     * The first nodes in the results list will be selected. 
     * 
     * Note: input list can be modified in place instead of creating a new list (optimization).
     * 
     * @param number of required nodes
     * @param nodes to be sorted
     * @param client for who the request is performed
     * 
     * @return the sorted list of nodes.
     */
    List<RMNode> arrangeNodes(int number, List<RMNode> nodes, Client client);
}
