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
package org.ow2.proactive.scheduler.gui.views;

import java.util.List;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.scheduler.util.Tools;
import org.ow2.proactive.scheduler.gui.data.JobsController;
import org.ow2.proactive.scheduler.gui.data.TableManager;
import org.ow2.proactive.scheduler.job.InternalJob;


/**
 * This view display many informations about a job.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class JobInfo extends ViewPart {

    /* an id */
    public static final String ID = "org.ow2.proactive.scheduler.gui.views.JobInfo";

    // The shared instance
    private static JobInfo instance = null;
    private static boolean isDisposed = true;
    private Table table = null;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /*
     * The default constructor
     */
    public JobInfo() {
        instance = this;
    }

    // -------------------------------------------------------------------- //
    // ----------------------------- public ------------------------------- //
    // -------------------------------------------------------------------- //
    /*
     * For display info about the given job
     * 
     * @param job a job
     */
    public void updateInfos(InternalJob job) {
        setVisible(true);
        Vector<String> propertiesName = new Vector<String>();
        Vector<Object> propertiesValue = new Vector<Object>();
        propertiesName.add("Id");
        propertiesValue.add(job.getId());
        propertiesName.add("State");
        propertiesValue.add(job.getState());
        propertiesName.add("Name");
        propertiesValue.add(job.getName());
        propertiesName.add("Priority");
        propertiesValue.add(job.getPriority());

        // Tasks **************************************************
        propertiesName.add("Pending tasks number");
        propertiesValue.add(job.getJobInfo().getNumberOfPendingTasks());
        propertiesName.add("Running tasks number");
        propertiesValue.add(job.getJobInfo().getNumberOfRunningTasks());
        propertiesName.add("Finished tasks number");
        propertiesValue.add(job.getJobInfo().getNumberOfFinishedTasks());
        propertiesName.add("Total tasks number");
        propertiesValue.add(job.getJobInfo().getTotalNumberOfTasks());

        // Time ******************************************************
        propertiesName.add("Submitted time");
        propertiesValue.add(Tools.getFormattedDate(job.getJobInfo().getSubmittedTime()));
        propertiesName.add("Started time");
        propertiesValue.add(Tools.getFormattedDate(job.getJobInfo().getStartTime()));
        propertiesName.add("Finished time");
        propertiesValue.add(Tools.getFormattedDate(job.getJobInfo().getFinishedTime()));

        // Duration ******************************************************
        propertiesName.add("Pending duration");
        propertiesValue.add(Tools.getFormattedDuration(job.getJobInfo().getSubmittedTime(), job.getJobInfo()
                .getStartTime()));
        propertiesName.add("Execution duration");
        propertiesValue.add(Tools.getFormattedDuration(job.getJobInfo().getStartTime(), job.getJobInfo()
                .getFinishedTime()));
        propertiesName.add("Total duration");
        propertiesValue.add(Tools.getFormattedDuration(job.getJobInfo().getSubmittedTime(), job.getJobInfo()
                .getFinishedTime()));

        // Others ******************************************************
        propertiesName.add("Description");
        propertiesValue.add(job.getDescription());

        fill(propertiesName, propertiesValue);
    }

    /*
     * For display info about the given job
     * 
     * @param job a job
     */
    public void updateInfos(List<InternalJob> jobs) {
        int pendingTasks = 0;
        int runningTasks = 0;
        int finishedTasks = 0;
        int totalTasks = 0;

        for (InternalJob job : jobs) {
            pendingTasks += job.getJobInfo().getNumberOfPendingTasks();
            runningTasks += job.getJobInfo().getNumberOfRunningTasks();
            finishedTasks += job.getJobInfo().getNumberOfFinishedTasks();
            totalTasks += job.getJobInfo().getTotalNumberOfTasks();
        }

        setVisible(true);
        Vector<String> propertiesName = new Vector<String>();
        Vector<Object> propertiesValue = new Vector<Object>();

        // Tasks **************************************************
        propertiesName.add("Pending tasks number");
        propertiesValue.add(pendingTasks);
        propertiesName.add("Running tasks number");
        propertiesValue.add(runningTasks);
        propertiesName.add("Finished tasks number");
        propertiesValue.add(finishedTasks);
        propertiesName.add("Total tasks number");
        propertiesValue.add(totalTasks);

        fill(propertiesName, propertiesValue);
    }

    private void fill(Vector<String> propertiesName, Vector<Object> propertiesValue) {
        if (propertiesName.size() != propertiesValue.size()) {
            throw new IllegalStateException(
                "The list propertiesName and propertiesValue must have the same size !");
        }

        if (!table.isDisposed()) {
            table.removeAll();
            TableItem item;
            for (int i = 0; i < propertiesName.size(); i++) {
                item = new TableItem(table, SWT.NONE);
                item
                        .setText(new String[] { propertiesName.get(i),
                                "  " + propertiesValue.get(i).toString() });
            }
            TableColumn[] cols = table.getColumns();
            for (TableColumn tc : cols) {
                tc.pack();
            }
        }
    }

    /*
     * To clear the view
     */
    public void clear() {
        table.removeAll();
    }

    /*
     * to display or not the view
     * 
     * @param isVisible
     */
    public void setVisible(boolean isVisible) {
        if (table != null) {
            table.setVisible(isVisible);
        }
    }

    /*
     * To enabled or not the view
     * 
     * @param isEnabled
     */
    public void setEnabled(boolean isEnabled) {
        if (table != null) {
            table.setEnabled(isEnabled);
        }
    }

    /*
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static JobInfo getInstance() {
        if (isDisposed) {
            return null;
        }
        return instance;
    }

    // -------------------------------------------------------------------- //
    // ------------------------- extends viewPart ------------------------- //
    // -------------------------------------------------------------------- //
    /*
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        isDisposed = false;
        table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tc1 = new TableColumn(table, SWT.LEFT);
        TableColumn tc2 = new TableColumn(table, SWT.LEFT);

        tc1.setText("Property");
        tc2.setText("Value");

        tc1.setWidth(100);
        tc2.setWidth(100);

        TableManager tableManager = TableManager.getInstance();
        if (tableManager != null) {
            List<JobId> jobIds = tableManager.getJobsIdOfSelectedItems();
            if (jobIds.size() == 1)
                updateInfos(JobsController.getLocalView().getJobById(jobIds.get(0)));
            else if (jobIds.size() > 0)
                updateInfos(JobsController.getLocalView().getJobsByIds(jobIds));
        }
    }

    /*
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        // TODO petit problème, Eclipse envoi 3 fois d'afiler le mm event
        // setFocus quand la fenêtre a une fenetre "onglet" voisine...

        //	 TableManager tableManager = TableManager.getInstance();
        //	 if (tableManager != null) {
        //	 TableItem item = tableManager.getLastSelectedItem();
        //	 if (item != null)
        //	 updateInfos(JobsController.getInstance().getJobById((IntWrapper)
        //	 item.getData()));
        //	 }
    }

    /*
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        isDisposed = true;
        super.dispose();
    }
}
