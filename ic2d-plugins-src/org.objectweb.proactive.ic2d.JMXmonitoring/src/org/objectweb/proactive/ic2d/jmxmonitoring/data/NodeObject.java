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

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean;
import org.objectweb.proactive.core.jmx.mbean.NodeWrapperMBean;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.server.ProActiveServerImpl;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.util.URIBuilder;


public class NodeObject extends AbstractData {
    private RuntimeObject parent;
    private VNObject vnParent;
    private String url;

    //Warning: Don't use this variavle directly, use getProxyNodeMBean().
    private NodeWrapperMBean proxyNodeMBean;

    public NodeObject(RuntimeObject parent, String url, ObjectName objectName) {
        super(objectName);
        this.parent = parent;

        this.url = FactoryName.getCompleteUrl(url);

        Comparator<String> comparator = new ActiveObject.ActiveObjectComparator();
        this.monitoredChildren = new TreeMap<String, AbstractData>(comparator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public RuntimeObject getParent() {
        return this.parent;
    }

    /**
     * Sets the virtual node.
     * @param vn the virtual node.
     */
    public void setVirtualNode(VNObject vn) {
        this.vnParent = vn;
    }

    /**
     * Returns the virtual node.
     * @return the virtual node.
     */
    public VNObject getVirtualNode() {
        return this.vnParent;
    }

    private NodeWrapperMBean getProxyNodeMBean() {
        if (proxyNodeMBean == null) {        	
            proxyNodeMBean = MBeanServerInvocationHandler.newProxyInstance(getConnection(),
                    getObjectName(), NodeWrapperMBean.class, false);
        }
        return proxyNodeMBean;
    }

    public void setProxyNodeMBean(NodeWrapperMBean proxyNodeMBean) {
		this.proxyNodeMBean = proxyNodeMBean;
	}

	@Override
    public void destroy() {
        this.vnParent.removeChild(this);
        super.destroy();
    }

    @Override
    public void explore() {
        findActiveObjects();
    }

    @Override
    public String getKey() {
        return this.url;
    }

    @Override
    public String getType() {
        return "node object";
    }

    /**
     * Returns the url of this object.
     * @return An url.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Finds all active objects of this node.
     */
    @SuppressWarnings("unchecked")
    private void findActiveObjects() {
        Map<String, AbstractData> childrenToRemoved = this.getMonitoredChildrenAsMap();

        List<ObjectName> activeObjectNames = getProxyNodeMBean()
                                                 .getActiveObjects();
        for (ObjectName oname : activeObjectNames) {
            BodyWrapperMBean proxyBodyMBean = MBeanServerInvocationHandler.newProxyInstance(getConnection(),
                    oname, BodyWrapperMBean.class, false);
            UniqueID id = proxyBodyMBean.getID();
            String activeObjectName = proxyBodyMBean.getName();

            // If this child is a NOT monitored child.
            if (containsChildInNOTMonitoredChildren(id.toString())) {
                continue;
            }
            ActiveObject child = (ActiveObject) this.getMonitoredChild(id.toString());

            // If this child is not yet monitored.
            if (child == null) {
                child = new ActiveObject(this, id, activeObjectName, oname);
                addChild(child);
            } else {
                child.explore();
            }
            // Removes from the model the not monitored or termined aos.
            childrenToRemoved.remove(child.getKey());
        }

        // Some child have to be removed
        for (Iterator<AbstractData> iter = childrenToRemoved.values().iterator();
                iter.hasNext();) {
            ActiveObject child = (ActiveObject) iter.next();
            child.stopMonitoring(true); //unsubscribes listener for this child object 
                                        //and call destroy() on the child object
        }
    }

    @Override
    public String getName() {
        return URIBuilder.getNameFromURI(getUrl());
    }

    @Override
    public String toString() {
        return "Node: " + getUrl();
    }

    public void addChild(ActiveObject child) {
        super.addChild(child);
        String name = child.getClassName();
        if ((!name.equals(ProActiveConnection.class.getName()) &&
                (!name.equals(ProActiveServerImpl.class.getName())))) {
            ObjectName oname = child.getObjectName();

            JMXNotificationManager.getInstance()
                                  .subscribe(oname, child.getListener(),
                getParent().getUrl());
        }
    }

    /**
     * Returns the virtual node name.
     * @return the virtual node name.
     */
    public String getVirtualNodeName() {
    	return this.vnParent.getName();
        //return getProxyNodeMBean().getVirtualNodeName();
    }

    /**
     * Returns the Job Id.
     * @return the Job Id.
     */
    public String getJobId() {
        return this.vnParent.getJobID();
    }

    /**
     * Used to highlight this node, in a virtual node.
     * @param highlighted true, or false
     */
    public void setHighlight(boolean highlighted) {
        this.setChanged();
        if (highlighted) {
            this.notifyObservers(State.HIGHLIGHTED);
        } else {
            this.notifyObservers(State.NOT_HIGHLIGHTED);
        }
    }
}
