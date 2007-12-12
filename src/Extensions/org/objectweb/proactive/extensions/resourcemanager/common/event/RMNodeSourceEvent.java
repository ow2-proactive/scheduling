/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extensions.resourcemanager.common.event;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extensions.resourcemanager.core.RMCore;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMMonitoring;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.dynamic.P2PNodeSource;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.pad.PADNodeSource;


/**
 * This class implements Event object related to a {@link NodeSource}
 * This event objects is thrown to all Resource Manager Monitors to inform them
 * about a NodeSource event. Events can be :<BR>
 * -new NodeSource added to the {@link RMCore}.<BR>
 * -NodeSource removed from the {@link RMCore}.<BR><BR>
 *
 * A node source has to aspects in a Monitor's point of view :<BR>
 * -A name, its sourceID.<BR>
 * -A type : {@link PADNodeSource}, {@link P2PNodeSource}...<BR>
 * NodeSource types are defined in {@link RMConstants}.
 *
 * @see RMMonitoring
 *
 * @author ProActive team.
 *
 */
@PublicAPI
public class RMNodeSourceEvent extends RMEvent {

    /** serial version UID */
    private static final long serialVersionUID = -8939602445052143312L;

    /** name of the source concerned by the event. */
    private String nodeSourceName = null;

    /** type of the source concerned by the event. */
    private String nodeSourceType = null;

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
    public RMNodeSourceEvent(String name, String type) {
        this.nodeSourceName = name;
        this.nodeSourceType = type;
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
            ((RMNodeSourceEvent) obj).nodeSourceType.equals(this.nodeSourceType);
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
    public String getSourceType() {
        return this.nodeSourceType;
    }
}
