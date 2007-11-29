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

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobState;
import org.objectweb.proactive.extra.scheduler.common.task.util.ResultDescriptorTool.SimpleTextPanel;
import org.objectweb.proactive.extra.scheduler.gui.Colors;
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
import org.objectweb.proactive.extra.scheduler.gui.data.JobsController;
import org.objectweb.proactive.extra.scheduler.gui.data.JobsOutputController;
import org.objectweb.proactive.extra.scheduler.gui.data.TableManager;
import org.objectweb.proactive.extra.scheduler.gui.views.JobInfo;
import org.objectweb.proactive.extra.scheduler.gui.views.ResultPreview;
import org.objectweb.proactive.extra.scheduler.gui.views.TaskView;
import org.objectweb.proactive.extra.scheduler.job.InternalJob;


/**
 * This class represents a composite which will be able to display many
 * information of a list of jobs.
 *
 * @author FRADJ Johann
 * @version 1.0, Jul 12, 2007
 * @since ProActive 3.2
 */
public abstract class AbstractJobComposite extends Composite {

    /** the unique id fort the "pending" table */
    public static final int PENDING_TABLE_ID = 0;

    /** the unique id fort the "running" table */
    public static final int RUNNING_TABLE_ID = 1;

    /** the unique id fort the "finished" table */
    public static final int FINISHED_TABLE_ID = 2;

    /** the unique id and the title for the column "Id" */
    public static final String COLUMN_ID_TITLE = "Id";

    /** the unique id and the title for the column "Priority" */
    public static final String COLUMN_PRIORITY_TITLE = "Priority";

    /** the unique id and the title for the column "Name" */
    public static final String COLUMN_NAME_TITLE = "Name";

    /** the unique id and the title for the column "User" */
    public static final String COLUMN_OWNER_TITLE = "User";

    /** the unique id and the title for the column "State" */
    public static final String COLUMN_STATE_TITLE = "State";

    /** the jobs failed background color */
    public static final Color JOB_FAILED_BACKGROUND_COLOR = Colors.RED;

    /** the jobs canceled background color */
    public static final Color JOB_CANCELED_BACKGROUND_COLOR = Colors.DARK_ORANGE;
    private Label label = null;
    private Table table = null;
    private int count = 0;
    private String title = null;
    private int order = InternalJob.ASC_ORDER;
    private int lastSorting = InternalJob.SORT_BY_ID;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * This is the default constructor
     *
     * @param parent the parent
     * @param title a title
     * @param jobsController an instance of jobsController
     * @param tableId an unique id for the table
     */
    public AbstractJobComposite(Composite parent, String title, int tableId) {
        super(parent, SWT.NONE);
        this.setLayout(new GridLayout());
        this.title = title;
        this.label = createLabel(parent, title);
        this.table = createTable(parent, tableId);
    }

