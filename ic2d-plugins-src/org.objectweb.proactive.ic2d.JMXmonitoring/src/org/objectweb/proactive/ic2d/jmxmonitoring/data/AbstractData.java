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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.jmxmonitoring.Activator;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.State;


/**
 * Holder class for the data representation.
 */
public abstract class AbstractData extends Observable {
    // -------------------------------------------
    // --- Variables -----------------------------
    // -------------------------------------------

    /**
     * The monitored children
     */
    protected final Map<String, AbstractData> monitoredChildren;

    /**
     * The NOT monitored children
     */
    protected final Map<String, AbstractData> notMonitoredChildren;

    /**
     * The object name associated to this object.
     */
    private final ObjectName objectName;

    // -------------------------------------------
    // --- Constructor ---------------------------
    // -------------------------------------------

    /**
     * The standard contructor for this model. Only object name have to be provided.
     * A HashMap<String, AbstractData> is used for monitored children and not monitored ones.
     * @param objectName An instance of ObjectName for this model
     */
    public AbstractData(final ObjectName objectName) {
        this(objectName, new HashMap<String, AbstractData>(), new HashMap<String, AbstractData>());
    }

    /**
     * This constructor is provided to allow subclasses to specify their own map implementations
     * for monitored children.
     * @param objectName An instance of ObjectName for this model
     * @param monitoredChildren An instance of map for monitored children
     */
    public AbstractData(final ObjectName objectName, final Map<String, AbstractData> monitoredChildren) {
        this(objectName, monitoredChildren, new HashMap<String, AbstractData>());
    }

    /**
     * This constructor is provided to allow subclasses to specify their own map implementations
     * for monitored children and not monitored children.
     * @param objectName An instance of ObjectName for this model
     * @param monitoredChildren An instance of map for monitored children
     * @param notMonitoredChildren An instance of map for not monitored children
     */
    public AbstractData(final ObjectName objectName, final Map<String, AbstractData> monitoredChildren,
            final Map<String, AbstractData> notMonitoredChildren) {
        this.objectName = objectName;
        this.monitoredChildren = monitoredChildren;
        this.notMonitoredChildren = notMonitoredChildren;
    }

    // -------------------------------------------
    // --- Methods -------------------------------
    // -------------------------------------------

    /**
     * Returns the object name associated to this object.
     * @return
     */
    public ObjectName getObjectName() {
        return this.objectName;
    }

    /**
     * Adds a child to this object, and explore this one.
     * @param <T>
     * @param child The child to explore
     */
    public synchronized void addChild(AbstractData child) {
        if (!this.monitoredChildren.containsKey(child.getKey())) {
            this.monitoredChildren.put(child.getKey(), child);
            setChanged();
            notifyObservers(new MVCNotification(MVCNotificationTag.ADD_CHILD, child.getKey()));
            child.explore();
        }
    }

    /**
     * Deletes a child from all recorded data.
     * @param child The child to delete.
     */
    public void removeChild(AbstractData child) {
        if (child == null) {
            return;
        }
        String key = child.getKey();
        monitoredChildren.remove(key);
        notMonitoredChildren.remove(key);
        setChanged();
        notifyObservers(new MVCNotification(MVCNotificationTag.REMOVE_CHILD, key));
    }

    /**
     * Moves a child from the monitored children to the NOT monitored children.
     * @param child The child to add to the NOT monitored children.
     */
    public void removeChildFromMonitoredChildren(AbstractData child) {
        monitoredChildren.remove(child.getKey());
        notMonitoredChildren.put(child.getKey(), child);
        setChanged();
        notifyObservers(new MVCNotification(MVCNotificationTag.REMOVE_CHILD_FROM_MONITORED_CHILDREN, child
                .getKey()));
    }

    /**
     * Returns the list of monitored children
     * @return The list of monitored children
     */
    public List<AbstractData> getMonitoredChildrenAsList() {
        Collection<AbstractData> c = monitoredChildren.values();
        ArrayList<AbstractData> arr = new ArrayList<AbstractData>(c);
        return arr;
    }

    /**
     * Returns a copy of the map of the monitored children.
     * @return
     */
    public Map<String, AbstractData> getMonitoredChildrenAsMap() {
        return new HashMap<String, AbstractData>(this.monitoredChildren);
    }

    /**
     * Returns the number of monitored children.
     * @return The number of monitored children.
     */
    public int getMonitoredChildrenSize() {
        return this.monitoredChildren.size();
    }

    /**
     * Returns a child, searches in all recorded data
     * @param key
     * @return the child
     */
    public AbstractData getChild(String key) {
        AbstractData child = this.monitoredChildren.get(key);
        if (child == null) {
            return this.notMonitoredChildren.get(key);
        }
        return child;
    }

    /**
     * Returns a monitored Child
     * @param key
     * @return The monitored child.
     */
    public AbstractData getMonitoredChild(String key) {
        return this.monitoredChildren.get(key);
    }

    /**
     * Returns true if this object has associated a child with this key.
     * @param keyChild
     * @return True if this object has associated a child with this key.
     */
    public boolean containsChild(String keyChild) {
        return containsChildInMonitoredChildren(keyChild) || containsChildInNOTMonitoredChildren(keyChild);
    }

