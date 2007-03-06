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

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.ic2d.event.NodeObjectListener;


/**
 * Holder class for the host data representation
 */
public class NodeObject extends AbstractDataObject {
    protected Node node;
    protected java.util.HashMap activeObjects;
    protected NodeObjectListener listener;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public NodeObject(VMObject parent, Node node) {
        super(parent);
        this.node = node;
        controller.log("Node Object " + node.getNodeInformation().getURL() +
            " created.");
    }

    //
    // -- PUBLIC METHOD -----------------------------------------------
    //
    public String toString() {
        return "Url= " + node.getNodeInformation().getURL() + "\n" +
        super.toString();
    }

    public Object createNewRemoteObject(String classname) {
        try {
            Object o = ProActive.newActive(classname, null, node);
            controller.log("Active Object " + classname +
                " succesfully created.");
            return o;
        } catch (ActiveObjectCreationException e) {
            controller.log("Cannot create active object " + classname, e);
            return null;
        } catch (NodeException e) {
            controller.log("Problem with this node " + getURL());
            return null;
        }
    }

    public boolean isInsideSameVM(NodeObject o) {
        return getTypedParent().getID().equals(o.getTypedParent().getID());
    }

    public boolean isInsideSameNode(NodeObject o) {
        return getTypedParent().getID().equals(o.getTypedParent().getID()) &&
        getURL().equals(o.getURL());
    }

    public ActiveObject findActiveObjectById(UniqueID id) {
        return getActiveObject(id);
    }

    //
    // Event Listener
    //
    public void registerListener(NodeObjectListener listener) {
        this.messageMonitoringListener = listener;
        this.listener = listener;
        notifyListenerOfExistingChilds();
    }

    //
    // Accessor methods
    //
    public String getURL() {
        //System.out.println(node.getNodeInformation().getProtocol()+":"+node.getNodeInformation().getURL());
        return node.getNodeInformation().getURL();
    }

    public String getName() {
        return node.getNodeInformation().getName();
    }

    public String getProtocol() {
        return node.getNodeInformation().getProtocol();
    }

    public Node getNode() {
        return node;
    }

    //
    // ActiveObject related methods
    //
    public ActiveObject addActiveObject(String classname, UniqueID bodyID,
        boolean isActive) {
        ActiveObject activeObject = (ActiveObject) getChild(bodyID);
        if (activeObject == null) {
            activeObject = new ActiveObject(this, bodyID, classname, isActive);
            putChild(bodyID, activeObject);
            getTypedParent().registerActiveObject(bodyID, this);
            ((IC2DObject) getTopLevelParent()).activeObjectAdded(activeObject);
            if (listener != null) {
                listener.activeObjectAdded(activeObject);
            }
        }
        return activeObject;
    }

    public void removeActiveObject(UniqueID bodyID) {
        ActiveObject activeObject = (ActiveObject) removeChild(bodyID);
        if (activeObject == null) {
            controller.log("Cannot find object id=" + bodyID);
        } else {
            getTypedParent().unregisterActiveObject(bodyID);
            ((IC2DObject) getTopLevelParent()).activeObjectRemoved(activeObject);
            if (listener != null) {
                listener.activeObjectRemoved(activeObject);
            }
        }
    }

    public ActiveObject getActiveObject(UniqueID id) {
        return (ActiveObject) getChild(id);
    }

    public void destroyObject() {
        getTypedParent().removeNodeObject(getName());
    }

    //
    // -- PROTECTED METHOD -----------------------------------------------
    //
    protected synchronized boolean destroy() {
        if (super.destroy()) {
            listener = null;
            return true;
        } else {
            return false;
        }
    }

    protected VMObject getTypedParent() {
        return (VMObject) parent;
    }

    //
    // -- PRIVATE METHOD -----------------------------------------------
    //
    private synchronized void notifyListenerOfExistingChilds() {
        if (getChildObjectsCount() == 0) {
            return;
        }
        java.util.Iterator iterator = childsIterator();
        while (iterator.hasNext()) {
            ActiveObject activeObject = (ActiveObject) iterator.next();
            listener.activeObjectAdded(activeObject);
        }
    }
}
