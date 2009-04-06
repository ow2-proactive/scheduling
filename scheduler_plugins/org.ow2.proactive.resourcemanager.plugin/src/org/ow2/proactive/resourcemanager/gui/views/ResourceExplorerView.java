package org.ow2.proactive.resourcemanager.gui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.tree.RMTreeViewer;
import org.ow2.proactive.resourcemanager.gui.tree.TreeLabelProvider;
import org.ow2.proactive.resourcemanager.gui.tree.TreeSelectionListener;
import org.ow2.proactive.resourcemanager.gui.tree.actions.CollapseAllAction;
import org.ow2.proactive.resourcemanager.gui.tree.actions.ExpandAllAction;


/**
 * @author The ProActive Team
 */
public class ResourceExplorerView extends ViewPart {

    /** the view part id */
    public static final String ID = "org.ow2.proactive.resourcemanager.gui.views.ResourceExplorerView";
    private static RMTreeViewer treeViewer = null;
    private static Action expandAllAction = null;
    private static Action collapseAllAction = null;

    private static Shell rmShell = null;

    private DrillDownAdapter drillDownAdapter = null;
    private Composite parent = null;

    public static void init() {
        treeViewer.init();
        expandAllAction.setEnabled(true);
        collapseAllAction.setEnabled(true);
    }

    public static RMTreeViewer getTreeViewer() {
        return treeViewer;
    }

    public static void clearOnDisconnection() {
        expandAllAction.setEnabled(false);
        collapseAllAction.setEnabled(false);
    }

    /**
     * Callback that will allow us to create the viewer and initialize it.
     */
    @Override
    public void createPartControl(Composite theParent) {
        parent = theParent;
        treeViewer = new RMTreeViewer(this, parent);

        TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.LEFT, 0);
        column.setLabelProvider(new TreeLabelProvider());
        column.getColumn().setWidth(200);

        drillDownAdapter = new DrillDownAdapter(treeViewer);
        makeActions();
        hookContextMenu();
        contributeToActionBars();
        treeViewer.addSelectionChangedListener(new TreeSelectionListener());
        if (RMStore.isConnected()) {
            init();
            treeViewer.expandAll();
        }

        if (rmShell == null) {
            rmShell = Display.getDefault().getShells()[1];
        }
        System.out.println(rmShell);
    }

    public static Shell getRMShell() {
        return rmShell;
    }

    private void makeActions() {
        collapseAllAction = CollapseAllAction.newInstance(treeViewer);
        expandAllAction = ExpandAllAction.newInstance(treeViewer);
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("PopupMenu");
        menuMgr.add(new Separator());
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, treeViewer);
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
        manager.add(expandAllAction);
        manager.add(collapseAllAction);

    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
        manager.add(expandAllAction);
        manager.add(collapseAllAction);
    }

    /**
     * Called when view is closed
     * sacrifices treeViewer to garbage collector
     */
    public void dispose() {
        super.dispose();
        treeViewer = null;
    }

    /**
     * Passing the focus request to the viewer's control. 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        if (treeViewer != null) {
            treeViewer.treeFocused();
        }
    }
}