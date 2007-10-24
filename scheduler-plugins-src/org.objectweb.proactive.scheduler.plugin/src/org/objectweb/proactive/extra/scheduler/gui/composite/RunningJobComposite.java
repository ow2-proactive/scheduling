/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://proactive.inria.fr/team_members.htm Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.scheduler.gui.composite;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.objectweb.proactive.extra.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobState;
import org.objectweb.proactive.extra.scheduler.common.task.TaskEvent;
import org.objectweb.proactive.extra.scheduler.gui.actions.KillJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.ObtainJobOutputAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PauseResumeJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityHighJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityHighestJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityIdleJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityLowJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityLowestJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityNormalJobAction;
import org.objectweb.proactive.extra.scheduler.gui.data.EventJobsListener;
import org.objectweb.proactive.extra.scheduler.gui.data.EventTasksListener;
import org.objectweb.proactive.extra.scheduler.gui.data.JobsController;
import org.objectweb.proactive.extra.scheduler.gui.data.RunningJobsListener;
import org.objectweb.proactive.extra.scheduler.gui.data.SchedulerProxy;
import org.objectweb.proactive.extra.scheduler.job.InternalJob;


/**
 * This class represents the running jobs
 *
 * @author ProActive Team
 * @version 1.0, Jul 12, 2007
 * @since ProActive 3.2
 */
