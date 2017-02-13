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
package functionaltests;

import java.io.Serializable;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMGroupEventListener;


public class RMEventListener extends RMGroupEventListener implements Serializable {

    private RMEventMonitor eventMonitor;

    public static RMEventListener createEventListener(RMEventMonitor eventMonitor) throws Exception {
        RMEventListener listener = new RMEventListener(eventMonitor);
        return PAActiveObject.turnActive(listener);
    }

    public RMEventListener() {
    }

    public RMEventListener(RMEventMonitor eventMonitor) {
        this.eventMonitor = eventMonitor;
    }

    @Override
    public void nodeEvent(RMNodeEvent nodeEvent) {
        this.eventMonitor.nodeEvent(nodeEvent);
    }

    @Override
    public void nodeSourceEvent(RMNodeSourceEvent nodeSourceEvent) {
        this.eventMonitor.nodeSourceEvent(nodeSourceEvent);

    }

    @Override
    public void rmEvent(RMEvent rmEvent) {
        this.eventMonitor.rmEvent(rmEvent);
    }

}
