/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
import org.ow2.proactive.resourcemanager.common.event.*;
import org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;

import static functionaltests.utils.RMTHelper.log;


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
