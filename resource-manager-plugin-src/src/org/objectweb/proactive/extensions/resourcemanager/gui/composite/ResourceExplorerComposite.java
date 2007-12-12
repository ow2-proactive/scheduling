package org.objectweb.proactive.extensions.resourcemanager.gui.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.extensions.resourcemanager.gui.tree.ResourceExplorerTree;


/**
 * @author FRADJ Johann
 */
public class ResourceExplorerComposite extends Composite {
    private ResourceExplorerTree treeViewer = null;

    public ResourceExplorerComposite(ViewPart view, Composite parent) {
        super(parent, SWT.NONE);
        // It must be a FillLayout !
        FillLayout layout = new FillLayout(SWT.HORIZONTAL);
        layout.spacing = 2;
        layout.marginHeight = 1;
        layout.marginWidth = 1;
        this.setLayout(layout);
        treeViewer = new ResourceExplorerTree(view, this);
    }

    public ResourceExplorerTree getTreeViewer() {
        return treeViewer;
    }
}
