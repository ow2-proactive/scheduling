package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import javax.swing.tree.DefaultTreeModel;

import org.objectweb.proactive.ic2d.gui.jobmonitor.*;


/*
 * The TreeModel is built on top of the DataAssociation and feeded with a JTree
 * to java which will display it.
 * The 4 instances of the model (4 views) are always the same, they are just rebuilt
 * when the underlying data changes.
 * The model contains a DataTreeNode which is the root and itself contains DataTreeNode which
 * are its children ...
 */
public class DataTreeModel extends DefaultTreeModel
    implements JobMonitorConstants {
    private DataAssociation asso;
    private DataModelTraversal traversal;

    public DataTreeModel(DataAssociation _asso, DataModelTraversal _traversal) {
        super(new DataTreeNode(_traversal));
        asso = _asso;
        traversal = _traversal;
    }

    public DataTreeNode root() {
        return (DataTreeNode) getRoot();
    }

    public void rebuild() {
        rebuild(root());
    }

    public void rebuild(DataTreeNode node) {
        node.setAllRemovedStates();
        node.rebuild(this, node.getObject(), node.makeConstraints());
    }

    public DataModelTraversal getTraversal() {
        return traversal;
    }

    public DataAssociation getAssociations() {
        return asso;
    }

    public void setHighlighted(int key, boolean highlight) {
        traversal.setHighlighted(key, highlight);
        root().keyDisplayChanged(this, key);
    }

    public boolean isHighlighted(int key) {
        return traversal.isHighlighted(key);
    }

    public void setHidden(int key, boolean hide) {
        traversal.setHidden(key, hide);
        rebuild();
    }

    public boolean isHidden(int key) {
        return traversal.isHidden(key);
    }

    public void exchange(int fromKey, int toKey) {
        traversal.exchange(fromKey, toKey);
        rebuild();
    }

    public int getNbKey() {
        return traversal.getNbKey();
    }

    public Branch getBranch(int index) {
        return traversal.getBranch(index);
    }

    public int indexOfKey(int key) {
        return traversal.indexOf(key);
    }
}
