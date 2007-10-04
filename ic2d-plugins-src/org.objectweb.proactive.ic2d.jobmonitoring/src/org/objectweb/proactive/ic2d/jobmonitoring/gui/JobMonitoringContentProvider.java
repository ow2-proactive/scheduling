package org.objectweb.proactive.ic2d.jobmonitoring.gui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;


public class JobMonitoringContentProvider implements ITreeContentProvider {
    public Object[] getChildren(Object parentElement) {
        return ((AbstractData) parentElement).getMonitoredChildrenAsList()
                .toArray();
    }

    public Object getParent(Object element) {
        return ((AbstractData) element).getParent();
    }

    public boolean hasChildren(Object element) {
        return ((AbstractData) element).getMonitoredChildrenAsList().size() > 0;
    }

    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    public void dispose() {
        // TODO Auto-generated method stub
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO Auto-generated method stub
    }
}
