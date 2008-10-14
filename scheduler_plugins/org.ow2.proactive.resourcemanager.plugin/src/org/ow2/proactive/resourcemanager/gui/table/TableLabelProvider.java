package org.ow2.proactive.resourcemanager.gui.table;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;


public class TableLabelProvider implements ITableLabelProvider {

    public static TableLabelProvider instance = null;

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (element instanceof NodeTableItem) {
            switch (columnIndex) {
                case 2:
                    switch (((NodeTableItem) element).getState()) {
                        case DOWN:
                            return ImageDescriptor.createFromFile(RMTableViewer.class, "icons/down.gif")
                                    .createImage();
                        case FREE:
                            return ImageDescriptor.createFromFile(RMTableViewer.class, "icons/free.gif")
                                    .createImage();
                        case BUSY:
                            return ImageDescriptor.createFromFile(RMTableViewer.class, "icons/busy.gif")
                                    .createImage();
                        case TO_BE_RELEASED:
                            return ImageDescriptor
                                    .createFromFile(RMTableViewer.class, "icons/to_release.gif")
                                    .createImage();
                    }
            }
            return null;
        }
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof NodeTableItem) {
            NodeTableItem nodeItem = (NodeTableItem) element;
            String str = null;
            switch (columnIndex) {
                case 0:
                    str = nodeItem.getNodeSource();
                    break;
                case 1:
                    str = nodeItem.getHost();
                    break;
                case 3:
                    str = nodeItem.getNodeUrl();
                    break;
            }
            return str;
        }
        return null;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub
    }
}
