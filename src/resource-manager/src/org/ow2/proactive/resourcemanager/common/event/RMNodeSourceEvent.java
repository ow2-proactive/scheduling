/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
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
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;


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

    /** name of the source concerned by the event. */
    private String nodeSourceName = null;

    /** description of the source concerned by the event. */
    private String nodeSourceDescription = null;

    /**
     * ProActive Empty constructor.
     */
    public RMNodeSourceEvent() {
    }

    /**
     * Creates an RMNodesourceEvent object.
     * @param name of the Node Source
     * @param type type of the NodeSource
     */
    public RMNodeSourceEvent(String name, String description) {
        this.nodeSourceName = name;
        this.nodeSourceDescription = description;
    }

    /**
     * Compare two RMNodeSourceEvent objects.
     * @param obj RMNodeSourceEvent object to compare.
     * @return true if the two events represent the same NodeSource.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RMNodeSourceEvent) {
            return ((RMNodeSourceEvent) obj).nodeSourceName.equals(this.nodeSourceName) &&
                ((RMNodeSourceEvent) obj).nodeSourceDescription.equals(this.nodeSourceDescription);
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
        return this.nodeSourceDescription;
    }
}
