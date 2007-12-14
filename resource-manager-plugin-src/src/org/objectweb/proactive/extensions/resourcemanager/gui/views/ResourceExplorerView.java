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
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMInitialState;
import org.objectweb.proactive.extensions.resourcemanager.gui.actions.CollapseAllAction;
import org.objectweb.proactive.extensions.resourcemanager.gui.actions.ConnectDeconnectResourceManagerAction;
import org.objectweb.proactive.extensions.resourcemanager.gui.actions.AddNodeAction;
import org.objectweb.proactive.extensions.resourcemanager.gui.actions.CreateSourceAction;
import org.objectweb.proactive.extensions.resourcemanager.gui.actions.ExpandAllAction;
import org.objectweb.proactive.extensions.resourcemanager.gui.actions.RemoveNodeAction;
import org.objectweb.proactive.extensions.resourcemanager.gui.actions.RemoveSourceAction;
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
    private static Action doubleClickAction = null;
    private static Action simpleClickAction = null;
    private static Action expandAllAction = null;
    private static Action collapseAllAction = null;
    private static Action createSourceAction = null;
    private static Action createNodeAction = null;
    private static Action removeNodeAction = null;
    private static Action removeSourceAction = null;
    private static Action connectDeconnectAction = null;
    private static Composite parent = null;

    /**
     * The constructor.
     */
    public ResourceExplorerView() {
    }

    public static void init() {
        RMInitialState initialState = ResourceManagerController.getLocalView().getInitialState();
        viewer.initTree(initialState);
        expandAllAction.setEnabled(true);
        collapseAllAction.setEnabled(true);
        createSourceAction.setEnabled(true);
        removeNodeAction.setEnabled(true);
        removeSourceAction.setEnabled(true);
        createNodeAction.setEnabled(true);
    }

    public static void clearOnDisconnection() {
        viewer.clear();
        if (StatisticsView.getInstance() != null)
            StatisticsView.getInstance().clear();
        expandAllAction.setEnabled(false);
        collapseAllAction.setEnabled(false);
        createSourceAction.setEnabled(false);
        removeNodeAction.setEnabled(false);
        removeSourceAction.setEnabled(false);
        createNodeAction.setEnabled(false);
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize it.
     */
    @Override
    public void createPartControl(Composite theParent) {
        parent = theParent;
        try {
            viewer = new ResourceExplorerTree(this, parent);

            ResourceManagerController.getLocalView().addNodeListener(viewer);

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
                if (drillDownAdapter.canGoInto())
                    drillDownAdapter.goInto();
            }
        };
        simpleClickAction = new Action() {
            @Override
            public void run() {
                // ISelection selection = viewer.getSelection();
                // if (selection != null) {
                // Object obj = ((IStructuredSelection) selection).getFirstElement();
                // if (obj != null)
                // System.out.println("simple-click detected on " + obj.toString());
                // }
            }
        };
        collapseAllAction = CollapseAllAction.newInstance(viewer);
        expandAllAction = ExpandAllAction.newInstance(viewer);
        createSourceAction = CreateSourceAction.newInstance(parent);
        removeSourceAction = RemoveSourceAction.newInstance(parent, viewer);
        createNodeAction = AddNodeAction.newInstance(parent, viewer);
        removeNodeAction = RemoveNodeAction.newInstance(parent, viewer);
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
        manager.add(createSourceAction);
        manager.add(removeSourceAction);
        manager.add(createNodeAction);
        manager.add(removeNodeAction);
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
        manager.add(expandAllAction);
        manager.add(collapseAllAction);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(connectDeconnectAction);
        manager.add(createSourceAction);
        manager.add(removeSourceAction);
        manager.add(createNodeAction);
        manager.add(removeNodeAction);
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