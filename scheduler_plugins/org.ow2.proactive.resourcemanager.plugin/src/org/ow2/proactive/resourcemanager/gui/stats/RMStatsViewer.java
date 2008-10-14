package org.ow2.proactive.resourcemanager.gui.stats;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;


public class RMStatsViewer extends TableViewer {

    TableItem freeNodesItem;
    TableItem busyNodesItem;
    TableItem downNodesItem;

    public RMStatsViewer(Composite parent) {
        super(parent);
        this.setContentProvider(new StatsContentProvider());
        this.setLabelProvider(new StatsLabelProvider());
    }

    public void init() {
        setInput(RMStore.getInstance().getModel());
    }

    public void actualize() {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                refresh();
            }
        });
    }
}
