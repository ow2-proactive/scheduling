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
package org.ow2.proactive.resourcemanager.common.event;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.db.types.BigString;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;


/**
 * This class implements the Event object related to a {@link NodeSource}
 * This event objects is thrown to all Resource Manager Monitors to inform them
 * about a NodeSource event. Events can be :<BR>
 * -new NodeSource added to the {@link RMCore}.<BR>
 * -NodeSource removed from the {@link RMCore}.<BR><BR>
 *
 * A node source has to aspects in a Monitor's point of view :<BR>
 * -A name, its sourceID.<BR>
 * NodeSource types are defined in {@link RMConstants}.
 *
 * @see RMMonitoring
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
@PublicAPI
public class RMNodeSourceEvent extends RMEvent {

    private static final long serialVersionUID = 60L;

    /** name of the source concerned by the event. */
    private String nodeSourceName = null;

    /** description of the source concerned by the event. */
    private BigString nodeSourceDescription = null;

    private String nodeSourceAdmin = null;

    /**
     * ProActive Empty constructor.
     */
    public RMNodeSourceEvent() {
    }

    /**
     * Creates an RMNodesourceEvent object.
     * Used to represent the resource manager state @see RMInitialState.
     */
    public RMNodeSourceEvent(String nodeSourceName, String nodeSourceDescription, String nodeSourceAdmin) {
        this.nodeSourceName = nodeSourceName;
        this.nodeSourceDescription = new BigString(nodeSourceDescription);
        this.nodeSourceAdmin = nodeSourceAdmin;
    }

    /**
     * Creates an RMNodesourceEvent object.
     */
    public RMNodeSourceEvent(RMEventType type, String initiator, String nodeSourceName,
            String nodeSourceDescription, String nodeSourceAdmin) {
        super(type);
        this.initiator = initiator;
        this.nodeSourceName = nodeSourceName;
        this.nodeSourceDescription = new BigString(nodeSourceDescription);
        this.nodeSourceAdmin = nodeSourceAdmin;
    }

    /**
     * Compare two RMNodeSourceEvent objects.
     * @param obj RMNodeSourceEvent object to compare.
     * @return true if the two events represent the same NodeSource.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RMNodeSourceEvent) {
            return ((RMNodeSourceEvent) obj).nodeSourceName.equals(this.nodeSourceName);
        }
        return false;
    }

    /**
     * Returns the {@link NodeSource} name of the event.
     * @return node source name of the event.
     */
    public String getSourceName() {
        return this.nodeSourceName;
    }

    /**
     * Returns the {@link NodeSource} type of the event.
     * @return node source type of the event.
     */
    public String getSourceDescription() {
        return this.nodeSourceDescription.getValue();
    }

    /**
     * Returns the name of the node source administrator.
     * @return the node source administrator name.
     */
    public String getNodeSourceAdmin() {
        return nodeSourceAdmin;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getEventType() + ((counter > 0) ? " counter: " + counter + " " : "") + "[" +
            getSourceName() + "]";
    }
}
