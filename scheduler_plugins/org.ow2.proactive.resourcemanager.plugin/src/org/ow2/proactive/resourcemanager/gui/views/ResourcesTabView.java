package org.ow2.proactive.resourcemanager.gui.views;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.table.RMTableViewer;
import org.ow2.proactive.resourcemanager.gui.table.TableLabelProvider;
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

    @Override
    public void createPartControl(Composite parent) {
        tabViewer = new RMTableViewer(parent);
        Table table = tabViewer.getTable();
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        new TableViewerColumn(tabViewer, SWT.LEFT).setLabelProvider(new TableLabelProvider(0));
        new TableViewerColumn(tabViewer, SWT.LEFT).setLabelProvider(new TableLabelProvider(1));
        new TableViewerColumn(tabViewer, SWT.CENTER).setLabelProvider(new TableLabelProvider(2));
        new TableViewerColumn(tabViewer, SWT.LEFT).setLabelProvider(new TableLabelProvider(3));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.getColumn(0).setWidth(100);
        table.getColumn(0).setText("Node Source");
        table.getColumn(1).setWidth(150);
        table.getColumn(1).setText("host");
        table.getColumn(2).setWidth(60);
        table.getColumn(2).setResizable(false);
        table.getColumn(2).setText("State");
        table.getColumn(2).setAlignment(SWT.CENTER);
        table.getColumn(3).setText("URL");
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
        tabViewer = null;
    }

    @Override
    public void setFocus() {
        if (tabViewer != null) {
            tabViewer.tabFocused();
        }
    }
}
