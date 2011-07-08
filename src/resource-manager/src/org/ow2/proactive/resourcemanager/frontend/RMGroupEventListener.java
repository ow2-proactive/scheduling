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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.frontend;

import java.util.Collection;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;


/**
 *
 * A listener of the resource manager which can receive a collection of events at once.
 * It is an optimization for the low latency networks when the price of sending
 * events sequentially is too high.
 *
 */
@PublicAPI
public abstract class RMGroupEventListener implements RMEventListener {

    public void notify(Collection<RMEvent> events) {
        for (RMEvent event : events) {
            if (event instanceof RMNodeEvent) {
                RMNodeEvent nodeEvent = (RMNodeEvent) event;
                nodeEvent(nodeEvent);
            } else if (event instanceof RMNodeSourceEvent) {
                RMNodeSourceEvent sourceEvent = (RMNodeSourceEvent) event;
                nodeSourceEvent(sourceEvent);
            } else {
                rmEvent(event);
            }
        }
    }
}
