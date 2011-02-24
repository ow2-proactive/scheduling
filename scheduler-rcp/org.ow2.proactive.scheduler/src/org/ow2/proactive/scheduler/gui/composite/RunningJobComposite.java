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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.composite;

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
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.util.ResultPreviewTool.SimpleTextPanel;
import org.ow2.proactive.scheduler.gui.data.ActionsManager;
import org.ow2.proactive.scheduler.gui.data.JobsController;
import org.ow2.proactive.scheduler.gui.data.TableManager;
import org.ow2.proactive.scheduler.gui.listeners.EventJobsListener;
import org.ow2.proactive.scheduler.gui.listeners.EventTasksListener;
import org.ow2.proactive.scheduler.gui.listeners.RunningJobsListener;
import org.ow2.proactive.scheduler.gui.views.ResultPreview;
import org.ow2.proactive.scheduler.gui.views.TaskView;


/**
 * This class represents the running jobs
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class RunningJobComposite extends AbstractJobComposite implements RunningJobsListener,
        EventTasksListener, EventJobsListener {

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
    public RunningJobComposite(Composite parent, JobsController jobsController) {
        super(parent, "Running", RUNNING_TABLE_ID);
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
     * @see org.ow2.proactive.scheduler.gui.composite.AbstractJobComposite#getJobs()
     */
    @Override
    public Vector<JobId> getJobs() {
        return JobsController.getLocalView().getRunningsJobs();
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.composite.AbstractJobComposite#sortJobs()
     */
    @Override
    public void sortJobs() {
        JobsController.getLocalView().sortRunningsJobs();
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.composite.AbstractJobComposite#clear()
     */
    @Override
    public void clear() {
        for (TableEditor te : tableEditors) {
            te.dispose();
        }
        tableEditors.clear();
        for (ProgressBar pb : progressBars) {
            pb.dispose();
        }
        progressBars.clear();
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.composite.AbstractJobComposite#createTable(org.eclipse.swt.widgets.Composite,
     *      int)
     */
    @Override
    protected Table createTable(Composite parent, int tableId) {
        Table table = super.createTable(parent, tableId);
        TableColumn tc = new TableColumn(table, SWT.RIGHT, 2);
        tc.setText(COLUMN_PROGRESS_TEXT_TITLE);
        tc.setWidth(78);
        tc.setMoveable(true);
        tc.setToolTipText("You cannot sort by this column");

        tc = new TableColumn(table, SWT.NONE, 2);
        tc.setText(COLUMN_PROGRESS_BAR_TITLE);
        tc.setWidth(100);
        tc.setMoveable(true);
        tc.setToolTipText("You cannot sort by this column");
        return table;
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.composite.AbstractJobComposite#createItem(org.ow2.proactive.scheduler.common.job.JobState, int)
     */
    @Override
    protected TableItem createItem(JobState job) {
        Table table = getTable();
        TableItem item = super.createItem(job);
        TableColumn[] cols = table.getColumns();
        for (int i = 0; i < cols.length; i++) {
            String title = cols[i].getText();
            if (title.equals(COLUMN_PROGRESS_BAR_TITLE)) {
                ProgressBar bar = new ProgressBar(table, SWT.NONE);
                bar.setMaximum(job.getTotalNumberOfTasks() * 100);
                bar.setSelection(job.getNumberOfFinishedTasks() * 100);
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
                item.setText(i, job.getNumberOfFinishedTasks() + "/" + job.getTotalNumberOfTasks());
            } else if (title.equals(COLUMN_STATE_TITLE)) {
            }
        }
        return item;
    }

    // -------------------------------------------------------------------- //
    // ----------------- implements RunningJobsListener ------------------ //
    // -------------------------------------------------------------------- //
    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.RunningJobsListener#addRunningJob(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public void addRunningJob(JobId jobId) {
        addJob(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.RunningJobsListener#removeRunningJob(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public void removeRunningJob(final JobId jobId) {
        if (!isDisposed()) {
            Vector<JobId> jobsId = getJobs();
            int tmp = -1;
            for (int i = 0; i < jobsId.size(); i++) {
                if (jobsId.get(i).equals(jobId)) {
                    tmp = i;
                    break;
                }
            }
            final int i = tmp;
            getDisplay().syncExec(new Runnable() {
                public void run() {
                    int[] j = getTable().getSelectionIndices();

                    TableItem item = getTable().getItem(i);
                    TableEditor editor = ((TableEditor) item.getData("editor"));
                    ProgressBar bar = ((ProgressBar) item.getData("bar"));
                    bar.setSelection(bar.getMaximum());
                    try {
                        Thread.sleep(150);
                    } catch (Exception e) {
                    }
                    bar.dispose();
                    progressBars.remove(bar);
                    editor.dispose();
                    tableEditors.remove(editor);
                    getTable().remove(i);

                    //compute progress if task is restarted
                    setProgressBarValue(JobsController.getLocalView().getJobById(jobId), item);

                    if (j.length == 1) {
                        if (i == j[0]) {
                            org.ow2.proactive.scheduler.gui.views.JobInfo jobInfo = org.ow2.proactive.scheduler.gui.views.JobInfo
                                    .getInstance();
                            if (jobInfo != null) {
                                jobInfo.clear();
                            }

                            ResultPreview resultPreview = ResultPreview.getInstance();
                            if (resultPreview != null) {
                                resultPreview.update(new SimpleTextPanel("No task selected"));
                            }

                            TaskView taskView = TaskView.getInstance();
                            if (taskView != null) {
                                taskView.clear();
                            }
                        }
                    } else if (j.length > 1) {
                        List<JobState> jobs = JobsController.getLocalView().getJobsByIds(
                                TableManager.getInstance().getJobsIdOfSelectedItems());

                        org.ow2.proactive.scheduler.gui.views.JobInfo jobInfo = org.ow2.proactive.scheduler.gui.views.JobInfo
                                .getInstance();
                        if (jobInfo != null) {
                            jobInfo.updateInfos(jobs);
                        }

                        ResultPreview resultPreview = ResultPreview.getInstance();
                        if (resultPreview != null) {
                            resultPreview.update(new SimpleTextPanel("No selected task"));
                        }

                        TaskView taskView = TaskView.getInstance();
                        if (taskView != null) {
                            taskView.fullUpdate(jobs);
                        }
                    }

                    TableColumn[] cols = getTable().getColumns();
                    for (TableColumn col : cols) {
                        col.notifyListeners(SWT.Move, null);
                    }
                    // enabling/disabling button permitted with this job
                    ActionsManager.getInstance().update();
                    decreaseCount();
                }
            });
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.RunningJobsListener#taskReplicated(JobId)
     */
    public void taskReplicated(JobId jobId) {
        super.stateUpdate(jobId);

        if (!this.isDisposed()) {
            final JobId jobi = jobId;

            getDisplay().syncExec(new Runnable() {

                public void run() {
                    Table table = getTable();
                    TableItem[] items = table.getItems();
                    TableItem item = null;
                    for (TableItem it : items) {
                        if (((JobId) (it.getData())).equals(jobi)) {
                            item = it;
                            break;
                        }
                    }

                    JobState job = JobsController.getLocalView().getJobById(jobi);
                    ((ProgressBar) item.getData("bar")).setMaximum(job.getTotalNumberOfTasks() * 100);
                    //compute progress if task is restarted
                    setProgressBarValue(job, item);

                    TableColumn[] cols = table.getColumns();
                    for (int i = 0; i < cols.length; i++) {
                        String title = cols[i].getText();
                        if (COLUMN_PROGRESS_TEXT_TITLE.equals(title)) {
                            item.setText(i, job.getNumberOfFinishedTasks() + "/" +
                                job.getTotalNumberOfTasks());
                        }
                    }
                }
            });
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.RunningJobsListener#taskSkipped(JobId)
     */
    public void taskSkipped(JobId jobId) {
        super.stateUpdate(jobId);

        if (!this.isDisposed()) {
            final JobId jobi = jobId;

            getDisplay().syncExec(new Runnable() {

                public void run() {
                    Table table = getTable();
                    TableItem[] items = table.getItems();
                    TableItem item = null;
                    for (TableItem it : items) {
                        if (((JobId) (it.getData())).equals(jobi)) {
                            item = it;
                            break;
                        }
                    }

                    JobState job = JobsController.getLocalView().getJobById(jobi);
                    ((ProgressBar) item.getData("bar")).setMaximum(job.getTotalNumberOfTasks() * 100);
                    //compute progress if task is restarted
                    setProgressBarValue(job, item);

                    TableColumn[] cols = table.getColumns();
                    for (int i = 0; i < cols.length; i++) {
                        String title = cols[i].getText();
                        if (COLUMN_PROGRESS_TEXT_TITLE.equals(title)) {
                            item.setText(i, job.getNumberOfFinishedTasks() + "/" +
                                job.getTotalNumberOfTasks());
                        }
                    }
                }
            });
        }
    }

    // -------------------------------------------------------------------- //
    // ----------------- implements FinishedTasksListener ----------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.EventTasksListener#runningTaskEvent(org.objectweb.proactive.extra.scheduler.task.TaskInfo)
     */
    public void runningTaskEvent(final TaskInfo info) {
        super.stateUpdate(info.getJobId());
        switch (info.getStatus()) {
            case WAITING_ON_FAILURE:
            case WAITING_ON_ERROR:
            case FAULTY:
            case ABORTED:
            case FAILED:
                getDisplay().syncExec(new Runnable() {
                    public void run() {
                        TableItem[] items = getTable().getItems();
                        for (TableItem it : items) {
                            if (((JobId) (it.getData())).equals(info.getJobId())) {
                                //compute progress if task is restarted
                                setProgressBarValue(
                                        JobsController.getLocalView().getJobById(info.getJobId()), it);
                                break;
                            }
                        }
                    }
                });
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.EventTasksListener#finishedTaskEvent(org.objectweb.proactive.extra.scheduler.task.TaskInfo)
     */
    public void finishedTaskEvent(TaskInfo info) {
        super.stateUpdate(info.getJobId());
        if (!this.isDisposed()) {
            final TaskInfo taskInfo = info;

            getDisplay().syncExec(new Runnable() {
                public void run() {
                    Table table = getTable();
                    TableItem[] items = table.getItems();
                    TableItem item = null;
                    for (TableItem it : items) {
                        if (((JobId) (it.getData())).equals(taskInfo.getJobId())) {
                            item = it;
                            break;
                        }
                    }

                    TableColumn[] cols = table.getColumns();
                    JobState job = JobsController.getLocalView().getJobById(taskInfo.getJobId());
                    for (int i = 0; i < cols.length; i++) {
                        String title = cols[i].getText();
                        if (COLUMN_PROGRESS_TEXT_TITLE.equals(title)) {
                            item.setText(i, job.getNumberOfFinishedTasks() + "/" +
                                job.getTotalNumberOfTasks());
                        }
                    }

                    //compute progress
                    setProgressBarValue(job, item);
                }
            });
        }
    }

    public void progressTaskEvent(TaskInfo info) {
        super.stateUpdate(info.getJobId());
        if (!this.isDisposed()) {
            final TaskInfo taskInfo = info;

            getDisplay().syncExec(new Runnable() {
                public void run() {
                    Table table = getTable();
                    TableItem[] items = table.getItems();
                    TableItem item = null;
                    for (TableItem it : items) {
                        if (((JobId) (it.getData())).equals(taskInfo.getJobId())) {
                            item = it;
                            break;
                        }
                    }

                    JobState job = JobsController.getLocalView().getJobById(taskInfo.getJobId());
                    //compute progress
                    setProgressBarValue(job, item);
                }
            });
        }
    }

    private void setProgressBarValue(JobState job, TableItem item) {
        int progress = 0;
        for (TaskState ts : job.getTasks()) {
            progress += ts.getProgress();
        }
        if (!item.isDisposed()) {
            ((ProgressBar) item.getData("bar")).setSelection(progress);
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------- implements EventJobsListener ------------------- //
    // -------------------------------------------------------------------- //

    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.EventJobsListener#pausedEvent(org.objectweb.proactive.extra.scheduler.job.JobInfo)
     */
    public void pausedEvent(JobInfo info) {
        stateUpdate(info);
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.EventJobsListener#resumedEvent(org.objectweb.proactive.extra.scheduler.job.JobInfo)
     */
    public void resumedEvent(JobInfo info) {
        stateUpdate(info);
    }

    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.EventJobsListener#priorityChangedEvent(org.objectweb.proactive.extra.scheduler.job.JobInfo)
     */
    public void priorityChangedEvent(JobInfo info) {
        JobId jobId = info.getJobId();
        if (getJobs().contains(jobId)) {
            super.priorityUpdate(jobId);
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------- implements EventJobsListener ------------------- //
    // -------------------------------------------------------------------- //
    private void stateUpdate(JobInfo info) {
        JobId jobId = info.getJobId();
        if (getJobs().contains(jobId)) {
            super.stateUpdate(jobId);
        }
    }

}
