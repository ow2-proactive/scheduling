package org.ow2.proactive.resourcemanager.gui.tree;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;


/**
 * @author The ProActive Team
 */
public class RMTreeViewer extends TreeViewer {

    public RMTreeViewer(ViewPart view, Composite parent) {
        super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        this.setContentProvider(new TreeContentProvider());
        ColumnViewerToolTipSupport.enableFor(this, ToolTip.NO_RECREATE);
    }

    public void init() {
        setInput(RMStore.getInstance().getModel().getRoot());
    }

    public void actualize(TreeLeafElement element) {
        final TreeLeafElement elem = element;
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                refresh(elem, true);
            }
        });
    }

    public void treeFocused() {
        fireSelectionChanged(new SelectionChangedEvent(this, this.getSelection()));
    }

}