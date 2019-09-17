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
package org.ow2.proactive.resourcemanager.frontend;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;


/**
 * Interface for RM events monitoring.
 * Interface and methods to implements for a object that want
 * to receive (monitor) Resource manager's (RM) events.
 *
 * RM Events are defined in {@link RMEventType}.
 *
 * @see org.ow2.proactive.resourcemanager.frontend.RMMonitoring
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
@PublicAPI
public interface RMEventListener {

    /** RM is shutting down
     * @param event object representing the event.
     */
    void rmEvent(RMEvent event);

    /** new node source available in RM.
     * @param event node source event containing new {@code NodeSource} properties.
     */
    void nodeSourceEvent(RMNodeSourceEvent event);

    /** new node available in RM.
     * @param event node event containing new {@code RMNode} properties.
     */
    void nodeEvent(RMNodeEvent event);
}
