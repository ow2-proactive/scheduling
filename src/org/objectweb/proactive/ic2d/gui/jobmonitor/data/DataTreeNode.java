package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;


public class DataTreeNode extends DefaultMutableTreeNode
    implements JobMonitorConstants {
    private static final int STATE_NEW = 0;
    private static final int STATE_REMOVED = 1;
    private static final int STATE_KEPT = 2;
    private int state = STATE_NEW;
    private BasicMonitoredObject object;

    public DataTreeNode(DataModelTraversal traversal) {
        int key = traversal.getFollowingKey(NO_KEY);
        object = BasicMonitoredObject.create(key, null);
    }

    public DataTreeNode(DataTreeModel model, BasicMonitoredObject value,
        Set constraints) {
        rebuild(model, value, constraints);
    }

    public void setAllRemovedStates() {
        this.state = STATE_REMOVED;
        for (int i = 0, length = getChildCount(); i < length; i++) {
            DataTreeNode child = (DataTreeNode) getChildAt(i);
            child.setAllRemovedStates();
        }
    }

    public DataTreeNode getChild(BasicMonitoredObject value) {
        int length = getChildCount();

        if (length == 0) {
            return null;
        }

        DataTreeNode firstChild = (DataTreeNode) getChildAt(0);
        if (firstChild.getKey() != value.getKey()) {
            return null;
        }

        for (int i = 0; i < length; i++) {
            DataTreeNode child = (DataTreeNode) getChildAt(i);
            if (child.getObject().equals(value)) {
                return child;
            }
        }
        return null;
    }

    private boolean isEverythingRemoved() {
        for (int i = 0, length = getChildCount(); i < length; i++) {
            DataTreeNode child = (DataTreeNode) getChildAt(i);
            if (child.state != STATE_REMOVED) {
                return false;
            }
        }

        return true;
    }

    /* key : la cle de cette branche, les fils sont donc des traversal.getFollowingKey(key) */
    public void rebuild(DataTreeModel model, BasicMonitoredObject value,
        Set constraints) {
        DataModelTraversal traversal = model.getTraversal();
        int nextKey;

        if (value == null) {
            return;
        }

        if (value.isRoot()) {
            object = BasicMonitoredObject.create(traversal.getFollowingKey(
                        NO_KEY), null);
            nextKey = NO_KEY;
        } else {
            object = value;
            nextKey = object.getKey();
        }

        DataAssociation asso = model.getAssociations();
        MonitoredObjectSet children = null;

        do {
            nextKey = traversal.getFollowingKey(nextKey);
            if (nextKey == NO_KEY) {
                children = null;
                break;
            }
            if (object.isRoot()) {
                int rootKey = object.getKey();
                object.setKey(NO_KEY);
                children = asso.getValues(object, nextKey, constraints);
                object.setKey(rootKey);
            } else {
                children = asso.getValues(object, nextKey, constraints);
            }
        } while (children.isEmpty());

        if (children != null) {
            Iterator iter = children.iterator();
            while (iter.hasNext()) {
                BasicMonitoredObject childValue = (BasicMonitoredObject) iter.next();
                DataTreeNode child = getChild(childValue);

                if (!object.isRoot()) {
                    constraints.add(object);
                }

                if (child != null) {
                    child.state = STATE_KEPT;
                    child.rebuild(model, childValue, constraints);
                } else {
                    DataTreeNode newChild = new DataTreeNode(model, childValue,
                            constraints);
                    model.insertNodeInto(newChild, this, getChildCount());
                }

                if (!object.isRoot()) {
                    constraints.remove(object);
                }
            }
        }

        boolean empty = isEverythingRemoved();

        for (int i = 0; i < getChildCount(); i++) {
            DataTreeNode child = (DataTreeNode) getChildAt(i);

            if (((child.getKey() != nextKey) && empty) ||
                    (child.state == STATE_REMOVED)) {
                model.removeNodeFromParent(child);
                i--;
            }
        }

        model.nodeChanged(this);
    }

    public void keyDisplayChanged(DataTreeModel model, int key) {
        if (getKey() == key) {
            model.nodeChanged(this);
        } else {
            int length = getChildCount();
            for (int i = 0; i < length; i++) {
                DataTreeNode child = (DataTreeNode) getChildAt(i);
                child.keyDisplayChanged(model, key);
            }
        }
    }

    public int getKey() {
        return object.getKey();
    }

    public String getName() {
        return object.getPrettyName();
    }

    public String toString() {
        if (object == null) {
            return null;
        }

        if (isRoot()) {
            return NAMES[KEY2INDEX[getKey()]];
        }

        return getName();
    }

    public BasicMonitoredObject getObject() {
        return object;
    }

    public Set makeConstraints() {
        if (isRoot()) {
            return new TreeSet();
        }

        DataTreeNode parent = (DataTreeNode) getParent();
        Set constraints = parent.makeConstraints();
        constraints.add(object);

        return constraints;
    }
}
