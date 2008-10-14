package org.ow2.proactive.resourcemanager.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.stats.RMStatsViewer;


/**
 * @author The ProActive Team
 */
public class StatisticsView extends ViewPart {

    /**
     * view part id
     */
    public static final String ID = "org.ow2.proactive.resourcemanager.gui.views.StatisticsView";
    private static RMStatsViewer statsViewer = null;

    public static void init() {
        statsViewer.init();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        statsViewer = new RMStatsViewer(parent);
        Table table = statsViewer.getTable();
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        TableColumn tc1 = new TableColumn(table, SWT.LEFT);
        TableColumn tc2 = new TableColumn(table, SWT.LEFT);
        tc1.setText("state");
        tc2.setText("aggregate");
        tc1.setWidth(120);
        tc2.setWidth(150);
        tc1.setMoveable(true);
        tc2.setMoveable(true);
        if (RMStore.isConnected()) {
            statsViewer.init();
        }
    }

    /**
     * Passing the focus request to the viewer's control.
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        // viewer.getControl().setFocus();
    }

    /**
     * Called when view is closed
     * sacrifices statsViewer to garbage collector
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        if (statsViewer != null) {
            statsViewer.setInput(null);
            statsViewer = null;
        }
    }

    /**
     * @return statsViewer if view is activated,
     * null otherwise
     */
    public static RMStatsViewer getStatsViewer() {
        return statsViewer;
    }
}