    /**
     * Returns true if this object has associated a monitored child with this key.
     * @param keyChild
     * @return True if this object has associated a monitored child with this key.
     */
    public boolean containsChildInMonitoredChildren(String keyChild) {
        return this.monitoredChildren.containsKey(keyChild);
    }

    /**
     * Returns true if this object has associated a NOT monitored child with this key.
     * @param keyChild
     * @return True if this object has associated a NOT monitored child with this key.
     */
    public boolean containsChildInNOTMonitoredChildren(String keyChild) {
        return this.notMonitoredChildren.containsKey(keyChild);
    }

    /**
     * Remove all the communications of this object.
     */
    public void resetCommunications() {
        List<AbstractData> children = getMonitoredChildrenAsList();
        for (AbstractData child : children) {
            child.resetCommunications();
        }
    }

    /**
     * Returns the object's parent
     * @return the object's parent
     */
    public abstract <T extends AbstractData> T getParent();

    /**
     * Updates the set of the children of this object so that they are in sync with the
     * children of the monitored object corresponding to this object.
     *
     */
    public abstract void explore();

    /**
     * Explores each child of monitored children.
     */
    public void exploreEachChild() {
        for (AbstractData child : getMonitoredChildrenAsList()) {
            child.explore();
        }
    }

    /**
     * Stop monitoring this object
     * @param log Indicates if you want to log a message in the console.
     */
    public void stopMonitoring(boolean log) {
        if (log) {
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Stop monitoring the " + getType() + " " + getName());
        }
        if (this.monitoredChildren != null) {
            for (final AbstractData child : this.getMonitoredChildrenAsList()) {
                child.stopMonitoring(false);
            }
        }
        getParent().removeChildFromMonitoredChildren(this);
        setChanged();
        notifyObservers(new MVCNotification(MVCNotificationTag.STATE_CHANGED, State.NOT_MONITORED));
    }

    /**
     * Returns an unique identifer,
     * it is an unique key used to add this object to
     * a set of monitored children.
     * @return The unique key.
     */
    public abstract String getKey();

    /**
     * Returns the type of the object (ex : "active object" for ActiveObject).
     * @return the type of the object.
     */
    public abstract String getType();

    /**
     * Returns the name of the object.
     * @return the name of the object.
     */
    public abstract String getName();

    /**
     * Returns the ProActive Connection
     * @return a ProActiveConnection
     */
    public ProActiveConnection getProActiveConnection() {
        return getParent().getProActiveConnection();
    }

    /**
     * Returns the MBeanServerConnection Connection.
     * This method is used to avoid third-party plugins being dependent on ProActive 
     * @return a ProActiveConnection
     */
    public MBeanServerConnection getMBeanServerConnection() {
        return getProActiveConnection();
    }

    /**
     * Invokes an operation on the MBean associated to the ProActive object.
     *
     * @param operationName The name of the operation to be invoked.
     * @param params An array containing the parameters to be set when
     * the operation is invoked
     * @param signature An array containing the signature of the
     * operation.
     *
     * @return The object returned by the operation, which represents
     * the result of invoking the operation on the ProActive object.
     *
     * @throws IOException
     * @throws ReflectionException Wraps a
     * <CODE>java.lang.Exception</CODE> thrown while trying to invoke
     * the method.
     * @throws MBeanException Wraps an exception thrown by the
     * MBean's invoked method.
     * @throws InstanceNotFoundException The MBean representing the ProActive object is not
     * registered in the remote MBean server.
     */
    public Object invoke(String operationName, Object[] params, String[] signature)
            throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        return getProActiveConnection().invoke(getObjectName(), operationName, params, signature);
    }

    /**
     * Invokes an operation on the MBean associated to the ProActive object.
     *
     * @param operationName The name of the operation to be invoked.
     * @param params An array containing the parameters to be set when
     * the operation is invoked
     * @param signature An array containing the signature of the
     * operation.
     *
     * @return The object returned by the operation, which represents
     * the result of invoking the operation on the ProActive object.
     */
    public Object invokeAsynchronous(String operationName, Object[] params, String[] signature) {
        return getProActiveConnection().invokeAsynchronous(getObjectName(), operationName, params, signature);
    }

    public Object getAttribute(String attribute) throws AttributeNotFoundException,
            InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        return getProActiveConnection().getAttribute(getObjectName(), attribute);
    }

    public Object getAttributeAsynchronous(String attribute) {
        return getProActiveConnection().getAttributeAsynchronous(getObjectName(), attribute);
    }

    protected String getHostUrlServer() {
        return getParent().getHostUrlServer();
    }

    /**
     * Returns the JMX Server Name
     * @return the JMX Server Name
     */
    protected String getServerName() {
        return getParent().getServerName();
    }

    /**
     * Returns the current World
     * @return The World, or null if the parent of this object is null.
     */
    public WorldObject getWorldObject() {
        return getParent().getWorldObject();
    }

    /**
     * Destroy this object.
     */
    public void destroy() {
        getParent().removeChild(this);
    }

    /**
     * Returns the host rank.
     * @return the host rank.
     */
    public int getHostRank() {
        return getParent().getHostRank();
    }

    /**
     * Return the max depth.
     * @return the max depth.
     */
    public int getDepth() {
        return getParent().getDepth();
    }
}