    // -------------------------------------------------------------------- //
    // ----------------------------- private ------------------------------ //
    // -------------------------------------------------------------------- //
    private void setCount(int count) {
        if (!this.isDisposed()) {
            final int c = count;
            getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        label.setText(title + " (" + c + ")");
                    }
                });
        }
    }

    private void refreshTable() {
        if (!isDisposed()) {
            // Turn off drawing to avoid flicker
            table.setRedraw(false);

            // We remove all the table entries and then add the new entries
            table.removeAll();

            Vector<JobId> jobsId = getJobs();
            int i = 0;
            for (JobId jobId : jobsId)
                addJobInTable(jobId, i++);

            // Turn drawing back on
            table.setRedraw(true);
        }
    }

    private void addJobInTable(JobId jobId, int anItemIndex) {
        if (!isDisposed()) {
            final InternalJob job = JobsController.getLocalView()
                                                  .getJobById(jobId);
            final int itemIndex = anItemIndex;
            getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        createItem(job, itemIndex);
                    }
                });
        }
    }

    private void sort(SelectionEvent event, int field) {
        if (lastSorting == field) {
            order = (order == InternalJob.DESC_ORDER) ? InternalJob.ASC_ORDER
                                                      : InternalJob.DESC_ORDER;
            InternalJob.setSortingOrder(order);
        }
        InternalJob.setSortingBy(field);
        lastSorting = field;

        sortJobs();

        refreshTable();
        table.setSortColumn((TableColumn) event.widget);
        table.setSortDirection((order == InternalJob.DESC_ORDER) ? SWT.DOWN
                                                                 : SWT.UP);
    }

    private void fillBackgroundColor(TableItem item, JobState state, Color col) {
        boolean setFont = false;
        switch (state) {
        case CANCELLED:
            setFont = true;
            item.setForeground(JOB_CANCELED_BACKGROUND_COLOR);
            break;
        case FAILED:
            setFont = true;
            item.setForeground(JOB_FAILED_BACKGROUND_COLOR);
            break;
        case FINISHED:
        case PAUSED:
        case PENDING:
        case RUNNING:
        case STALLED:
        }
        if (setFont) {
            Font font = item.getFont();
            item.setFont(new Font(font.getDevice(),
                    font.getFontData()[0].getName(),
                    font.getFontData()[0].getHeight(),
                    font.getFontData()[0].getStyle() | SWT.BOLD));
        }
    }

    // -------------------------------------------------------------------- //
    // ---------------------------- protected ----------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * Create and return a Label
     *
     * @param parent the parent
     * @param title a title
     * @return
     */
    protected Label createLabel(Composite parent, String title) {
        Label label = new Label(this, SWT.CENTER);
        label.setText(title + " (" + count + ")");
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setForeground(Colors.RED);
        return label;
    }

    /**
     * Create and return a table
     *
     * @param parent the parent
     * @param tableId an unique id for the table
     * @return
     */
    protected Table createTable(Composite parent, int tableId) {
        table = new Table(this, SWT.BORDER | SWT.SINGLE);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setData(tableId);

        // creating TableColumn
        TableColumn tc1 = new TableColumn(table, SWT.RIGHT);
        TableColumn tc4 = new TableColumn(table, SWT.LEFT);
        TableColumn tc5 = new TableColumn(table, SWT.LEFT);
        TableColumn tc2 = new TableColumn(table, SWT.CENTER);
        TableColumn tc3 = new TableColumn(table, SWT.CENTER);
        // addSelectionListener
        tc1.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    sort(event, InternalJob.SORT_BY_ID);
                }
            });
        tc2.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    sort(event, InternalJob.SORT_BY_PRIORITY);
                }
            });
        tc3.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    sort(event, InternalJob.SORT_BY_NAME);
                }
            });
        tc4.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    sort(event, InternalJob.SORT_BY_STATE);
                }
            });
        tc5.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    sort(event, InternalJob.SORT_BY_OWNER);
                }
            });
        // setText
        tc1.setText(COLUMN_ID_TITLE);
        tc2.setText(COLUMN_PRIORITY_TITLE);
        tc3.setText(COLUMN_NAME_TITLE);
        tc4.setText(COLUMN_STATE_TITLE);
        tc5.setText(COLUMN_OWNER_TITLE);
        // setWidth
        tc1.setWidth(30);
        tc2.setWidth(70);
        tc3.setWidth(100);
        tc4.setWidth(100);
        tc5.setWidth(45);
        // setMoveable
        tc1.setMoveable(true);
        tc2.setMoveable(true);
        tc3.setMoveable(true);
        tc4.setMoveable(true);
        tc5.setMoveable(true);

        table.addListener(SWT.Selection,
            new Listener() {
                public void handleEvent(Event event) {
                    // get the jobId
                    JobId jobId = (JobId) event.item.getData();

                    // get the job by jobId
                    InternalJob job = JobsController.getLocalView()
                                                    .getJobById(jobId);

                    if (job == null) {
                        System.err.println(
                            "LE JOB EST NULL .......... jobId ==> " + jobId);
                        return;
                    }

                    // show its output
                    // TODO est-ce que je laisse ou pas ???
                    JobsOutputController.getInstance().showJobOutput(jobId);

                    // update its informations
                    JobInfo jobInfo = JobInfo.getInstance();
                    if (jobInfo != null) {
                        jobInfo.updateInfos(job);

                        // set Focus on job info
                        IWorkbench iworkbench = PlatformUI.getWorkbench();
                        IWorkbenchWindow currentWindow = iworkbench.getActiveWorkbenchWindow();
                        IWorkbenchPage page = currentWindow.getActivePage();
                        try {
                            IViewPart part = page.showView(JobInfo.ID);
                            part.setFocus();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // update its tasks informations
                    TaskView taskView = TaskView.getInstance();
                    if (taskView != null) {
                        taskView.fullUpdate(job);
                    }

                    ResultPreview resultPreview = ResultPreview.getInstance();
                    if (resultPreview != null) {
                        resultPreview.update(new SimpleTextPanel(
                                "No selected task"));
                    }

                    jobSelected(job);
                }
            });

        // register to the table manager
        TableManager.getInstance().add(table);
        return table;
    }

    /**
     * Create and return an item which will be added in the table. The item will
     * has its data set to the jobId of the job.
     *
     * @param job the job which represent the item
     * @return the new item
     */
    protected TableItem createItem(InternalJob job, int itemIndex) {
        TableColumn[] cols = table.getColumns();
        TableItem item = new TableItem(table, SWT.NONE);
        item.setData(job.getId());
        if (itemIndex == 0) {
            fillBackgroundColor(item, job.getState(), null);
        } else {
            fillBackgroundColor(item, job.getState(),
                table.getItem(itemIndex - 1).getBackground());
        }

        for (int i = 0; i < cols.length; i++) {
            String title = cols[i].getText();
            if (title.equals(COLUMN_STATE_TITLE)) {
                item.setText(i, job.getState().toString());
            } else if (title.equals(COLUMN_ID_TITLE)) {
                item.setText(i, job.getId().toString());
            } else if (title.equals(COLUMN_PRIORITY_TITLE)) {
                item.setText(i, job.getPriority().toString());
            } else if (title.equals(COLUMN_NAME_TITLE)) {
                item.setText(i, job.getName());
            } else if (title.equals(COLUMN_OWNER_TITLE)) {
                item.setText(i, job.getOwner());
            }
        }
        return item;
    }

    protected void stateUpdate(JobId aJobId) {
        if (!this.isDisposed()) {
            final JobId jobId = aJobId;

            getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        Table table = getTable();
                        TableItem[] items = table.getItems();
                        TableItem item = null;
                        int itemIndex = 0;
                        for (TableItem it : items) {
                            if (((JobId) (it.getData())).equals(jobId)) {
                                item = it;
                                break;
                            }
                            itemIndex++;
                        }

                        if (item == null) {
                            // TODO
                            // throw new IllegalArgumentException("the item which represent the job : " +
                            // jobId
                            // + " is unknown !");
                            //TODO System.err.println("the item which represent the job : " + jobId + " is unknown !");
                            return;
                        }

                        TableColumn[] cols = table.getColumns();
                        InternalJob job = JobsController.getLocalView()
                                                        .getJobById(jobId);
                        for (int i = 0; i < cols.length; i++) {
                            String title = cols[i].getText();
                            if ((title != null) &&
                                    (title.equals(COLUMN_STATE_TITLE))) {
                                if (itemIndex == 0) {
                                    fillBackgroundColor(item, job.getState(),
                                        null);
                                } else {
                                    fillBackgroundColor(item, job.getState(),
                                        items[itemIndex].getBackground());
                                }
                                item.setText(i, job.getState().toString());
                                break;
                            }
                        }
                    }
                });
        }
    }

    protected void priorityUpdate(JobId aJobId) {
        if (!this.isDisposed()) {
            final JobId jobId = aJobId;

            getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        Table table = getTable();
                        TableItem[] items = table.getItems();
                        TableItem item = null;
                        for (TableItem it : items)
                            if (((JobId) (it.getData())).equals(jobId)) {
                                item = it;
                                break;
                            }

                        if (item == null) {
                            //						TODO throw new IllegalArgumentException("the item which represent the job : " + jobId
                            //								+ " is unknown !");
                            return;
                        }

                        TableColumn[] cols = table.getColumns();
                        InternalJob job = JobsController.getLocalView()
                                                        .getJobById(jobId);
                        for (int i = 0; i < cols.length; i++) {
                            String title = cols[i].getText();
                            if ((title != null) &&
                                    (title.equals(COLUMN_PRIORITY_TITLE))) {
                                item.setText(i, job.getPriority().toString());
                                break;
                            }
                        }
                    }
                });
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * To increase the count include in the label
     *
     * @return the current value used in the label
     */
    public int increaseCount() {
        setCount(++count);
        return count;
    }

    /**
     * To decrease the count include in the label
     *
     * @return the current value used in the label
     */
    public int decreaseCount() {
        if (count == 0) {
            return 0;
        }
        setCount(--count);
        return count;
    }

    /**
     * To get the table
     *
     * @return the table
     */
    public Table getTable() {
        return table;
    }

    /**
     * To add a job in the table by its jobId
     *
     * @param jobId the jobid of the job which will be added in the table
     */
    public void addJob(JobId jobId) {
        increaseCount();
        addJobInTable(jobId, count - 1);
    }

    /**
     * To remove a job in the table by its jobId
     *
     * @param jobId the jobid of the job which will be removed in the table
     */
    public void removeJob(JobId jobId) {
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
                //TODO
                //				throw new IllegalArgumentException("jobId unknown : " + jobId);
                return;
            }
            final int i = tmp;
            getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        int j = table.getSelectionIndex();
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
                        table.remove(i);
                        // TODO je pense qu'il ne servent à rien
                        // table.redraw();
                        // table.update();
                        decreaseCount();
                    }
                });
        }
    }

    /**
     * To initialize the table at the beginning. This method set the count
     * (include in the label) to the jobs list size and refresh the table.
     */
    public void initTable() {
        count = getJobs().size();
        setCount(count);
        refreshTable();
    }

    // -------------------------------------------------------------------- //
    // ----------------------------- abstract ----------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * To obtain the jobs list
     *
     * @return jobs list
     */
    public abstract Vector<JobId> getJobs();

    /**
     * To sort jobs
     */
    public abstract void sortJobs();

    /**
     * Call when a job is selected in a table
     *
     * @param job the job selected
     */
    public abstract void jobSelected(InternalJob job);

    /**
     * To clear properly the composite. This method will be called on
     * disconnection only.
     */
    public abstract void clear();

    // -------------------------------------------------------------------- //
    // ------------------------ extends composite ------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.eclipse.swt.widgets.Control#setMenu(org.eclipse.swt.widgets.Menu)
     */
    @Override
    public void setMenu(Menu menu) {
        super.setMenu(menu);
        table.setMenu(menu);
        label.setMenu(menu);
    }

    /**
     * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        if (label != null) {
            label.setVisible(visible);
        }
        if (table != null) {
            table.setVisible(visible);
        }
    }
}
