package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;


public class DataTreeNode extends DefaultMutableTreeNode
    implements JobMonitorConstants {
    public static final int STATE_NEW = 0;
    public static final int STATE_REMOVED = 1;
    public static final int STATE_KEPT = 2;
    private int state = STATE_NEW;
    private int key = NO_KEY;
    private String name;

    public DataTreeNode(DataModelTraversal traversal) {
        key = traversal.getFollowingKey(NO_KEY);
    }

    public DataTreeNode(DataTreeModel model, int key, String name,
        Map constraints) {
        rebuild(model, key, name, constraints);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setAllStates(int state) {
        setState(state);
        for (int i = 0, length = getChildCount(); i < length; i++) {
            DataTreeNode child = (DataTreeNode) getChildAt(i);
            child.setAllStates(state);
        }
    }

    public DataTreeNode getChild(int key, String childName) {
        int length = getChildCount();

        if (length == 0) {
            return null;
        }

        DataTreeNode firstChild = (DataTreeNode) getChildAt(0);
        if (firstChild.getKey() != key) {
            return null;
        }

        for (int i = 0; i < length; i++) {
            DataTreeNode child = (DataTreeNode) getChildAt(i);
            if (child.getName().equals(childName)) {
                return child;
            }
        }
        return null;
    }

    private void handleRemovedChildren(DataTreeModel model) {
        for (int i = 0; i < getChildCount(); i++) {
            DataTreeNode child = (DataTreeNode) getChildAt(i);
            switch (child.state) {
            case STATE_REMOVED:
                model.removeNodeFromParent(child);
                i--;
                break;
            case STATE_KEPT:
                child.handleRemovedChildren(model);
                break;
            }
        }
    }

    /* key : la cle de cette branche, les fils sont donc des traversal.getFollowingKey(key) */
    public void rebuild(DataTreeModel model, int key, String name,
        Map constraints) {
        DataModelTraversal traversal = model.getTraversal();
        this.name = name;
        this.key = key;

        DataAssociation asso = model.getAssociations();
        int nextKey = key;
        Set children;

        do {
            nextKey = traversal.getFollowingKey(nextKey);
            children = asso.getValues(key, name, nextKey, constraints);
        } while ((nextKey != NO_KEY) && children.isEmpty());

        if (key == NO_KEY) {
            this.key = nextKey;
            if (this.key == NO_KEY) {
                this.key = traversal.getFollowingKey(key);
            }
        }

        if (nextKey != NO_KEY) {
            Iterator iter = children.iterator();
            while (iter.hasNext()) {
                String childName = (String) iter.next();
                DataTreeNode child = getChild(nextKey, childName);
                Integer constraintKey = (key == NO_KEY) ? null : new Integer(key);
                if (constraintKey != null) {
                    constraints.put(constraintKey, name);
                }
                if (child != null) {
                    child.state = STATE_KEPT;
                    child.rebuild(model, nextKey, childName, constraints);
                } else {
                    DataTreeNode newChild = new DataTreeNode(model, nextKey,
                            childName, constraints);
                    model.insertNodeInto(newChild, this, getChildCount());
                }
                if (constraintKey != null) {
                    constraints.remove(constraintKey);
                }
            }
        }

        handleRemovedChildren(model);
    }

    public int getKey() {

        /* For the TreeCellRenderer */
        if (name == null) {
            return NO_KEY;
        }

        return key;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        if ((name == null) && (key != NO_KEY)) {
            return NAMES[KEY2INDEX[key]];
        }

        return name;
    }

    public Map makeConstraints() {
        if (isRoot()) {
            return new HashMap(NB_KEYS);
        }

        DataTreeNode parent = (DataTreeNode) getParent();
        Map constraints = parent.makeConstraints();
        constraints.put(new Integer(key), name);

        return constraints;
    }
}
