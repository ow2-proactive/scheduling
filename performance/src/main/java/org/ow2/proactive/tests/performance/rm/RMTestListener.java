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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.rm;

import java.io.Serializable;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
import org.ow2.proactive.resourcemanager.frontend.RMGroupEventListener;


public class RMTestListener extends RMGroupEventListener implements Serializable {

    private static final long serialVersionUID = 61L;

    private RMEventListener eventsMonitor;

    public RMTestListener() {
    }

    public RMTestListener(RMEventListener eventsMonitor) {
        this.eventsMonitor = eventsMonitor;
    }

    public static RMTestListener createRMTestListener(RMEventListener eventsMonitor) throws Exception {
        RMTestListener listener = new RMTestListener(eventsMonitor);
        listener = PAActiveObject.turnActive(listener);
        return listener;
    }

    @Override
    public void rmEvent(RMEvent event) {
        eventsMonitor.rmEvent(event);
    }

    @Override
    public void nodeSourceEvent(RMNodeSourceEvent event) {
        eventsMonitor.nodeSourceEvent(event);
    }

    @Override
    public void nodeEvent(RMNodeEvent event) {
        eventsMonitor.nodeEvent(event);
    }

}
