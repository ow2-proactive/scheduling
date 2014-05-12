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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.monitor;

import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMGroupEventListener;

import functionaltests.RMTHelper;


@ActiveObject
public class RMMonitorEventReceiver extends RMGroupEventListener {

    private RMMonitorsHandler monitorsHandler;

    /**
     * empty constructor
     */
    public RMMonitorEventReceiver() {
    }

    /**
     * @param monitor SchedulerMonitorsHandler object which is notified
     * of Schedulers events.
     */
    public RMMonitorEventReceiver(RMMonitorsHandler monitor) {
        this.monitorsHandler = monitor;
    }

    public void nodeEvent(RMNodeEvent event) {
        RMTHelper.log("Event: " + event);
        monitorsHandler.handleNodeEvent(event);
    }

    public void nodeSourceEvent(RMNodeSourceEvent event) {
        RMTHelper.log("Event: " + event);
        monitorsHandler.handleNodesourceEvent(event);
    }

    public void rmEvent(RMEvent event) {
        RMTHelper.log("Event: " + event);
        monitorsHandler.handleSchedulerStateEvent(event.getEventType());
    }
}
