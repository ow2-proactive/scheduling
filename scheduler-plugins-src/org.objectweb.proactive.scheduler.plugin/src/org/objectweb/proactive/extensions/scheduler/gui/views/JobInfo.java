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
package org.objectweb.proactive.extensions.scheduler.gui.views;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.extensions.scheduler.common.job.JobId;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.Tools;
import org.objectweb.proactive.extensions.scheduler.gui.data.JobsController;
import org.objectweb.proactive.extensions.scheduler.gui.data.TableManager;
import org.objectweb.proactive.extensions.scheduler.job.InternalJob;


/**
 * This view display many informations about a job.
 *
 * @author FRADJ Johann
 * @version 1.0, Jul 12, 2007
 * @since ProActive 3.2
 */
public class JobInfo extends ViewPart {

    /** an id */
    public static final String ID = "org.objectweb.proactive.extensions.scheduler.gui.views.JobInfo";

    // The shared instance
    private static JobInfo instance = null;
    private static boolean isDisposed = true;
    private Table table = null;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * The default constructor
     *
     */
    public JobInfo() {
        instance = this;
    }

    // -------------------------------------------------------------------- //
    // ----------------------------- public ------------------------------- //
    // -------------------------------------------------------------------- //
    /**
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
        propertiesValue.add(Tools.getFormattedDate(job.getJobInfo()
                                                      .getSubmittedTime()));
        propertiesName.add("Started time");
        propertiesValue.add(Tools.getFormattedDate(job.getJobInfo()
                                                      .getStartTime()));
        propertiesName.add("Finished time");
        propertiesValue.add(Tools.getFormattedDate(job.getJobInfo()
                                                      .getFinishedTime()));

        // Duration ******************************************************
        propertiesName.add("Pending duration");
        propertiesValue.add(Tools.getFormattedDuration(
                job.getJobInfo().getSubmittedTime(),
                job.getJobInfo().getStartTime()));
        propertiesName.add("Execution duration");
        propertiesValue.add(Tools.getFormattedDuration(
                job.getJobInfo().getStartTime(),
                job.getJobInfo().getFinishedTime()));
        propertiesName.add("Total duration");
        propertiesValue.add(Tools.getFormattedDuration(
                job.getJobInfo().getSubmittedTime(),
                job.getJobInfo().getFinishedTime()));

        // Others ******************************************************
        propertiesName.add("Description");
        propertiesValue.add(job.getDescription());

        if (propertiesName.size() != propertiesValue.size()) {
            throw new IllegalArgumentException(
                "The list propertiesName and propertiesValue must have the same size !");
        }

        if (!table.isDisposed()) {
            table.removeAll();
            TableItem item;
            for (int i = 0; i < propertiesName.size(); i++) {
                item = new TableItem(table, SWT.NONE);
                item.setText(new String[] {
                        propertiesName.get(i),
                        "  " + propertiesValue.get(i).toString()
                    });
            }
            TableColumn[] cols = table.getColumns();
            for (TableColumn tc : cols) {
                tc.pack();
            }
        }
    }

    /**
     * To clear the view
     */
    public void clear() {
        table.removeAll();
    }

    /**
     * to display or not the view
     *
     * @param isVisible
     */
    public void setVisible(boolean isVisible) {
        if (table != null) {
            table.setVisible(isVisible);
        }
    }

    /**
     * To enabled or not the view
     *
     * @param isEnabled
     */
    public void setEnabled(boolean isEnabled) {
        if (table != null) {
            table.setEnabled(isEnabled);
        }
    }

    /**
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
    /**
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        isDisposed = false;
        table = new Table(parent, SWT.BORDER | SWT.SINGLE);
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
            JobId jobId = tableManager.getLastJobIdOfLastSelectedItem();
            if (jobId != null) {
                updateInfos(JobsController.getLocalView().getJobById(jobId));
            }
        }
    }

    /**
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

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        isDisposed = true;
        super.dispose();
    }
}
