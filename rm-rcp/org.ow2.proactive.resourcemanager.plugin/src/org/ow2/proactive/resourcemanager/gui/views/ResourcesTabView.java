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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.handlers.ConnectHandler;
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
        new TableViewerColumn(tabViewer, SWT.LEFT).setLabelProvider(new TableLabelProvider(4));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.getColumn(0).setWidth(100);
        table.getColumn(0).setText("Node Source");
        table.getColumn(1).setWidth(150);
        table.getColumn(1).setText("Host");
        table.getColumn(2).setWidth(60);
        table.getColumn(2).setResizable(false);
        table.getColumn(2).setText("State");
        table.getColumn(2).setAlignment(SWT.CENTER);
        table.getColumn(3).setText("Since");
        table.getColumn(3).setWidth(120);
        table.getColumn(4).setText("URL");
        table.getColumn(4).setWidth(10);
        table.setSortColumn(table.getColumn(1));
        hookContextMenu();
        tabViewer.addSelectionChangedListener(new TableSelectionListener());
        if (RMStore.isConnected()) {
            tabViewer.init();
        }

        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                ConnectHandler.getHandler().execute(Display.getDefault().getActiveShell());
            }
        });
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
