package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;


public class DataTreeNode extends DefaultMutableTreeNode
    implements JobMonitorConstants {
    public static final int STATE_NEW = 0;
    public static final int STATE_REMOVED = 1;
    public static final int STATE_KEPT = 2;
    private int state = STATE_NEW;
    private BasicMonitoredObject object;

    public DataTreeNode(DataModelTraversal traversal) {
    	int key = traversal.getFollowingKey(NO_KEY);
    	object = BasicMonitoredObject.create(key, null);
    }

    public DataTreeNode(DataTreeModel model, BasicMonitoredObject value, Set constraints) {
        rebuild(model, value, constraints);
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
    public void rebuild(DataTreeModel model, BasicMonitoredObject value, Set constraints) {
        DataModelTraversal traversal = model.getTraversal();
        this.object = value;
        DataAssociation asso = model.getAssociations();
        int nextKey = value.getKey();
        Set children = null;

        do {
            nextKey = traversal.getFollowingKey(nextKey);
            if (nextKey == NO_KEY)
            	break;
            children = asso.getValues(value, nextKey, constraints);
        } while (children.isEmpty());

        if (nextKey != NO_KEY) {
            Iterator iter = children.iterator();
            while (iter.hasNext()) {
            	BasicMonitoredObject childValue = (BasicMonitoredObject) iter.next();
                DataTreeNode child = getChild(childValue);

                if (value.getFullName() != null)
                	constraints.add(value);
                
                if (child != null) {
                    child.state = STATE_KEPT;
                    child.rebuild(model, childValue, constraints);
                } else {
                    DataTreeNode newChild = new DataTreeNode(model, childValue, constraints);
                    model.insertNodeInto(newChild, this, getChildCount());
                }

                if (value.getFullName() != null)
                	constraints.remove(value);
            }
        }

        handleRemovedChildren(model);
        model.nodeChanged(this);
    }

    public void keyDisplayChanged(DataTreeModel model, int key) {
    	if (getKey() == key)
    		model.nodeChanged(this);
    	else {
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
        if ((getName() == null) && (getKey() != NO_KEY)) {
            return NAMES[KEY2INDEX[getKey()]];
        }

        return getName();
    }

    public BasicMonitoredObject getObject() {
    	return object;
    }
    
    public Set makeConstraints() {
        if (isRoot())
            return new TreeSet();

        DataTreeNode parent = (DataTreeNode) getParent();
        Set constraints = parent.makeConstraints();
        constraints.add(object);

        return constraints;
    }
}
