/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.data;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.ic2d.event.MessageMonitoringListener;


/**
 * Holder class for the host data representation
 */
public abstract class AbstractDataObject implements MessageMonitoringController {
    protected DataObjectController controller;
    protected AbstractDataObject parent;
    private java.util.HashMap childs;
    protected String abstractDataObjectName;
    protected boolean isDestroyed;
    protected boolean monitoringRequestReceiver;
    protected boolean monitoringRequestSender;
    protected boolean monitoringReplyReceiver;
    protected boolean monitoringReplySender;
    protected boolean viewingInEventList;
    protected MessageMonitoringListener messageMonitoringListener;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    protected AbstractDataObject(AbstractDataObject parent,
        String abstractDataObjectName) {
        this();
        this.parent = parent;
        this.abstractDataObjectName = abstractDataObjectName;
        if (abstractDataObjectName == null) {
            this.abstractDataObjectName = this.getClass().getName();
        } else {
            this.abstractDataObjectName = abstractDataObjectName;
        }

        this.controller = parent.getController();
        initializeMonitoring(parent);
        childs = new java.util.HashMap();
    }

    protected AbstractDataObject(AbstractDataObject parent) {
        this(parent, null);
    }

    protected AbstractDataObject() {
        initializeMonitoring(null);
        childs = new java.util.HashMap();
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //
    public String toString() {
    	System.out.println("xcgfxg");
        return "DataObject " + abstractDataObjectName + "\n" +
        childs.toString();
    }

    public DataObjectController getController() {
        return controller;
    }

    public AbstractDataObject getParent() {
        return parent;
    }

    /**
     * return the top level parent
     */
    public AbstractDataObject getTopLevelParent() {
        if (parent == null) {
            return this;
        } else {
            return parent.getTopLevelParent();
        }
    }

    public java.util.Iterator childsIterator() {
        return childs.values().iterator();
    }

    public int getChildObjectsCount() {
        return childs.size();
    }

    public synchronized ActiveObject findActiveObjectById(UniqueID id) {
        java.util.Iterator iterator = childsIterator();
        while (iterator.hasNext()) {
            AbstractDataObject o = (AbstractDataObject) iterator.next();
            ActiveObject activeObject = o.findActiveObjectById(id);
            if (activeObject != null) {
                return activeObject;
            }
        }
        return null;
    }

    public abstract void destroyObject();

    //
    // -- implements MessageMonitoringController -----------------------------------------------
    //
    public void viewInEventList(boolean shouldView) {
        if (isDestroyed) {
            return;
        }
        if (viewingInEventList != shouldView) {
            viewingInEventList = shouldView;
            if (messageMonitoringListener != null) {
                messageMonitoringListener.viewingInEventListChanged(shouldView);
            }
        }
        viewInEventListCollection(shouldView, childsIterator());
    }

    public void monitorRequestReceiver(boolean shouldMonitor) {
        if (isDestroyed) {
            return;
        }
        if (monitoringRequestReceiver != shouldMonitor) {
            monitoringRequestReceiver = shouldMonitor;
            if (messageMonitoringListener != null) {
                messageMonitoringListener.monitoringRequestReceiverChanged(shouldMonitor);
            }
        }
        monitorRequestReceiverCollection(shouldMonitor, childsIterator());
    }

    public void monitorRequestSender(boolean shouldMonitor) {
        if (isDestroyed) {
            return;
        }
        if (monitoringRequestSender != shouldMonitor) {
            monitoringRequestSender = shouldMonitor;
            if (messageMonitoringListener != null) {
                messageMonitoringListener.monitoringRequestSenderChanged(shouldMonitor);
            }
        }
        monitorRequestSenderCollection(shouldMonitor, childsIterator());
    }

    public void monitorReplyReceiver(boolean shouldMonitor) {
        if (isDestroyed) {
            return;
        }
        if (monitoringReplyReceiver != shouldMonitor) {
            monitoringReplyReceiver = shouldMonitor;
            if (messageMonitoringListener != null) {
                messageMonitoringListener.monitoringReplyReceiverChanged(shouldMonitor);
            }
        }
        monitorReplyReceiverCollection(shouldMonitor, childsIterator());
    }

    public void monitorReplySender(boolean shouldMonitor) {
        if (isDestroyed) {
            return;
        }
        if (monitoringReplySender != shouldMonitor) {
            monitoringReplySender = shouldMonitor;
            if (messageMonitoringListener != null) {
                messageMonitoringListener.monitoringReplySenderChanged(shouldMonitor);
            }
        }
        monitorReplySenderCollection(shouldMonitor, childsIterator());
    }

    public void monitorAll(boolean shouldMonitor) {
        monitorRequestReceiver(shouldMonitor);
        monitorRequestSender(shouldMonitor);
        monitorReplyReceiver(shouldMonitor);
        monitorReplySender(shouldMonitor);
    }

    public boolean isMonitoring() {
        return monitoringRequestReceiver || monitoringRequestSender ||
        monitoringReplyReceiver || monitoringReplySender;
    }

    public boolean isMonitoringRequestReceiver() {
        return monitoringRequestReceiver;
    }

    public boolean isMonitoringRequestSender() {
        return monitoringRequestSender;
    }

    public boolean isMonitoringReplyReceiver() {
        return monitoringReplyReceiver;
    }

    public boolean isMonitoringReplySender() {
        return monitoringReplySender;
    }

    public boolean isViewedInEventList() {
        return viewingInEventList;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * destroy this object
     */
    protected synchronized boolean destroy() {
        if (isDestroyed) {
            return false;
        }

        //System.out.println("AbstractDataObject destroy "+abstractDataObjectName);
        isDestroyed = true;
        destroyCollection(childsIterator());
        childs.clear();
        parent = null;
        controller = null;
        return true;
    }

    /**
     * clear Child
     */
    protected synchronized void clearChilds() {
        childs.clear();
    }

    /**
     * add Child
     */
    public synchronized void putChild(Object key, AbstractDataObject child) {
        if (isDestroyed) {
            return;
        }

        //System.out.println("AbstractDataObject putChild child="+child.abstractDataObjectName);
        childs.put(key, child);
    }

    /**
     * remove Child
     */
    protected synchronized AbstractDataObject removeChild(Object key) {
        AbstractDataObject o;
        if (isDestroyed) {
            // we are in the Iterator to destroy all childs object :
            // we don't want to remove from the collection not to
            // have an exception from the iterator
            o = (AbstractDataObject) childs.get(key);
        } else {
            // we are asked to remove the child from elsewhere
            o = (AbstractDataObject) childs.remove(key);
        }
        if (o == null) {
            return null;
        }

        //System.out.println("AbstractDataObject removeChild child="+o.abstractDataObjectName);
        o.destroy();
        return o;
    }

    /**
     * remove Child
     */
    protected synchronized AbstractDataObject getChild(Object key) {
        return (AbstractDataObject) childs.get(key);
    }

    /**
     * destroys all object known by this object
     */
    protected synchronized void destroyCollection(java.util.Iterator iterator) {
        //System.out.println("AbstractDataObject destroyCollection "+abstractDataObjectName+" childs#"+childs.size());
        while (iterator.hasNext()) {
            AbstractDataObject o = (AbstractDataObject) iterator.next();
            o.destroyObject();
        }
    }

    /**
     * notifies all known objects of the monitoring request
     * @param shouldView whether the monitoring is activated or not
     * @param iterator an iterator on the collection of object to notify
     */
    protected synchronized void viewInEventListCollection(boolean shouldView,
        java.util.Iterator iterator) {
        while (iterator.hasNext() && !isDestroyed) {
            MessageMonitoringController o = (MessageMonitoringController) iterator.next();
            o.viewInEventList(shouldView);
        }
    }

    /**
     * notifies all known objects of the monitoring request
     * @param shouldMonitor whether the monitoring is activated or not
     * @param iterator an iterator on the collection of object to notify
     */
    protected synchronized void monitorRequestReceiverCollection(
        boolean shouldMonitor, java.util.Iterator iterator) {
        while (iterator.hasNext() && !isDestroyed) {
            MessageMonitoringController o = (MessageMonitoringController) iterator.next();
            o.monitorRequestReceiver(shouldMonitor);
        }
    }

    /**
     * notifies all known objects of the monitoring request
     * @param shouldMonitor whether the monitoring is activated or not
     * @param iterator an iterator on the collection of object to notify
     */
    protected synchronized void monitorRequestSenderCollection(
        boolean shouldMonitor, java.util.Iterator iterator) {
        while (iterator.hasNext() && !isDestroyed) {
            MessageMonitoringController o = (MessageMonitoringController) iterator.next();
            o.monitorRequestSender(shouldMonitor);
        }
    }

    /**
     * notifies all known objects of the monitoring request
     * @param shouldMonitor whether the monitoring is activated or not
     * @param iterator an iterator on the collection of object to notify
     */
    protected synchronized void monitorReplyReceiverCollection(
        boolean shouldMonitor, java.util.Iterator iterator) {
        while (iterator.hasNext() && !isDestroyed) {
            MessageMonitoringController o = (MessageMonitoringController) iterator.next();
            o.monitorReplyReceiver(shouldMonitor);
        }
    }

    /**
     * notifies all known objects of the monitoring request
     * @param shouldMonitor whether the monitoring is activated or not
     * @param iterator an iterator on the collection of object to notify
     */
    protected synchronized void monitorReplySenderCollection(
        boolean shouldMonitor, java.util.Iterator iterator) {
        while (iterator.hasNext() && !isDestroyed) {
            MessageMonitoringController o = (MessageMonitoringController) iterator.next();
            o.monitorReplySender(shouldMonitor);
        }
    }

    /**
     * notifies all known objects of the monitoring request
     * @param shouldMonitor whether the monitoring is activated or not
     * @param iterator an iterator on the collection of object to notify
     */
    protected synchronized void monitorAllCollection(boolean shouldMonitor,
        java.util.Iterator iterator) {
        while (iterator.hasNext() && !isDestroyed) {
            MessageMonitoringController o = (MessageMonitoringController) iterator.next();
            o.monitorAll(shouldMonitor);
        }
    }

    // notification methods to the parent
    protected void monitoringMessageEventChanged(ActiveObject object,
        boolean value) {
        if ((parent != null) && !isDestroyed) {
            parent.monitoringMessageEventChanged(object, value);
        }
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void initializeMonitoring(AbstractDataObject parent) {
        if (parent != null) {
            monitoringRequestReceiver = parent.monitoringRequestReceiver;
            monitoringRequestSender = parent.monitoringRequestSender;
            monitoringReplyReceiver = parent.monitoringReplyReceiver;
            monitoringReplySender = parent.monitoringReplySender;
            viewingInEventList = parent.viewingInEventList;
        } else {
            // monitor everything by default
            monitoringRequestReceiver = true;
            monitoringRequestSender = true;
            monitoringReplyReceiver = true;
            monitoringReplySender = true;
            // not adding in event list by default
            viewingInEventList = false;
        }
    }
}
