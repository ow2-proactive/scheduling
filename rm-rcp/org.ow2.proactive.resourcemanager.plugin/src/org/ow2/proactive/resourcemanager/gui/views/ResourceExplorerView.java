/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
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
import org.ow2.proactive.resourcemanager.gui.actions.CollapseAllAction;
import org.ow2.proactive.resourcemanager.gui.actions.ExpandAllAction;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.tree.RMTreeViewer;
import org.ow2.proactive.resourcemanager.gui.tree.TreeLabelProvider;
import org.ow2.proactive.resourcemanager.gui.tree.TreeSelectionListener;


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
            rmShell = Display.getDefault().getActiveShell();
        }

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
    @Override
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