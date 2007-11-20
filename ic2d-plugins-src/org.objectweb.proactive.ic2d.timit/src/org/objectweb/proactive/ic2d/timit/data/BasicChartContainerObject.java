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
package org.objectweb.proactive.ic2d.timit.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.benchmarks.timit.util.basic.BasicTimer;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.HostObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.timit.editparts.BasicChartContainerEditPart;


/**
 * A simple object that handles all charts in it
 *
 * @author vbodnart
 */
public class BasicChartContainerObject {

    /**
     * A list of children models
     */
    protected List<BasicChartObject> childrenList;

    /**
     * A map view on children models
     */
    protected Map<UniqueID, BasicChartObject> childrenMap;

    /**
     * The edit part of this model
     */
    protected BasicChartContainerEditPart ep;

    /**
     * Constructor of the chart container object.
     */
    public BasicChartContainerObject() {
        this.childrenList = new ArrayList<BasicChartObject>();
        this.childrenMap = new HashMap<UniqueID, BasicChartObject>();
    }

    /**
     * This method allows to automatically find potential active objects that are timer aware
     * and create a chart from the gathered data.
     * @param object an AbstractObject instance that can be ActiveObject, NodeObject, RuntimeObject, HostObject or WorldObject
     */
    public final void recognizeAndCreateChart(final AbstractData object) {
        if (object == null) {
            return;
        }

        if (object instanceof ActiveObject) {
            createFromAOObject((ActiveObject) object);
        } else if (object instanceof NodeObject) {
            createFromNodeObject((NodeObject) object);
        } else if (object instanceof RuntimeObject) {
            createFromVMObject((RuntimeObject) object);
        } else if (object instanceof HostObject) {
            createFromHostObject((HostObject) object);
        } else if (object instanceof WorldObject) {
            createFromWorldObject((WorldObject) object);
        } else {
            // Unknown object
        }
    }

    protected final void createFromWorldObject(final WorldObject object) {
        // Get All nodes from the Virtual Machine object and create charts from
        // them
        List<AbstractData> children = object.getMonitoredChildrenAsList();
        for (AbstractData o : children) {
            createFromHostObject((HostObject) o);
        }
    }

    protected final void createFromHostObject(final HostObject object) {
        // Get All nodes from the Virtual Machine object and create charts from
        // them
        List<AbstractData> children = object.getMonitoredChildrenAsList();
        for (AbstractData o : children) {
            createFromVMObject((RuntimeObject) o);
        }
    }

    protected final void createFromVMObject(final RuntimeObject object) {
        // Get All nodes from the Virtual Machine object and create charts from
        // them
        List<AbstractData> children = object.getMonitoredChildrenAsList();
        for (AbstractData o : children) {
            createFromNodeObject((NodeObject) o);
        }
    }

    protected final void createFromNodeObject(final NodeObject object) {
        // Get All active objects from the node and create charts from them
        List<AbstractData> children = object.getMonitoredChildrenAsList();
        for (AbstractData o : children) {
            createFromAOObject((ActiveObject) o);
        }
    }

    protected final void createFromAOObject(final ActiveObject aoObject) {
        // First look if this active object is already registred
        if (this.childrenMap.containsKey(aoObject.getUniqueID())) {
            BasicChartObject basicChartObject = this.childrenMap.get(aoObject.getUniqueID());
            basicChartObject.performSnapshot();
            return;
        }

        // Before ChartObject instanciation try to take a snapshot of timers
        BasicTimer[] timersCollection = BasicChartObject.performSnapshotInternal(aoObject,
                BasicChartObject.BASIC_LEVEL);
        if (timersCollection != null) {
            new BasicChartObject(this, timersCollection, aoObject);
        }
    }

    /**
     * Since each children can be identified, this method returns a specific child
     * @param id The id of the child
     * @return The identified child
     */
    public BasicChartObject getChartObjectById(UniqueID id) {
        return this.childrenMap.get(id);
    }

    /**
     * Returns the list of children of this container
     * @return A list of children
     */
    public List<BasicChartObject> getChildrenList() {
        return this.childrenList;
    }

    /**
     * Adds a child to this container
     * @param child The child to add
     */
    public void addChild(BasicChartObject child) {
        this.childrenList.add(child);
        if (!BasicChartObject.DEBUG) {
            this.childrenMap.put(child.aoObject.getUniqueID(), child);
        }
        this.update(false);
    }

    /**
     * Removes a child from this container.
     * @param child The child object to remove
     */
    public void removeChild(BasicChartObject child) {
        this.childrenList.remove(child);
        this.childrenMap.remove(child.aoObject.getUniqueID());
        this.update(false);
    }

    /**
     * A setter for the edit part of this model
     * @param ep A new edit part
     */
    public void setEp(BasicChartContainerEditPart ep) {
        this.ep = ep;
    }

    /**
     * This method is used to update the edit part of this model.
     * @param forceRefresh If true each children model will be refreshed
     */
    public void update(boolean forceRefresh) {
        if (forceRefresh) {
            for (BasicChartObject o : this.childrenList) {
                o.ep.asyncRefresh();
            }
            return;
        }
        this.ep.asyncRefresh();
    }
}
