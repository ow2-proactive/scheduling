/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 *              Nice-Sophia Antipolis/ActiveEon
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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.data;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


public abstract class TableColumnSorter extends ViewerComparator {
    public static final int ASC = 1;
    public static final int NONE = 0;
    public static final int DESC = -1;

    private int direction = 0;
    private TableColumn column = null;
    private int columnIndex = 0;
    final private TableViewer viewer;

    final private SelectionListener selectionHandler = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
            TableColumn selectedColumn = (TableColumn) e.widget;
            TableColumnSorter.this.setColumn(selectedColumn);
        }
    };

    public TableColumnSorter(TableViewer viewer) {
        this.viewer = viewer;
        viewer.setComparator(this);

        for (TableColumn tableColumn : viewer.getTable().getColumns()) {
            tableColumn.addSelectionListener(selectionHandler);
        }
    }

    public void setColumn(TableColumn selectedColumn) {
        if (column == selectedColumn) {
            switch (direction) {
                case ASC:
                    direction = DESC;
                    break;
                case DESC:
                    direction = ASC;
                    break;
                default:
                    direction = ASC;
                    break;
            }
        } else {
            this.column = selectedColumn;
            this.direction = ASC;
        }

        Table table = viewer.getTable();
        switch (direction) {
            case ASC:
                table.setSortColumn(selectedColumn);
                table.setSortDirection(SWT.UP);
                break;
            case DESC:
                table.setSortColumn(selectedColumn);
                table.setSortDirection(SWT.DOWN);
                break;
            default:
                table.setSortColumn(null);
                table.setSortDirection(SWT.NONE);
                break;
        }

        TableColumn[] columns = table.getColumns();
        for (int i = 0; i < columns.length; i++) {
            TableColumn theColumn = columns[i];
            if (theColumn == this.column)
                columnIndex = i;
        }
        viewer.setComparator(null);
        viewer.setComparator(this);
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        return direction * doCompare(viewer, columnIndex, e1, e2);
    }

    protected abstract int doCompare(Viewer v, int index, Object e1, Object e2);
}