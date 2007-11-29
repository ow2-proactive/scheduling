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
package org.objectweb.proactive.extra.scheduler.gui.composite;

import java.util.ArrayList;
import java.util.List;
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
import org.objectweb.proactive.extra.scheduler.common.task.util.ResultDescriptorTool.SimpleTextPanel;
import org.objectweb.proactive.extra.scheduler.gui.actions.KillRemoveJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.ObtainJobOutputAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PauseResumeJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityHighJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityHighestJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityIdleJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityLowJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityLowestJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityNormalJobAction;
import org.objectweb.proactive.extra.scheduler.gui.data.EventJobsListener;
import org.objectweb.proactive.extra.scheduler.gui.data.EventTasksListener;
import org.objectweb.proactive.extra.scheduler.gui.data.JobsController;
import org.objectweb.proactive.extra.scheduler.gui.data.RunningJobsListener;
import org.objectweb.proactive.extra.scheduler.gui.data.SchedulerProxy;
import org.objectweb.proactive.extra.scheduler.gui.views.JobInfo;
import org.objectweb.proactive.extra.scheduler.gui.views.ResultPreview;
import org.objectweb.proactive.extra.scheduler.gui.views.TaskView;
import org.objectweb.proactive.extra.scheduler.job.InternalJob;


/**
 * This class represents the running jobs
 *
 * @author FRADJ Johann
 * @version 1.0, Jul 12, 2007
 * @since ProActive 3.2
 */
public class RunningJobComposite extends AbstractJobComposite
    implements RunningJobsListener, EventTasksListener, EventJobsListener {

    /** the unique id and the title for the column "Progress" */
    public static final String COLUMN_PROGRESS_TEXT_TITLE = "# Finished Tasks";
    public static final String COLUMN_PROGRESS_BAR_TITLE = "Progress";

    /** This list is used to remember which table editor wasn't disposed */
    private List<TableEditor> tableEditors = null;

    /** This list is used to remember which progress bar weren't disposed */
    private List<ProgressBar> progressBars = null;

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
        tableEditors = new ArrayList<TableEditor>();
        progressBars = new ArrayList<ProgressBar>();
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
            PriorityJobAction.getInstance().setEnabled(false);
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
            PriorityJobAction.getInstance().setEnabled(enabled);
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

        KillRemoveJobAction killRemoveJobAction = KillRemoveJobAction.getInstance();
        killRemoveJobAction.setKillMode();
        killRemoveJobAction.setEnabled(enabled);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.gui.composite.AbstractJobComposite#clear()
     */
    @Override
    public void clear() {
        for (TableEditor te : tableEditors)
            te.dispose();
        tableEditors.clear();
        for (ProgressBar pb : progressBars)
            pb.dispose();
        progressBars.clear();
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
        tc.setToolTipText("You cannot sort by this column");

        tc = new TableColumn(table, SWT.NONE, 2);
        tc.setText(COLUMN_PROGRESS_BAR_TITLE);
        tc.setWidth(70);
        tc.setMoveable(true);
        tc.setToolTipText("You cannot sort by this column");
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
                tableEditors.add(editor);
                progressBars.add(bar);
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
        if (!isDisposed()) {
            Vector<JobId> jobsId = getJobs();
            int tmp = -1;
            for (int i = 0; i < jobsId.size(); i++) {
                if (jobsId.get(i).equals(jobId)) {
                    tmp = i;
                    break;
                }
            }
            if (tmp == -1) {
                // TODO throw new IllegalArgumentException("jobId unknown : " +
                // jobId);
                return;
            }
            final int i = tmp;
            getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        int j = getTable().getSelectionIndex();
                        if (i == j) {
                            JobInfo jobInfo = JobInfo.getInstance();
                            if (jobInfo != null) {
                                jobInfo.clear();
                            }

                            ResultPreview resultPreview = ResultPreview.getInstance();
                            if (resultPreview != null) {
                                resultPreview.update(new SimpleTextPanel(
                                        "No selected task"));
                            }

                            TaskView taskView = TaskView.getInstance();
                            if (taskView != null) {
                                taskView.clear();
                            }

                            // enabling/disabling button permitted with this job
                            ObtainJobOutputAction.getInstance().setEnabled(false);
                            PriorityJobAction.getInstance().setEnabled(false);
                            PriorityIdleJobAction.getInstance().setEnabled(false);
                            PriorityLowestJobAction.getInstance()
                                                   .setEnabled(false);
                            PriorityLowJobAction.getInstance().setEnabled(false);
                            PriorityNormalJobAction.getInstance()
                                                   .setEnabled(false);
                            PriorityHighJobAction.getInstance().setEnabled(false);
                            PriorityHighestJobAction.getInstance()
                                                    .setEnabled(false);
                            PauseResumeJobAction pauseResumeJobAction = PauseResumeJobAction.getInstance();
                            pauseResumeJobAction.setEnabled(false);
                            pauseResumeJobAction.setPauseResumeMode();
                            KillRemoveJobAction.getInstance().setEnabled(false);
                        }
                        TableItem item = getTable().getItem(i);
                        ProgressBar bar = ((ProgressBar) item.getData("bar"));
                        TableEditor editor = ((TableEditor) item.getData(
                                "editor"));
                        bar.dispose();
                        progressBars.remove(bar);
                        editor.dispose();
                        tableEditors.remove(editor);
                        getTable().remove(i);
                        TableColumn[] cols = getTable().getColumns();
                        for (TableColumn col : cols)
                            col.notifyListeners(SWT.Move, null);
                        decreaseCount();
                    }
                });
        }
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
                            // TODO throw new IllegalArgumentException("the item
                            // which represent the job : "
                            // + taskEvent.getJobId() + " is unknown !");
                            return;
                        }

                        TableColumn[] cols = table.getColumns();
                        InternalJob job = JobsController.getLocalView()
                                                        .getJobById(taskEvent.getJobId());
                        for (int i = 0; i < cols.length; i++) {
                            String title = cols[i].getText();
                            if ((title != null) &&
                                    (title.equals(COLUMN_PROGRESS_BAR_TITLE) &&
                                    (!item.isDisposed()))) {
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
