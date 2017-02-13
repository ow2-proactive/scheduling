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

import static functionaltests.utils.RMTHelper.log;

import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.common.event.*;
import org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;


@ActiveObject
public class RMMonitorEventReceiver extends RMProxyUserInterface {

    private RMMonitorsHandler monitorsHandler;

    /**
     * empty constructor
     */
    public RMMonitorEventReceiver() {
    }

    public RMMonitorEventReceiver(RMMonitorsHandler monitor) {
        this.monitorsHandler = monitor;
    }

    public void nodeEvent(RMNodeEvent event) {
        log("RM Event [last: " + counter + "]: " + event);
        monitorsHandler.handleNodeEvent(event);
        super.nodeEvent(event);
    }

    public void nodeSourceEvent(RMNodeSourceEvent event) {
        log("RM Event [last: " + counter + "]: " + event);
        monitorsHandler.handleNodesourceEvent(event);
        super.nodeSourceEvent(event);
    }

    public void rmEvent(RMEvent event) {
        log("RM Event: [last: " + counter + "]: " + event);
        monitorsHandler.handleSchedulerStateEvent(event.getEventType());
        super.rmEvent(event);
    }

    @Override
    public RMMonitoring getMonitoring() {
        throw null;
    }

    public RMInitialState addRMEventListener(RMEventListener listener, RMEventType... events) {
        return super.getMonitoring().addRMEventListener(listener, events);
    }

    public void removeRMEventListener() throws RMException {
        monitorsHandler.clear();
        counter = 0;
        super.getMonitoring().removeRMEventListener();
    }

    public RMInitialState getInitialState() {
        return super.getMonitoring().getState();
    }
}
