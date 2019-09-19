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
package org.ow2.proactive.resourcemanager.common.event.dto;

import java.io.Serializable;
import java.util.List;

import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;


public class RMStateFull implements Serializable {

    private List<RMNodeSourceEvent> nodeSource;

    private List<RMNodeEvent> nodesEvents;

    /**
     * Current version of RM portal and maybe other clients expects "nodeSource" inside JSON
     *
     * @return list of RMNodeSourceEvent
     */
    public List<RMNodeSourceEvent> getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(List<RMNodeSourceEvent> nodeSource) {
        this.nodeSource = nodeSource;
    }

    /**
     * Current version of RM portal and maybe other clients expects "nodesEvents" inside JSON
     *
     * @return list of RMNodeEvent
     */
    public List<RMNodeEvent> getNodesEvents() {
        return nodesEvents;
    }

    public void setNodesEvents(List<RMNodeEvent> nodesEvents) {
        this.nodesEvents = nodesEvents;
    }
}
