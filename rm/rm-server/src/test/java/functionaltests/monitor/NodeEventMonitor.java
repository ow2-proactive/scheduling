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
package functionaltests.monitor;

import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;


public class NodeEventMonitor extends RMEventMonitor {

    private String nodeUrl;

    private RMNodeEvent nodeEvent;

    public NodeEventMonitor(RMEventType evt, String nodeUrl) {
        super(evt);
        this.nodeUrl = nodeUrl;
    }

    public NodeEventMonitor(RMEventType evt, String nodeUrl, RMNodeEvent event) {
        super(evt);
        this.nodeUrl = nodeUrl;
        this.nodeEvent = event;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            if (o instanceof NodeEventMonitor) {
                return (((NodeEventMonitor) o).getNodeUrl().equals(nodeUrl));
            }
        }
        return false;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String url) {
        nodeUrl = url;
    }

    public RMNodeEvent getNodeEvent() {
        return nodeEvent;
    }

    public void setNodeEvent(RMNodeEvent nodeEvent) {
        this.nodeEvent = nodeEvent;
    }

    public String toString() {
        if (nodeEvent != null) {
            return nodeEvent.toString();
        }

        return super.toString() + " " + nodeUrl;
    }

}
