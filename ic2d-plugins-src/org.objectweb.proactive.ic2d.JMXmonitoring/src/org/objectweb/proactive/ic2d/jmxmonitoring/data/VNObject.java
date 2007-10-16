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
package org.objectweb.proactive.ic2d.jmxmonitoring.data;

import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.jmx.naming.FactoryName;


public class VNObject extends AbstractData {
    private WorldObject parent;

    /** The virtual node name */
    private String name;

    /** The virtual node job ID */
    private String jobID;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a nes VNObject
     * @param name
     * @param jobID
     * @param world
     */
    protected VNObject(String name, String jobID, WorldObject world) {
        super(FactoryName.createVirtualNodeObjectName(name, jobID));

        this.parent = world;
        this.name = name;
        this.jobID = jobID;
    }

    //
    // -- PUBLIC METHOD -----------------------------------------------
    //
    @Override
    public void explore() { /* Do nothing */
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getKey() {
        return name;
    }

    @Override
    public String getType() {
        return "virtual node";
    }

    /**
     * Returns the job id
     * @return The job id
     */
    public String getJobID() {
        return jobID;
    }

    @Override
    public void addChild(AbstractData child) {
        if (!monitoredChildren.containsKey(child.getKey())) {
            monitoredChildren.put(child.getKey(), child);
        }
    }

    @Override
    public void removeChild(AbstractData child) {
        monitoredChildren.remove(child.getKey());
        if (monitoredChildren.isEmpty()) {
            parent.removeVirtualNode(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public WorldObject getParent() {
        return parent;
    }

    @Override
    public ProActiveConnection getConnection() {
        // A Virtual node has no JMX ProActiveConnection
        return null;
    }
}
