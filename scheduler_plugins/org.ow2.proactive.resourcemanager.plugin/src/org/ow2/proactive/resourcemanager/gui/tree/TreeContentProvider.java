package org.ow2.proactive.resourcemanager.gui.tree;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeParentElement;


public class TreeContentProvider implements IStructuredContentProvider, ITreeContentProvider {

    // ----------------------------------------------------------------------
    // 3 methods implemented from ITreeContentProvider, 
    // ----------------------------------------------------------------------    

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object child) {
        if (child instanceof TreeLeafElement) {
            return ((TreeLeafElement) child).getParent();
        }
        return null;
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parent) {
        if (parent instanceof TreeParentElement) {
            return ((TreeParentElement) parent).getChildren();
        }
        return new Object[0];
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object parent) {
        if (parent instanceof TreeParentElement)
            return ((TreeParentElement) parent).hasChildren();
        return false;
    }

    // ----------------------------------------------------------------------
    // 3 methods implemented from IStructuredContentProvider, 
    // ----------------------------------------------------------------------    

    /**
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object parent) {
        return getChildren(parent);
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
    }
}
