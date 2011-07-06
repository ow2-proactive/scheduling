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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.stats.RMHostsStatsViewer;
import org.ow2.proactive.resourcemanager.gui.stats.RMNodesStatsViewer;


/**
 * @author The ProActive Team
 */
public class StatisticsView extends ViewPart {

    /**
     * view part id
     */
    public static final String ID = "org.ow2.proactive.resourcemanager.gui.views.StatisticsView";
    private static RMNodesStatsViewer nodesStatsViewer = null;
    private static RMHostsStatsViewer hostsStatsViewer = null;

    public static void init() {
        nodesStatsViewer.init();
        hostsStatsViewer.init();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout(1, true));
        nodesStatsViewer = new RMNodesStatsViewer(parent);
        Table table = nodesStatsViewer.getTable();
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        TableColumn tc0 = new TableColumn(table, SWT.LEFT);
        TableColumn tc1 = new TableColumn(table, SWT.LEFT);
        TableColumn tc2 = new TableColumn(table, SWT.LEFT);
        tc0.setText("");
        tc1.setText("Node Status");
        tc2.setText("Count");
        tc0.setWidth(30);
        tc1.setWidth(120);
        tc2.setWidth(150);
        tc0.setMoveable(false);
        tc0.setResizable(false);
        tc1.setMoveable(false);
        tc2.setMoveable(false);

        hostsStatsViewer = new RMHostsStatsViewer(parent);
        table = hostsStatsViewer.getTable();
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        tc0 = new TableColumn(table, SWT.LEFT);
        tc1 = new TableColumn(table, SWT.LEFT);
        tc2 = new TableColumn(table, SWT.LEFT);
        tc0.setText("");
        tc1.setText("Host Status");
        tc2.setText("Count");
        tc0.setWidth(30);
        tc1.setWidth(120);
        tc2.setWidth(150);
        tc0.setMoveable(false);
        tc0.setResizable(false);
        tc1.setMoveable(false);
        tc2.setMoveable(false);

        if (RMStore.isConnected()) {
            nodesStatsViewer.init();
            hostsStatsViewer.init();
        }

        parent.pack();
        parent.layout();
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
     * Called when view is closed
     * sacrifices statsViewer to garbage collector
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        nodesStatsViewer = null;
        hostsStatsViewer = null;
    }

    /**
     * @return statsViewer if view is activated,
     * null otherwise
     */
    public static RMNodesStatsViewer getNodesStatsViewer() {
        return nodesStatsViewer;
    }

    /**
     * @return statsViewer if view is activated,
     * null otherwise
     */
    public static RMHostsStatsViewer getHostsStatsViewer() {
        return hostsStatsViewer;
    }
}
