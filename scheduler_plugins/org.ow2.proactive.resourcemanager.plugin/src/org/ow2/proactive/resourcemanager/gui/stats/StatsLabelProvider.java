package org.ow2.proactive.resourcemanager.gui.stats;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;


public class StatsLabelProvider implements ITableLabelProvider {

    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    public String getColumnText(Object element, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return ((StatsItem) element).getAggregate();
            case 1:
                return ((StatsItem) element).getValue();
        }
        return null;
    }

    public void addListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub
    }

    public void dispose() {
        // TODO Auto-generated method stub
    }

    public boolean isLabelProperty(Object element, String property) {
        // TODO Auto-generated method stub
        return false;
    }

    public void removeListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub
    }

}
