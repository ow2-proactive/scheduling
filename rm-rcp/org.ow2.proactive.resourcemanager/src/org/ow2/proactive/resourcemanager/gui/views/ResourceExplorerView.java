/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.ow2.proactive.resourcemanager.gui.actions.CollapseAllAction;
import org.ow2.proactive.resourcemanager.gui.actions.ExpandAllAction;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.handlers.ConnectHandler;
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
    private static TreeViewerColumn column = null;
    private static Action expandAllAction = null;
    private static Action collapseAllAction = null;

    private static Shell rmShell = null;

    private Composite parent = null;

    public static void init() {
        treeViewer.init();
        column.getColumn().pack();
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
    public void createPartControl(final Composite theParent) {
        parent = theParent;
        treeViewer = new RMTreeViewer(this, parent);

        column = new TreeViewerColumn(treeViewer, SWT.LEFT, 0);
        column.setLabelProvider(new TreeLabelProvider());

        makeActions();
        hookContextMenu();
        contributeToActionBars();
        treeViewer.addSelectionChangedListener(new TreeSelectionListener());

        if (RMStore.isConnected()) {
            init();
            treeViewer.expandAll();
        } else {
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    ConnectHandler.getHandler().execute(theParent.getShell());
                }
            });
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
        manager.add(expandAllAction);
        manager.add(collapseAllAction);
        manager.add(new Separator());

    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(expandAllAction);
        manager.add(collapseAllAction);
        manager.add(new Separator());
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