/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.scheduler.gui.data;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.objectweb.proactive.extensions.scheduler.common.job.JobId;
import org.objectweb.proactive.extensions.scheduler.gui.views.JobInfo;
import org.objectweb.proactive.extensions.scheduler.gui.views.TaskView;
import org.objectweb.proactive.extensions.scheduler.job.InternalJob;


/**
 * This class allow to assert that are only one item selected in all table managed.
 *
 * @author The ProActive Team
 * @version 1.0, Jul 12, 2007
 * @since ProActive 3.2
 */
public class TableManager {
    // The shared instance
    private static TableManager instance = null;

    // managed tables
    private Vector<Table> tables = null;
    private Table lastSelectedTable = null;
    private TableItem lastSelectedItem = null;
    private JobId jobIdOfLastSelectedItem = null;
    private Integer idOfLastSelectedTable = null;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    private TableManager() {
        tables = new Vector<Table>();
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static TableManager getInstance() {
        if (instance == null) {
            instance = new TableManager();
        }
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }

    /**
     * To add a table which will be managed
     *
     * @param table which will be managed
     */
    public void add(Table table) {
        table.addListener(SWT.Selection, new TableListener(table));
        tables.add(table);
    }

    /**
     * Returns the last selected item
     *
     * @return the last selected item
     */
    public TableItem getLastSelectedItem() {
        return lastSelectedItem;
    }

    /**
     * Returns the last jobId of the last selected item
     *
     * @return the last jobId of the last selected item
     */
    public JobId getLastJobIdOfLastSelectedItem() {
        return jobIdOfLastSelectedItem;
    }

    /**
     * This method move a selection from a table to another.
     *
     * @param jobId the jobId
     * @param tableId the tableId
     */
    public void moveJobSelection(JobId jobId, int tableId) {
        final int tId = tableId;
        final JobId jId = jobId;
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                // get the table
                Table table = getTableById(tId);

                // get all items
                TableItem[] items = table.getItems();
                for (int i = 0; i < items.length; i++)
                    if (((JobId) items[i].getData()).equals(jId)) {
                        // select the job
                        table.select(i);

                        // get the job by jobId
                        InternalJob job = JobsController.getLocalView().getJobById(jId);

                        // update its informations
                        JobInfo jobInfo = JobInfo.getInstance();
                        if (jobInfo != null) {
                            jobInfo.updateInfos(job);
                        }

                        // update its tasks informations
                        TaskView taskView = TaskView.getInstance();
                        if (taskView != null) {
                            taskView.fullUpdate(job);
                        }
                    }
            }
        });
    }

    /**
     * This method check if the item which represents the job (determinate by
     * its jobId) is selected in the table identified by its id (tableId)
     *
     * @param jobId the jobId
     * @param tableId the tableId
     * @return true if the job is selected in the table
     */
    public boolean isJobSelectedInThisTable(JobId jobId, int tableId) {
        if ((lastSelectedTable == null) || (lastSelectedItem == null) || (jobIdOfLastSelectedItem == null)) {
            return false;
        }
        return jobIdOfLastSelectedItem.equals(jobId);
    }

    public boolean isItTheLastSelectedTable(Integer tableId) {
        return tableId.equals(idOfLastSelectedTable);
    }

    // -------------------------------------------------------------------- //
    // ----------------------------- private ------------------------------ //
    // -------------------------------------------------------------------- //
    private Table getTableById(int tableId) {
        for (Table table : tables)
            if (((Integer) table.getData()).equals(tableId)) {
                return table;
            }
        throw new IllegalArgumentException("the tableId : " + tableId + " is unknwon !");
    }

    // -------------------------------------------------------------------- //
    // --------------------------- inner class ---------------------------- //
    // -------------------------------------------------------------------- //
    private class TableListener implements Listener {
        private Table table = null;

        public TableListener(Table table) {
            this.table = table;
        }

        public void handleEvent(Event event) {
            for (Table table : tables)
                if (!this.table.equals(table)) {
                    table.deselectAll();
                }
            lastSelectedTable = table;
            TableItem[] items = lastSelectedTable.getSelection();
            if (items.length <= 0) {
                // Normally impossible to be here...
                lastSelectedItem = null;
                jobIdOfLastSelectedItem = null;
                idOfLastSelectedTable = null;
            } else {
                lastSelectedItem = items[0];
                jobIdOfLastSelectedItem = (JobId) lastSelectedItem.getData();
                idOfLastSelectedTable = (Integer) lastSelectedTable.getData();
            }
        }
    }
}
