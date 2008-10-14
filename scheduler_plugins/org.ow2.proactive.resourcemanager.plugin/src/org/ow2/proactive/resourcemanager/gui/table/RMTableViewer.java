package org.ow2.proactive.resourcemanager.gui.table;

import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.data.model.Root;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;


public class RMTableViewer extends TableViewer {

    public RMTableViewer(Composite parent) {
        super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        this.setContentProvider(new TableContentProvider());
        this.setLabelProvider(new TableLabelProvider());
    }

    public void init() {
        setInput(RMStore.getInstance().getModel().getRoot());
    }

    public void actualize(TreeLeafElement element) {
        final TreeLeafElement elem = element;
        if (element instanceof Root) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    refresh(elem);
                }
            });
        }
    }

    public void tabFocused() {
        fireSelectionChanged(new SelectionChangedEvent(this, this.getSelection()));
    }
}
