package org.ow2.proactive.resourcemanager.gui.tree;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;


public class TreeLabelProvider extends LabelProvider {
    // ----------------------------------------------------------------------
    // 3 methods overridden from LabelProvider, 
    // ----------------------------------------------------------------------        

    /**
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object obj) {
        return obj.toString();
    }

    /**
     * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
     */
    @Override
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
}
