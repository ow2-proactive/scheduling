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
package org.ow2.proactive.scheduler.gui.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
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

    private Timer dotTimer;

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
        dotTimer = new Timer("DotTimer");
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
    protected TableItem createItem(JobState job, int itemIndex) {
        Table table = getTable();
        TableItem item = super.createItem(job, itemIndex);
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
                DotTask dt = new DotTask(getDisplay(), item, i, job);
                item.setData("dotTask", dt);
                dotTimer.schedule(dt, 1000, DotTask.REFRESH_BAR_DELAY * 1000);
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
            final int i = tmp;
            getDisplay().syncExec(new Runnable() {
                public void run() {
                    int[] j = getTable().getSelectionIndices();

                    TableItem item = getTable().getItem(i);
                    TableEditor editor = ((TableEditor) item.getData("editor"));
                    ProgressBar bar = ((ProgressBar) item.getData("bar"));
                    JobState job = ((DotTask) item.getData("dotTask")).getJob();
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

                    DotTask.updateRunningFactor(job);

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

    // -------------------------------------------------------------------- //
    // ----------------- implements FinishedTasksListener ----------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.ow2.proactive.scheduler.gui.listeners.EventTasksListener#runningTaskEvent(org.objectweb.proactive.extra.scheduler.task.TaskInfo)
     */
    public void runningTaskEvent(TaskInfo info) {
        super.stateUpdate(info.getJobId());
        switch (info.getStatus()) {
            case WAITING_ON_FAILURE:
            case WAITING_ON_ERROR:
            case FAULTY:
            case ABORTED:
            case FAILED:
                TableItem[] items = getTable().getItems();
                for (TableItem it : items) {
                    if (((JobId) (it.getData())).equals(info.getJobId())) {
                        ((DotTask) it.getData("dotTask")).restartTask();
                        break;
                    }
                }
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

                    ((DotTask) item.getData("dotTask")).finishedTask();

                    TableColumn[] cols = table.getColumns();
                    JobState job = JobsController.getLocalView().getJobById(taskInfo.getJobId());
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

class DotTask extends TimerTask {

    private static final String TOTAL_ESTIMATED_JOB_TIME_PROPERTY = "job.average.completion";
    private static final String MAKE_AVERAGE_PROPERTY = "job.average.makeaverage";
    protected static final int REFRESH_BAR_DELAY = 5;//in second
    private static final int RUNNING_TASK_MAX_PERCENT = 92;
    //default and setting value
    private static int FINISHED_TASK_REDUCE_FACTOR = 1;
    private static int TOTAL_ESTIMATED_JOB_TIME = 60 * 60;//1 hour (in second)
    private static boolean MAKE_AVERAGE = false;
    //calculated value
    private static int RUNNING_TASK_REDUCE_FACTOR;

    private TableItem item;
    private Display display;
    private JobState job;
    private int colId;
    private double[] tasks;
    private int index;

    static {
        try {
            if (System.getProperty(TOTAL_ESTIMATED_JOB_TIME_PROPERTY) != null) {
                try {
                    //set total estimated job time
                    TOTAL_ESTIMATED_JOB_TIME = Integer.parseInt(System
                            .getProperty(TOTAL_ESTIMATED_JOB_TIME_PROPERTY));
                } catch (Exception e) {
                    //no way to get the int value from property... keep default value
                }
            }
            //set running reduce factor to fit estimated job time
            RUNNING_TASK_REDUCE_FACTOR = findX();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (System.getProperty(MAKE_AVERAGE_PROPERTY) != null) {
            try {
                //set total estimated job time
                MAKE_AVERAGE = Boolean.parseBoolean(System.getProperty(MAKE_AVERAGE_PROPERTY));
            } catch (Exception e) {
                //no way to get the int value from property... keep default value
            }
        }
    }

    private static int totalTasks = 0;
    private static int jobNumber = 0;

    public static void updateRunningFactor(JobState job) {
        if (MAKE_AVERAGE) {
            try {
                totalTasks += job.getTotalNumberOfTasks();
                jobNumber++;
                //compute mean job duration and set total estimated job time
                TOTAL_ESTIMATED_JOB_TIME = (TOTAL_ESTIMATED_JOB_TIME + (int) (job.getFinishedTime() - job
                        .getStartTime()) / 1000) / 2;
                //set running reduce factor to fit estimated job time
                RUNNING_TASK_REDUCE_FACTOR = findX();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create a new instance of DotTask
     *
     * @param items
     * @param jobs
     * @param colIds
     * @param display
     */
    public DotTask(Display display, TableItem item, int colId, JobState job) {
        this.item = item;
        this.colId = colId;
        this.display = display;
        this.job = job;
        this.tasks = new double[job.getTotalNumberOfTasks()];
        this.index = job.getNumberOfFinishedTasks();
        for (int i = 0; i < this.index; i++) {
            this.tasks[i] = 100;
        }
    }

    @Override
    public void run() {
        if (item.isDisposed()) {
            this.cancel();
            return;
        }
        if (job.getStatus() == JobStatus.RUNNING) {
            double tmp = 0;
            for (int i = 0; i < index; i++) {
                if (tasks[i] < 100) {
                    tasks[i] += ((100 - tasks[i]) / (FINISHED_TASK_REDUCE_FACTOR));
                }
                tmp += tasks[i];
            }
            try {
                for (int i = index; i < index + job.getNumberOfRunningTasks(); i++) {
                    if (tasks[i] < RUNNING_TASK_MAX_PERCENT) {
                        tasks[i] += ((100 - tasks[i]) / (RUNNING_TASK_REDUCE_FACTOR));
                    }
                    tmp += tasks[i];
                }
                final int sum = (int) tmp;

                display.syncExec(new Runnable() {
                    public void run() {
                        try {
                            ((ProgressBar) item.getData("bar")).setSelection(sum);
                            item.setText(colId, JobStatus.RUNNING + " (" + job.getNumberOfRunningTasks() +
                                ")");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void finishedTask() {
        index++;
        run();
    }

    public void restartTask() {
        try {
            //shift values
            for (int i = index; i < tasks.length - 1; i++) {
                tasks[i] = tasks[i + 1];
            }
            //set last to 0;
            this.tasks[tasks.length - 1] = 0;
            index--;
            run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int findX() {
        double sum;
        int step;
        boolean terminated = false;
        int x1 = 1, x2 = 130000, x = (x1 + x2) / 2;//x2=divisor for 15 days
        if (TOTAL_ESTIMATED_JOB_TIME > 15 * 24 * 3600) {//more than 15 days
            return x2;
        }
        int stepToReach = (int) Math.round((double) TOTAL_ESTIMATED_JOB_TIME / (double) REFRESH_BAR_DELAY);
        if (jobNumber > 0) {
            stepToReach += (totalTasks / jobNumber);
        } else if (stepToReach < 1) {
            return x1;
        }
        int i = 0;
        do {
            sum = 0;
            step = 0;
            do {
                sum += (100 - sum) / x;
                i++;
                step++;
                if (step > stepToReach) {
                    x2 = x;
                    x = (x1 + x2) / 2;
                    if (x2 == x) {
                        terminated = true;
                    }
                    sum = 0;
                    step = 0;
                    break;
                } else if (sum > RUNNING_TASK_MAX_PERCENT) {
                    x1 = x;
                    x = (x1 + x2) / 2;
                    if (x1 == x) {
                        terminated = true;
                    }
                    sum = 0;
                    step = 0;
                    break;
                }
            } while (!terminated);
        } while (!terminated);
        return x;
    }

    /**
     * Get the job
     *
     * @return the job
     */
    public JobState getJob() {
        return job;
    }

}
