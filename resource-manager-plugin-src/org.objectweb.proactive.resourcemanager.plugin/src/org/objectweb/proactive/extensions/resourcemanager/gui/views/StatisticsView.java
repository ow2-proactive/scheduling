package org.objectweb.proactive.extensions.resourcemanager.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.extensions.resourcemanager.gui.tree.TreeManager;
import org.objectweb.proactive.extensions.resourcemanager.gui.tree.TreeStatistic;


/**
 * @author FRADJ Johann
 */
public class StatisticsView extends ViewPart {

    /** the view part id */
    public static final String ID = "org.objectweb.proactive.extensions.resourcemanager.gui.views.StatisticsView";

    // the shared instance
    private static StatisticsView instance = null;
    private static boolean isDisposed = true;
    private Table table = null;

    /**
     * The constructor.
     */
    public StatisticsView() {
        instance = this;
    }

    public void maj() {
        TreeManager treeManager = TreeManager.getInstance();
        if (treeManager != null) {
            final TreeStatistic statistic = treeManager.getStatistic();
            if (statistic != null) {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        if (!table.isDisposed()) {
                            // Turn off drawing to avoid flicker
                            table.setRedraw(false);

                            table.removeAll();
                            TableItem item;

                            // free
                            item = new TableItem(table, SWT.NONE);
                            item.setText(new String[] { "# free nodes", statistic.getFreeNodes() + "" });
                            // busy
                            item = new TableItem(table, SWT.NONE);
                            item.setText(new String[] { "# busy nodes", statistic.getBusyNodes() + "" });
                            // down
                            item = new TableItem(table, SWT.NONE);
                            item.setText(new String[] { "# down nodes", statistic.getDownNodes() + "" });

                            //                            TableColumn[] cols = table.getColumns();
                            //                            for (TableColumn tc : cols) {
                            //                                tc.pack();
                            //                            }

                            // Turn on drawing
                            table.setRedraw(true);
                        }
                    }
                });
            }
        }
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize it.
     */
    @Override
    public void createPartControl(Composite parent) {
        isDisposed = false;
        table = new Table(parent, SWT.BORDER | SWT.SINGLE);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tc1 = new TableColumn(table, SWT.LEFT);
        TableColumn tc2 = new TableColumn(table, SWT.LEFT);
        tc1.setText("name");
        tc2.setText("value");
        tc1.setWidth(120);
        tc2.setWidth(150);
        tc1.setMoveable(true);
        tc2.setMoveable(true);
        //maj();
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
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static StatisticsView getInstance() {
        if (isDisposed) {
            return null;
        }
        return instance;
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        isDisposed = true;
        super.dispose();
    }

    /**
     * To clear the view
     */
    public void clear() {
        table.removeAll();
    }

    /**
     * to display or not the view
     * 
     * @param isVisible
     */
    public void setVisible(boolean isVisible) {
        if (table != null) {
            table.setVisible(isVisible);
        }
    }

    /**
     * To enabled or not the view
     * 
     * @param isEnabled
     */
    public void setEnabled(boolean isEnabled) {
        if (table != null) {
            table.setEnabled(isEnabled);
        }
    }
}
