/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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
    public void createPartControl(final Composite parent) {
        tabViewer = new RMTableViewer(parent);
        Table table = tabViewer.getTable();
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        final TableViewerColumn nodeSourceTableViewerColumn = new TableViewerColumn(tabViewer, SWT.NONE);
        nodeSourceTableViewerColumn.setLabelProvider(new TableLabelProvider(0));
        nodeSourceTableViewerColumn.getColumn().setText("Node Source");
        nodeSourceTableViewerColumn.getColumn().setWidth(100);

        final TableViewerColumn hostTableViewerColumn = new TableViewerColumn(tabViewer, SWT.NONE);
        hostTableViewerColumn.setLabelProvider(new TableLabelProvider(1));
        hostTableViewerColumn.getColumn().setText("Host");
        hostTableViewerColumn.getColumn().setWidth(120);

        final TableViewerColumn stateTableViewerColumn = new TableViewerColumn(tabViewer, SWT.NONE);
        stateTableViewerColumn.setLabelProvider(new TableLabelProvider(2));
        stateTableViewerColumn.getColumn().setText("State");
        stateTableViewerColumn.getColumn().setWidth(40);
        stateTableViewerColumn.getColumn().setResizable(false);
        stateTableViewerColumn.getColumn().setAlignment(SWT.CENTER);

        final TableViewerColumn sinceTableViewerColumn = new TableViewerColumn(tabViewer, SWT.NONE);
        sinceTableViewerColumn.setLabelProvider(new TableLabelProvider(3));
        sinceTableViewerColumn.getColumn().setText("Since");
        sinceTableViewerColumn.getColumn().setWidth(120);

        final TableViewerColumn urlTableViewerColumn = new TableViewerColumn(tabViewer, SWT.NONE);
        urlTableViewerColumn.setLabelProvider(new TableLabelProvider(4));
        urlTableViewerColumn.getColumn().setText("URL");
        urlTableViewerColumn.getColumn().setWidth(300);

        final TableViewerColumn providerTableViewerColumn = new TableViewerColumn(tabViewer, SWT.NONE);
        providerTableViewerColumn.setLabelProvider(new TableLabelProvider(5));
        providerTableViewerColumn.getColumn().setText("Provider");
        providerTableViewerColumn.getColumn().setWidth(100);

        final TableViewerColumn ownerTableViewerColumn = new TableViewerColumn(tabViewer, SWT.NONE);
        ownerTableViewerColumn.setLabelProvider(new TableLabelProvider(6));
        ownerTableViewerColumn.getColumn().setText("Owner");
        ownerTableViewerColumn.getColumn().setWidth(100);

        table.setSortColumn(table.getColumn(1));
        hookContextMenu();
        tabViewer.addSelectionChangedListener(new TableSelectionListener());

        if (RMStore.isConnected()) {
            tabViewer.init();
        } else {
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    ConnectHandler.getHandler().execute(parent.getShell());
                }
            });
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
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
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
