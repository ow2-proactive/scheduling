package org.ow2.proactive.resourcemanager.gui.table;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;


public class RMTableViewer extends TableViewer {

    public RMTableViewer(Composite parent) {
        super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        this.setContentProvider(new TableContentProvider());
        ColumnViewerToolTipSupport.enableFor(this, ToolTip.NO_RECREATE);
    }

    public void init() {
        setInput(RMStore.getInstance().getModel().getRoot());
    }

    public void actualize() {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                refresh();
            }
        });
    }

    public void updateItem(String nodeSource, String host, NodeState state, String nodeUrl) {
        final NodeTableItem item = new NodeTableItem(nodeSource, host, state, nodeUrl);
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                update(item, null);
            }
        });
    }

    public void removeItem(String nodeUrl) {
        final NodeTableItem item = new NodeTableItem("", "", null, nodeUrl);
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                remove(item);
            }
        });
    }

    public void addItem(String nodeSource, String host, NodeState state, String nodeUrl) {
        final NodeTableItem item = new NodeTableItem(nodeSource, host, state, nodeUrl);
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                add(item);
            }
        });
    }

    public void tabFocused() {
        fireSelectionChanged(new SelectionChangedEvent(this, this.getSelection()));
    }
}
