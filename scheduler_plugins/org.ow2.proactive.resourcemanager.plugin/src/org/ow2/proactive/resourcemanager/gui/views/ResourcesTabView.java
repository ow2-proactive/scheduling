package org.ow2.proactive.resourcemanager.gui.views;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.table.RMTableViewer;
import org.ow2.proactive.resourcemanager.gui.table.TableSelectionListener;


public class ResourcesTabView extends ViewPart {

    public static final String ID = "org.ow2.proactive.resourcemanager.gui.views.ResourcesTabView";
    private static RMTableViewer tabViewer = null;

    public static RMTableViewer getTabViewer() {
        return tabViewer;
    }

    public static void init() {
        tabViewer.init();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        tabViewer = new RMTableViewer(parent);
        Table table = tabViewer.getTable();
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        new TableColumn(table, SWT.LEFT).setText("Node source");
        new TableColumn(table, SWT.LEFT).setText("host");
        new TableColumn(table, SWT.LEFT).setText("state");
        new TableColumn(table, SWT.LEFT).setText("URL");
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.getColumn(0).setWidth(100);
        table.getColumn(1).setWidth(150);
        table.getColumn(2).setWidth(60);
        table.getColumn(2).setResizable(false);
        table.getColumn(2).setAlignment(SWT.CENTER);
        table.getColumn(3).setWidth(10);
        table.setSortColumn(table.getColumn(1));
        hookContextMenu();
        tabViewer.addSelectionChangedListener(new TableSelectionListener());
        if (RMStore.isConnected()) {
            tabViewer.init();
        }
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("PopupMenu");
        menuMgr.add(new Separator("top"));
        menuMgr.setRemoveAllWhenShown(true);
        Menu menu = menuMgr.createContextMenu(tabViewer.getControl());
        tabViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, tabViewer);
    }

    /**
     * Called when view is closed
     * sacrifices tabViewer to garbage collector
     */
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose() {
        super.dispose();
        if (tabViewer != null) {
            tabViewer.setInput(null);
            tabViewer = null;
        }
    }

    @Override
    public void setFocus() {
        if (tabViewer != null) {
            tabViewer.tabFocused();
        }
    }
}
