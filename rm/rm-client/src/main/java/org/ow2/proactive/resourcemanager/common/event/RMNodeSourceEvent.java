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
package org.ow2.proactive.resourcemanager.common.event;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;


/**
 * This class implements the Event object related to a {@code NodeSource}.
 * <p>
 * This event objects is thrown to all Resource Manager Monitors to inform them
 * about a NodeSource event. Events can be:
 * <ul>
 *     <li>new NodeSource added to the {@code RMCore}</li>
 *     <li>the NodeSource removed from the {@code RMCore}</li>
 * </ul>
 *
 * A node source has to aspects in a Monitor's point of view a name, its sourceID.
 * NodeSource types are defined in {@link RMConstants}.
 *
 * @see RMMonitoring
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class RMNodeSourceEvent extends RMEvent {

    /** name of the source concerned by the event. */
    private String nodeSourceName = null;

    /** description of the source concerned by the event. */
    private String nodeSourceDescription = null;

    private String nodeSourceAdmin = null;

    /** string representation of the status of node source */
    private String nodeSourceStatus;

    /**
     * ProActive Empty constructor.
     */
    public RMNodeSourceEvent() {
    }

    /**
     * Creates an RMNodesourceEvent object.
     * Used to represent the resource manager state @see RMInitialState.
     */
    public RMNodeSourceEvent(String nodeSourceName, String nodeSourceDescription, String nodeSourceAdmin,
            String nodeSourceStatus) {
        this.nodeSourceName = nodeSourceName;
        this.nodeSourceDescription = nodeSourceDescription;
        this.nodeSourceAdmin = nodeSourceAdmin;
        this.nodeSourceStatus = nodeSourceStatus;
    }

    /**
     * Creates an RMNodesourceEvent object.
     */
    public RMNodeSourceEvent(RMEventType type, String initiator, String nodeSourceName, String nodeSourceDescription,
            String nodeSourceAdmin, String nodeSourceStatus) {
        super(type);
        this.initiator = initiator;
        this.nodeSourceName = nodeSourceName;
        this.nodeSourceDescription = nodeSourceDescription;
        this.nodeSourceAdmin = nodeSourceAdmin;
        this.nodeSourceStatus = nodeSourceStatus;
    }

    // for testing purpose RMinitialStateTest
    public RMNodeSourceEvent(String nodeSourceName, long counter) {
        this.nodeSourceName = nodeSourceName;
        this.counter = counter;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nodeSourceName == null) ? 0 : nodeSourceName.hashCode());
        return result;
    }

    /**
     * Compare two RMNodeSourceEvent objects.
     * @param obj RMNodeSourceEvent object to compare.
     * @return true if the two events represent the same NodeSource.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof RMNodeSourceEvent))
            return false;
        RMNodeSourceEvent other = (RMNodeSourceEvent) obj;
        if (nodeSourceName == null) {
            if (other.nodeSourceName != null)
                return false;
        } else if (!nodeSourceName.equals(other.nodeSourceName))
            return false;
        return true;
    }

    /**
     * Returns Node source name of the event.
     * @return node source name of the event.
     */
    public String getSourceName() {
        return this.nodeSourceName;
    }

    /**
     * Returns Node source type of the event.
     * @return node source type of the event.
     */
    public String getSourceDescription() {
        return this.nodeSourceDescription;
    }

    /**
     * Returns the name of the node source administrator.
     * @return the node source administrator name.
     */
    public String getNodeSourceAdmin() {
        return nodeSourceAdmin;
    }

    /**
     * Returns node source status under String representation
     * @return string value of node source status
     */
    public String getNodeSourceStatus() {
        return nodeSourceStatus;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getEventType() + ((counter > 0) ? " counter: " + counter + " " : "") + "[" + getSourceName() + "]";
    }
}
