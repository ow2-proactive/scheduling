package org.objectweb.proactive.extensions.resourcemanager.gui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMInitialState;
import org.objectweb.proactive.extensions.resourcemanager.gui.actions.CollapseAllAction;
import org.objectweb.proactive.extensions.resourcemanager.gui.actions.ConnectDeconnectResourceManagerAction;
import org.objectweb.proactive.extensions.resourcemanager.gui.actions.ExpandAllAction;
import org.objectweb.proactive.extensions.resourcemanager.gui.data.ResourceManagerController;
import org.objectweb.proactive.extensions.resourcemanager.gui.tree.ResourceExplorerTree;


/**
 * @author FRADJ Johann
 */
public class ResourceExplorerView extends ViewPart {

    /** the view part id */
    public static final String ID = "org.objectweb.proactive.extensions.resourcemanager.gui.views.ResourceExplorerView";
    private static ResourceExplorerTree viewer = null;
    private DrillDownAdapter drillDownAdapter = null;
    private Action doubleClickAction = null;
    private Action simpleClickAction = null;
    private ExpandAllAction expandAllAction = null;
    private CollapseAllAction collapseAllAction = null;
    private ConnectDeconnectResourceManagerAction connectDeconnectAction = null;
    private static Composite parent = null;

    /**
     * The constructor.
     */
    public ResourceExplorerView() {
    }

    public static void init() {
        RMInitialState initialState = ResourceManagerController.getLocalView().getInitialState();
        viewer.initTree(initialState);
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    @Override
    public void createPartControl(Composite theParent) {
        parent = theParent;
        try {
            viewer = new ResourceExplorerTree(this, parent);

            //			RMAdmin rmAdmin = RMConnection.connectAsAdmin(null);
            //			RMMonitoring imMonitoring = RMConnection.connectAsMonitor(null);
            ResourceManagerController.getLocalView().addNodeListener(viewer);
            //			ResourceManagerController.getActiveView().init(imMonitoring);
            //			RMInitialState i = ResourceManagerController.getLocalView().getInitialState();
            //			viewer.initTree(i);
            drillDownAdapter = new DrillDownAdapter(viewer);

            makeActions();
            hookContextMenu();
            contributeToActionBars();
            hookClickAction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeActions() {
        doubleClickAction = new Action() {
            @Override
            public void run() {
                if (drillDownAdapter.canGoInto()) {
                    drillDownAdapter.goInto();
                }
            }
        };
        simpleClickAction = new Action() {
            @Override
            public void run() {
                //				ISelection selection = viewer.getSelection();
                //				if (selection != null) {
                //					Object obj = ((IStructuredSelection) selection).getFirstElement();
                //					if (obj != null)
                //						System.out.println("simple-click detected on " + obj.toString());
                //				}
            }
        };
        collapseAllAction = CollapseAllAction.newInstance(viewer);
        expandAllAction = ExpandAllAction.newInstance(viewer);
        connectDeconnectAction = ConnectDeconnectResourceManagerAction.newInstance(parent);
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }
        });

        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(connectDeconnectAction);
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
        manager.add(expandAllAction);
        manager.add(collapseAllAction);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(connectDeconnectAction);
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
        manager.add(expandAllAction);
        manager.add(collapseAllAction);
    }

    private void hookClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                simpleClickAction.run();
            }
        });
    }

    /**
     * Passing the focus request to the viewer's control.
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }
}
// private void showMessage(String message) {
// MessageDialog.openInformation(viewer.getControl().getShell(), "Ressource
// Explorer", message);
// }
