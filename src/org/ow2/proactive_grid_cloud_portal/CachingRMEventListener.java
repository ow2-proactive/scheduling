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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal;

import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;

public class CachingRMEventListener implements RMEventListener {

    protected RMMonitoringImpl rmMonitoring;
    protected RMInitialState rmInitialState;
    protected RMEventType RMstate;


    public void rmEvent(RMEvent event) {
        switch (event.getEventType()) {
            case ALIVE:
                RMstate = RMEventType.ALIVE;
                break;
            case SHUTTING_DOWN:
                RMstate = RMEventType.SHUTTING_DOWN;
                break;
            case SHUTDOWN:
                RMstate = RMEventType.SHUTDOWN;
                break;
        }

    }

    public void nodeSourceEvent(RMNodeSourceEvent event) {
       switch (event.getEventType()) {
           case NODESOURCE_CREATED:
               rmInitialState.getNodeSource().add(event);
               break;
           case NODESOURCE_REMOVED:
               for (int i = 0 ; i < rmInitialState.getNodeSource().size(); i++) {
                   if (rmInitialState.getNodeSource().get(i).getSourceName().equals(event.getSourceName())) {
                       rmInitialState.getNodeSource().remove(i);
                       break;
                   }
               }
               break;
       }

    }

    public void nodeEvent(RMNodeEvent event) {
        switch (event.getEventType()) {
            case NODE_REMOVED:
                for (int i = 0; i < rmInitialState.getNodesEvents().size(); i++) {
                    if (event.getNodeUrl().equals(rmInitialState.getNodesEvents().get(i))) {
                        rmInitialState.getNodesEvents().remove(i);
                        break;
                    }
                }
                break;
            case NODE_ADDED:
                rmInitialState.getNodesEvents().add(event);
                break;
            case NODE_STATE_CHANGED:
                for (int i = 0; i < rmInitialState.getNodesEvents().size(); i++) {
                    if (event.getNodeUrl().equals(rmInitialState.getNodesEvents().get(i))) {
                        rmInitialState.getNodesEvents().set(i,event);
                        break;
                    }
                }
                break;


        }

      }



}