public class RunningJobComposite extends AbstractJobComposite
    implements RunningJobsListener, EventTasksListener, EventJobsListener {

    /** the unique id and the title for the column "Progress" */
    public static final String COLUMN_PROGRESS_TEXT_TITLE = "# Finished Tasks";
    public static final String COLUMN_PROGRESS_BAR_TITLE = "Progress";

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * This is the default constructor.
     *
     * @param parent
     * @param title
     * @param jobsController
     */
    public RunningJobComposite(Composite parent, String title,
        JobsController jobsController) {
        super(parent, title, RUNNING_TABLE_ID);
        jobsController.addRunningJobsListener(this);
        jobsController.addEventTasksListener(this);
        jobsController.addEventJobsListener(this);
    }

    // -------------------------------------------------------------------- //
    // ---------------------- extends JobComposite ------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.composites.AbstractJobComposite#getJobs()
     */
    @Override
    public Vector<JobId> getJobs() {
        return JobsController.getLocalView().getRunningsJobs();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.composites.AbstractJobComposite#sortJobs()
     */
    @Override
    public void sortJobs() {
        JobsController.getLocalView().sortRunningsJobs();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.composites.AbstractJobComposite#jobSelected(org.objectweb.proactive.extra.scheduler.job.Job)
     */
    @Override
    public void jobSelected(InternalJob job) {
        // enabling/disabling button permitted with this job
        boolean enabled = SchedulerProxy.getInstance().isItHisJob(job.getOwner());
        PauseResumeJobAction pauseResumeJobAction = PauseResumeJobAction.getInstance();

        switch (JobsController.getSchedulerState()) {
        case SHUTTING_DOWN:
        case KILLED:
            PriorityIdleJobAction.getInstance().setEnabled(false);
            PriorityLowestJobAction.getInstance().setEnabled(false);
            PriorityLowJobAction.getInstance().setEnabled(false);
            PriorityNormalJobAction.getInstance().setEnabled(false);
            PriorityHighJobAction.getInstance().setEnabled(false);
            PriorityHighestJobAction.getInstance().setEnabled(false);

            pauseResumeJobAction.setEnabled(false);
            pauseResumeJobAction.setPauseResumeMode();
            break;
        default:
            PriorityIdleJobAction.getInstance().setEnabled(enabled);
            PriorityLowestJobAction.getInstance().setEnabled(enabled);
            PriorityLowJobAction.getInstance().setEnabled(enabled);
            PriorityNormalJobAction.getInstance().setEnabled(enabled);
            PriorityHighJobAction.getInstance().setEnabled(enabled);
            PriorityHighestJobAction.getInstance().setEnabled(enabled);

            pauseResumeJobAction.setEnabled(enabled);
            JobState jobState = job.getState();
            if (jobState.equals(JobState.PAUSED)) {
                pauseResumeJobAction.setResumeMode();
            } else if (jobState.equals(JobState.RUNNING) ||
                    jobState.equals(JobState.PENDING) ||
                    jobState.equals(JobState.STALLED)) {
                pauseResumeJobAction.setPauseMode();
            } else {
                pauseResumeJobAction.setPauseResumeMode();
            }
        }

        ObtainJobOutputAction.getInstance().setEnabled(enabled);
        KillJobAction.getInstance().setEnabled(enabled);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.composites.AbstractJobComposite#createTable(org.eclipse.swt.widgets.Composite,
     *      int)
     */
    @Override
    protected Table createTable(Composite parent, int tableId) {
        Table table = super.createTable(parent, tableId);
        TableColumn tc = new TableColumn(table, SWT.RIGHT, 2);
        tc.setText(COLUMN_PROGRESS_TEXT_TITLE);
        tc.setWidth(70);
        tc.setMoveable(true);
        tc.setToolTipText("You can't sort by this column");

        tc = new TableColumn(table, SWT.NONE, 2);
        tc.setText(COLUMN_PROGRESS_BAR_TITLE);
        tc.setWidth(70);
        tc.setMoveable(true);
        tc.setToolTipText("You can't sort by this column");
        return table;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.composites.AbstractJobComposite#createItem(org.objectweb.proactive.extra.scheduler.job.Job)
     */
    @Override
    protected TableItem createItem(InternalJob job, int itemIndex) {
        Table table = getTable();
        TableItem item = super.createItem(job, itemIndex);
        TableColumn[] cols = table.getColumns();
        for (int i = 0; i < cols.length; i++) {
            String title = cols[i].getText();
            if (title.equals(COLUMN_PROGRESS_BAR_TITLE)) {
                ProgressBar bar = new ProgressBar(table, SWT.NONE);
                bar.setMaximum(job.getTotalNumberOfTasks());
                bar.setSelection(job.getNumberOfFinishedTask());
                TableEditor editor = new TableEditor(table);
                editor.grabHorizontal = true;
                editor.grabVertical = true;
                editor.setEditor(bar, item, i);
                editor.layout();
                table.layout();
                item.setData("bar", bar);
                item.setData("editor", editor);
            } else if (title.equals(COLUMN_PROGRESS_TEXT_TITLE)) {
                item.setText(i,
                    job.getNumberOfFinishedTask() + "/" +
                    job.getTotalNumberOfTasks());
            }
        }
        return item;
    }

    // -------------------------------------------------------------------- //
    // ----------------- implements RunningJobsListener ------------------ //
    // -------------------------------------------------------------------- //
    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.data.RunningJobsListener#addRunningJob(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    @Override
    public void addRunningJob(JobId jobId) {
        addJob(jobId);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.data.RunningJobsListener#removeRunningJob(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    @Override
    public void removeRunningJob(JobId jobId) {
        removeJob(jobId);
        // TODO : deux problème ici :
        // - je ne dispose ni la progress ni le tableEditor
        // - j'envoi le notifyListeners sur toutes les colonnes...
        TableColumn[] cols = getTable().getColumns();
        for (TableColumn col : cols)
            col.notifyListeners(SWT.Move, null);
    }

    // -------------------------------------------------------------------- //
    // ----------------- implements FinishedTasksListener ----------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.data.EventTasksListener#runningTaskEvent(org.objectweb.proactive.extra.scheduler.task.TaskEvent)
     */
    @Override
    public void runningTaskEvent(TaskEvent event) {
        super.stateUpdate(event.getJobId());
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.data.EventTasksListener#finishedTaskEvent(org.objectweb.proactive.extra.scheduler.task.TaskEvent)
     */
    @Override
    public void finishedTaskEvent(TaskEvent event) {
        super.stateUpdate(event.getJobId());
        if (!this.isDisposed()) {
            final TaskEvent taskEvent = event;

            getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        Table table = getTable();
                        TableItem[] items = table.getItems();
                        TableItem item = null;
                        for (TableItem it : items)
                            if (((JobId) (it.getData())).equals(
                                        taskEvent.getJobId())) {
                                item = it;
                                break;
                            }

                        if (item == null) {
                            throw new IllegalArgumentException(
                                "the item which represent the job : " +
                                taskEvent.getJobId() + " is unknown !");
                        }

                        TableColumn[] cols = table.getColumns();
                        InternalJob job = JobsController.getLocalView()
                                                        .getJobById(taskEvent.getJobId());
                        for (int i = 0; i < cols.length; i++) {
                            String title = cols[i].getText();
                            if ((title != null) &&
                                    (title.equals(COLUMN_PROGRESS_BAR_TITLE))) {
                                ((ProgressBar) item.getData("bar")).setSelection(job.getNumberOfFinishedTask());
                            } else if ((title != null) &&
                                    (title.equals(COLUMN_PROGRESS_TEXT_TITLE))) {
                                item.setText(i,
                                    job.getNumberOfFinishedTask() + "/" +
                                    job.getTotalNumberOfTasks());
                            }
                        }
                    }
                });
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------- implements EventJobsListener ------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.data.EventJobsListener#killedEvent(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    @Override
    public void killedEvent(JobId jobId) {
        // Do nothing
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.data.EventJobsListener#pausedEvent(org.objectweb.proactive.extra.scheduler.job.JobEvent)
     */
    @Override
    public void pausedEvent(JobEvent event) {
        stateUpdate(event);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.data.EventJobsListener#resumedEvent(org.objectweb.proactive.extra.scheduler.job.JobEvent)
     */
    @Override
    public void resumedEvent(JobEvent event) {
        stateUpdate(event);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.data.EventJobsListener#priorityChangedEvent(org.objectweb.proactive.extra.scheduler.job.JobEvent)
     */
    @Override
    public void priorityChangedEvent(JobEvent event) {
        JobId jobId = event.getJobId();
        if (getJobs().contains(jobId)) {
            super.priorityUpdate(jobId);
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------- implements EventJobsListener ------------------- //
    // -------------------------------------------------------------------- //
    private void stateUpdate(JobEvent event) {
        JobId jobId = event.getJobId();
        if (getJobs().contains(jobId)) {
            super.stateUpdate(jobId);
        }
    }
}
