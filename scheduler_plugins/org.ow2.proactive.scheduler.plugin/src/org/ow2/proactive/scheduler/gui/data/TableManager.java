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
package org.ow2.proactive.scheduler.gui.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.gui.views.JobInfo;
import org.ow2.proactive.scheduler.gui.views.TaskView;


/**
 * This class allow to assert that are only one item selected in all table managed.
 *
 * @author The ProActive Team
 */
public class TableManager {
    // The shared instance
    private static TableManager instance = null;

    // managed tables
    private Vector<Table> tables = null;
    private List<JobId> jobsIdSelected = null;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    private TableManager() {
        tables = new Vector<Table>();
        jobsIdSelected = new ArrayList<JobId>();
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /*
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

    /*
     * To add a table which will be managed
     * 
     * @param table which will be managed
     */
    public void add(Table table) {
        table.addListener(SWT.Selection, new TableListener(table));
        tables.add(table);
    }

    /*
     * Returns a list containing all jobId of selected items
     * 
     * @return a list containing all jobId of selected items
     */
    public List<JobId> getJobsIdOfSelectedItems() {
        List<JobId> res = new ArrayList<JobId>();
        for (JobId id : jobsIdSelected)
            res.add(id);
        return res;
    }

    public void removeJobSelection(JobId jobId) {
        jobsIdSelected.remove(jobId);
    }

    /*
     * This method move a selection from a table to another.
     * 
     * @param jobId the jobId
     * 
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
                        JobState job = JobsController.getLocalView().getJobById(jId);

                        // update its informations
                        JobInfo jobInfo = JobInfo.getInstance();
                        if (jobInfo != null) {
                            jobInfo.updateInfos(job);
                        }

                        //TODO je pense que cet update est inutile !
                        // update its tasks informations
                        TaskView taskView = TaskView.getInstance();
                        if (taskView != null) {
                            taskView.fullUpdate(job);
                        }

                        // update the available buttons
                        ActionsManager.getInstance().update();

                        break;
                    }
            }
        });
    }

    /*
     * This method check if the item which represents the job (determinate by its jobId) is selected
     * 
     * @param jobId the jobId
     * 
     * @return true if the job is selected in the table
     */
    public boolean isJobSelected(JobId jobId) {
        return jobsIdSelected.contains(jobId);
    }

    /*
     * This method check if the item which represents the job (determinate by its jobId) is only the job selected
     * 
     * @param jobId the jobId
     * 
     * @return true if the job is selected in the table
     */
    public boolean isOnlyJobSelected(JobId jobId) {
        return (jobsIdSelected.size() == 1) && jobId.equals(jobsIdSelected.get(0));
    }

    public void clear() {
        jobsIdSelected.clear();
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
            Table selectedTable = table;
            TableItem[] selectedItems = selectedTable.getSelection();
            jobsIdSelected.clear();
            for (TableItem item : selectedItems)
                jobsIdSelected.add((JobId) item.getData());
        }
    }
}
