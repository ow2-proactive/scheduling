package org.ow2.proactive.resourcemanager.gui.tree;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeElementType;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;


public class TreeLabelProvider extends ColumnLabelProvider {

    public Image getImage(Object obj) {
        switch (((TreeLeafElement) obj).getType()) {
            case HOST:
                return ImageDescriptor.createFromFile(this.getClass(), "icons/host.gif").createImage();
            case NODE:
                switch (((Node) obj).getState()) {
                    case DOWN:
                        return ImageDescriptor.createFromFile(this.getClass(), "icons/down.gif")
                                .createImage();
                    case FREE:
                        return ImageDescriptor.createFromFile(this.getClass(), "icons/free.gif")
                                .createImage();
                    case BUSY:
                        return ImageDescriptor.createFromFile(this.getClass(), "icons/busy.gif")
                                .createImage();
                    case TO_BE_RELEASED:
                        return ImageDescriptor.createFromFile(this.getClass(), "icons/to_release.gif")
                                .createImage();
                }
                break;
            case SOURCE:
                return ImageDescriptor.createFromFile(this.getClass(), "icons/source.gif").createImage();
            case VIRTUAL_MACHINE:
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
        }
        return null;
    }

    public String getText(Object element) {
        return element.toString();
    }

    public int getToolTipDisplayDelayTime(Object object) {
        return 800;
    }

    public int getToolTipTimeDisplayed(Object object) {
        return 3000;
    }

    public Point getToolTipShift(Object object) {
        return new Point(5, 5);
    }

    public boolean useNativeToolTip(Object object) {
        return false;
    }

    public String getToolTipText(Object obj) {
        System.out.println("TooltipLabelProvider.getToolTipText()");
        if (((TreeLeafElement) obj).getType() == TreeElementType.NODE) {
            switch (((Node) obj).getState()) {
                case DOWN:
                    return "Node is down or unreachable";
                case FREE:
                    return "Node is ready to perform tasks";
                case BUSY:
                    return "Node is currently performing a task";
                case TO_BE_RELEASED:
                    return "Node is busy and will be removed at task's end";
            }
        }
        return null;
    }
}