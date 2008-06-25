package org.objectweb.proactive.extensions.resourcemanager.gui.tree;

import java.util.ArrayList;


/**
 * @author The ProActive Team
 *
 */
public abstract class TreeParentElement extends TreeLeafElement {
    protected ArrayList<TreeLeafElement> children;

    public TreeParentElement(String name, TreeElementType type) {
        super(name, type);
        this.children = new ArrayList<TreeLeafElement>();
    }

    public void addChild(TreeLeafElement child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(TreeLeafElement child) {
        children.remove(child);
        child.setParent(null);
    }

    public TreeLeafElement[] getChildren() {
        return children.toArray(new TreeLeafElement[children.size()]);
    }

    public boolean hasChildren() {
        return children.size() > 0;
    }
}
