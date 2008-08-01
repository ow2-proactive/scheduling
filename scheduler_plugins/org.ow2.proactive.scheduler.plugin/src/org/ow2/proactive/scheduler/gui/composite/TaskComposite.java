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
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.scheduler.Tools;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.util.ResultPreviewTool.SimpleTextPanel;
import org.ow2.proactive.scheduler.gui.Colors;
import org.ow2.proactive.scheduler.gui.data.JobsController;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;
import org.ow2.proactive.scheduler.gui.views.ResultPreview;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class TaskComposite extends Composite {

    /** the unique id and the title for the column "Id" */
    public static final String COLUMN_ID_TITLE = "Id";

    /** the unique id and the title for the column "State" */
    public static final String COLUMN_STATUS_TITLE = "State";

    /** the unique id and the title for the column "Name" */
    public static final String COLUMN_NAME_TITLE = "Name";

    /** the unique id and the title for the column "Description" */
    public static final String COLUMN_DESCRIPTION_TITLE = "Description";

    //    /** the unique id and the title for the column "Run time limit" */
    //    public static final String COLUMN_RUN_TIME_LIMIT_TITLE = "Run time limit";

    /** the unique id and the title for the column "Re-runnable" */
    public static final String COLUMN_RERUN_TITLE = "Re-run";

    /** the unique id and the title for the column "Start time" */
    public static final String COLUMN_START_TIME_TITLE = "Start time";

    /** the unique id and the title for the column "Finished time" */
    public static final String COLUMN_FINISHED_TIME_TITLE = "Finished time";

    /** the unique id and the title for the column "host name" */
    public static final String COLUMN_HOST_NAME_TITLE = "Host name";

    /** the canceled tasks background color */
    public static final Color TASKS_CANCELED_BACKGROUND_COLOR = Colors.ORANGE;

    /** the failed tasks background color */
    public static final Color TASKS_FAILED_BACKGROUND_COLOR = Colors.RED;

    /** the aborted tasks background color */
    public static final Color TASKS_ABORTED_BACKGROUND_COLOR = Colors.BROWN;

    /**
     * the background color of tasks that couldn't be started due to dependencies failure
     */
    public static final Color TASKS_NOT_STARTED_BACKGROUND_COLOR = Colors.DEEP_SKY_BLUE;
    private List<InternalTask> tasks = null;
    private Label label = null;
    private Table table = null;
    private int order = InternalTask.ASC_ORDER;
    private int lastSorting = InternalTask.SORT_BY_ID;

    /**
     * This is the default constructor.
     * 
     * @param parent
     */
    public TaskComposite(Composite parent) {
        super(parent, SWT.NONE);
        this.setLayout(new GridLayout());
        this.label = createLabel(parent);
        this.table = createTable(parent);
    }

    private Label createLabel(Composite parent) {
        Label label = new Label(this, SWT.CENTER);
        label.setText("No job selected");
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setForeground(Colors.RED);
        return label;
    }

    private Table createTable(Composite parent) {
        //The table must be create with the SWT.SINGLE option !
        table = new Table(this, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // creating TableColumn
        TableColumn tc1 = new TableColumn(table, SWT.LEFT);
        TableColumn tc2 = new TableColumn(table, SWT.LEFT);
        TableColumn tc3 = new TableColumn(table, SWT.LEFT);
        TableColumn tc4 = new TableColumn(table, SWT.LEFT);
        TableColumn tc5 = new TableColumn(table, SWT.LEFT);
        TableColumn tc6 = new TableColumn(table, SWT.LEFT);
        TableColumn tc7 = new TableColumn(table, SWT.LEFT);

        //        TableColumn tc8 = new TableColumn(table, SWT.LEFT);
        TableColumn tc9 = new TableColumn(table, SWT.LEFT);
        // addSelectionListener
        tc1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                sort(event, InternalTask.SORT_BY_ID);
            }
        });
        tc2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                sort(event, InternalTask.SORT_BY_STATUS);
            }
        });
        tc3.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                sort(event, InternalTask.SORT_BY_NAME);
            }
        });
        tc4.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                sort(event, InternalTask.SORT_BY_HOST_NAME);
            }
        });
        tc5.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                sort(event, InternalTask.SORT_BY_STARTED_TIME);
            }
        });
        tc6.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                sort(event, InternalTask.SORT_BY_FINISHED_TIME);
            }
        });
        tc7.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                sort(event, InternalTask.SORT_BY_RERUNNABLE);
            }
        });
        //        tc8.addSelectionListener(new SelectionAdapter() {
        //                
        //                public void widgetSelected(SelectionEvent event) {
        //                    sort(event, InternalTask.SORT_BY_RUN_TIME_LIMIT);
        //                }
        //            });
        tc9.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                sort(event, InternalTask.SORT_BY_DESCRIPTION);
            }
        });
        // setText
        tc1.setText(COLUMN_ID_TITLE);
        tc2.setText(COLUMN_STATUS_TITLE);
        tc3.setText(COLUMN_NAME_TITLE);
        tc4.setText(COLUMN_HOST_NAME_TITLE);
        tc5.setText(COLUMN_START_TIME_TITLE);
        tc6.setText(COLUMN_FINISHED_TIME_TITLE);
        tc7.setText(COLUMN_RERUN_TITLE);
        //        tc8.setText(COLUMN_RUN_TIME_LIMIT_TITLE);
        tc9.setText(COLUMN_DESCRIPTION_TITLE);
        // setWidth
        tc1.setWidth(50);
        tc2.setWidth(100);
        tc3.setWidth(100);
        tc4.setWidth(130);
        tc5.setWidth(130);
        tc6.setWidth(130);
        tc7.setWidth(50);
        //        tc8.setWidth(130);
        tc9.setWidth(200);
        // setMoveable
        tc1.setMoveable(true);
        tc2.setMoveable(true);
        tc3.setMoveable(true);
        tc4.setMoveable(true);
        tc5.setMoveable(true);
        tc6.setMoveable(true);
        tc7.setMoveable(true);
        //        tc8.setMoveable(true);
        tc9.setMoveable(true);

        table.addMouseListener(new MouseListener() {
            private boolean doubleClick = false;

            public void mouseDoubleClick(MouseEvent e) {
                doubleClick = true;
                TableItem[] items = ((Table) e.getSource()).getSelection();
                if (items.length > 0)
                    onClick((TaskId) items[0].getData(), doubleClick);
            }

            public void mouseDown(MouseEvent e) {
                doubleClick = false;
            }

            public void mouseUp(MouseEvent e) {
                if (!doubleClick) {
                    TableItem[] items = ((Table) e.getSource()).getSelection();
                    if (items.length > 0)
                        onClick((TaskId) items[0].getData(), doubleClick);
                }
            }

        });

        return table;
    }

    private void onClick(TaskId taskId, boolean doubleClick) {
        ResultPreview resultPreview = ResultPreview.getInstance();
        if (resultPreview != null) {
            InternalJob job = JobsController.getLocalView().getJobById(taskId.getJobId());

            // test job owner
            if (SchedulerProxy.getInstance().isItHisJob(job.getOwner())) {

                // set Focus on task result
                IWorkbench iworkbench = PlatformUI.getWorkbench();
                IWorkbenchWindow currentWindow = iworkbench.getActiveWorkbenchWindow();
                IWorkbenchPage page = currentWindow.getActivePage();
                try {
                    IViewPart part = page.showView(ResultPreview.ID);
                    part.setFocus();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                InternalTask task = job.getHMTasks().get(taskId);

                // update its tasks informations if task is finished
                if (task.getStatus() == TaskState.FINISHED) {
                    TaskResult tr = getTaskResult(job.getId(), taskId);
                    if (tr != null) {
                        if (doubleClick) {
                            try {
                                JPanel graphPrev = tr.getGraphicalDescription();
                                resultPreview.update(graphPrev);
                            } catch (Throwable e) {
                                // NoClassDefFound error if job classpath is not correct
                                resultPreview.update(new SimpleTextPanel(
                                    "Graphical preview cannot be displayed because " + e.getMessage()));
                            }
                        } else {
                            try {
                                String textPrev = tr.getTextualDescription();
                                resultPreview.update(new SimpleTextPanel(textPrev));
                            } catch (Throwable e) {
                                // NoClassDefFound error if job classpath is not correct
                                resultPreview.update(new SimpleTextPanel(
                                    "Textual preview cannot be displayed because " + e.getMessage()));
                            }
                        }
                    } else {
                        throw new RuntimeException("Task " + taskId + " is finished but result is null");
                    }
                } else { //Available 
                    resultPreview.update(new SimpleTextPanel("No preview is available because the task is " +
                        task.getStatus() + "..."));
                }
            } else {
                resultPreview.update(new SimpleTextPanel("You do not have sufficient rights !"));
            }
        }
    }

    // TODO TMP MKRIS
    private static Hashtable<TaskId, TaskResult> cachedTaskResult = new Hashtable<TaskId, TaskResult>();

    public static TaskResult getTaskResult(JobId jid, TaskId tid) {
        // TODO : NO ACCESS TO SCHED HERE ...
        // get result from scheduler
        // je viens de faire un copier coller de ce code de JobsController...
        TaskResult tr = cachedTaskResult.get(tid);
        if (tr == null) {
            tr = SchedulerProxy.getInstance().getTaskResult(jid, tid.getReadableName());
            cachedTaskResult.put(tid, tr);
        }
        return tr;
    }

    private static void deleteTaskResultCache() {
        cachedTaskResult.clear();
    }

    // END TMP MKRIS

    private void sort(SelectionEvent event, int field) {
        if (tasks != null) {
            if (lastSorting == field) {
                // if the new sort is the same as the last sort, invert order.
                order = (order == InternalTask.DESC_ORDER) ? InternalTask.ASC_ORDER : InternalTask.DESC_ORDER;
                InternalTask.setSortingOrder(order);
            }
            InternalTask.setSortingBy(field);
            lastSorting = field;

            sort();

            table.setSortColumn((TableColumn) event.widget);
            table.setSortDirection((order == InternalTask.DESC_ORDER) ? SWT.DOWN : SWT.UP);
        }
    }

    private void sort() {
        Collections.sort(tasks);
        refreshTable();
    }

    private void refreshTable() {
        if (!isDisposed()) {
            // Turn off drawing to avoid flicker
            table.setRedraw(false);

            // We remove all the table entries
            table.removeAll();

            int i = 0;

            // then add the entries
            for (InternalTask td : tasks)
                createItem(td, i++);

            // Turn drawing back on
            table.setRedraw(true);

            // delete taskresult cache
            deleteTaskResultCache();

        }
    }

    private void createItem(InternalTask internalTask, int itemIndex) {
        if (!table.isDisposed()) {
            TableItem item = new TableItem(table, SWT.NONE);
            // To have a unique identifier for this TableItem
            item.setData(internalTask.getId());
            if (itemIndex == 0) {
                fillItem(item, internalTask, null);
            } else {
                fillItem(item, internalTask, table.getItem(itemIndex - 1).getBackground());
            }
        }
    }

    private void fillItem(TableItem item, InternalTask internalTask, Color col) {
        if (!table.isDisposed()) {
            boolean setFont = false;
            switch (internalTask.getStatus()) {
                case ABORTED:
                    setFont = true;
                    item.setForeground(TASKS_ABORTED_BACKGROUND_COLOR);
                    break;
                case CANCELED:
                    setFont = true;
                    item.setForeground(TASKS_CANCELED_BACKGROUND_COLOR);
                    break;
                case FAILED:
                    setFont = true;
                    item.setForeground(TASKS_FAILED_BACKGROUND_COLOR);
                    break;
                case NOT_STARTED:
                    setFont = true;
                    item.setForeground(TASKS_NOT_STARTED_BACKGROUND_COLOR);
                    break;
                case FINISHED:
                case PAUSED:
                case PENDING:
                case RUNNING:
                case SUBMITTED:
            }

            if (!setFont && ((internalTask.getRerunnable() - internalTask.getRerunnableLeft()) > 0)) {
                setFont = true;
            }
            if (setFont) {
                Font font = item.getFont();
                item.setFont(new Font(font.getDevice(), font.getFontData()[0].getName(),
                    font.getFontData()[0].getHeight(), font.getFontData()[0].getStyle() | SWT.BOLD));
            }

            TableColumn[] cols = table.getColumns();

            // I'm must fill item by this way, because all columns are movable
            // !
            // So i don't know if the column "Id" is at the first or the "nth"
            // position
            for (int i = 0; i < cols.length; i++) {
                String title = cols[i].getText();
                if (title.equals(COLUMN_ID_TITLE)) {
                    item.setText(i, "" + internalTask.getId().hashCode());
                } else if (title.equals(COLUMN_STATUS_TITLE)) {
                    item.setText(i, internalTask.getStatus().toString());
                } else if (title.equals(COLUMN_NAME_TITLE)) {
                    item.setText(i, internalTask.getName());
                } else if (title.equals(COLUMN_DESCRIPTION_TITLE)) {
                    item.setText(i, (internalTask.getDescription() == null) ? "no description available"
                            : internalTask.getDescription());
                } else if (title.equals(COLUMN_START_TIME_TITLE)) {
                    item.setText(i, Tools.getFormattedDate(internalTask.getStartTime()));
                } else if (title.equals(COLUMN_FINISHED_TIME_TITLE)) {
                    item.setText(i, Tools.getFormattedDate(internalTask.getFinishedTime()));
                } else if (title.equals(COLUMN_RERUN_TITLE)) {
                    item.setText(i, (internalTask.getRerunnable() - internalTask.getRerunnableLeft()) + "/" +
                        internalTask.getRerunnable());
                    //                } else if (title.equals(COLUMN_RUN_TIME_LIMIT_TITLE)) {
                    //                    item.setText(i,
                    //                        Tools.getFormattedDate(internalTask.getRunTimeLimit()));
                } else if (title.equals(COLUMN_HOST_NAME_TITLE)) {
                    String hostName = internalTask.getExecutionHostName();
                    if (hostName == null) {
                        item.setText(i, "n/a");
                    } else {
                        item.setText(i, hostName);
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * This method "clear" the view by removing all item in the table and set the label to
     * "No job selected"
     */
    public void clear() {
        table.removeAll();
        label.setText("No selected job");
    }

    /**
     * This method remove all item of the table and fill it with the tasks vector. The label is also
     * updated.
     * 
     * @param jobId the jobId, just for the label.
     * 
     * @param tasks
     */
    public void setTasks(JobId jobId, ArrayList<InternalTask> tasks) {
        this.tasks = tasks;
        int tmp = tasks.size();

        if (!label.isDisposed()) {
            label.setText("Job " + jobId + " has " + tmp + ((tmp == 1) ? " task" : " tasks"));
        }
        refreshTable();
    }

    /**
     * This method remove all item of the table and fill it with the tasks vector. The label is also
     * updated.
     * 
     * @param numberOfJobs
     * 
     * @param tasks
     */
    public void setTasks(int numberOfJobs, ArrayList<InternalTask> tasks) {
        this.tasks = tasks;
        int tmp = tasks.size();

        if (!label.isDisposed()) {
            label.setText(numberOfJobs + " jobs selected / " + tmp + " tasks selected");
        }
        refreshTable();
    }

    /**
     * This method allow to replace only one line on the task table. This method identify the "good"
     * item with the taskId. The internalTask is use to fill item.
     * 
     * @param taskId the taskId which must be updated
     * 
     * @param internalTask all informations for fill item
     */
    public void changeLine(TaskId taskId, InternalTask internalTask) {
        if (!table.isDisposed()) {
            TableItem[] items = table.getItems();
            int itemIndex = 0;
            for (TableItem item : items) {
                if (((TaskId) item.getData()).equals(taskId)) {
                    if (itemIndex == 0) {
                        fillItem(item, internalTask, null);
                    } else {
                        fillItem(item, internalTask, items[itemIndex - 1].getBackground());
                    }
                    break;
                }
                itemIndex++;
            }
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------------ extends composite ------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.eclipse.swt.widgets.Widget#isDisposed()
     */
    @Override
    public boolean isDisposed() {
        return super.isDisposed() || ((table != null) && (table.isDisposed())) ||
            ((label != null) && (label.isDisposed()));
    }

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
        super.setVisible(visible);
        if (label != null) {
            label.setVisible(visible);
        }
        if (table != null) {
            table.setVisible(visible);
        }
    }

    public TaskId getIdOfSelectedTask() {
        TableItem[] items = table.getSelection();
        if (items.length == 0) {
            return null;
        }
        return (TaskId) items[0].getData();
    }
}
