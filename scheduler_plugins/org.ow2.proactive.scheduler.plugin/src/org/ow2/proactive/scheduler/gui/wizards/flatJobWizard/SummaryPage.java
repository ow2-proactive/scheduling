package org.ow2.proactive.scheduler.gui.wizards.flatJobWizard;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.Task;


public class SummaryPage extends WizardPage {

    private Table table = null;
    TableColumn taskColumn;
    TableColumn commandColumn;
    private Composite composite;
    TaskFlowJob j;

    protected SummaryPage() {
        super("Commands file job submission");
        setTitle("Commands file job submission");
        setDescription("Job Summary");
    }

    public void createControl(Composite parent) {
        // create the composite to hold the widgets
        composite = new Composite(parent, SWT.NULL);

        // create the desired layout for this wizard page
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        composite.setLayout(layout);

        // job name
        Label jobNameLabel = new Label(composite, SWT.NONE);
        jobNameLabel.setText("Commands to launch :");

        table = new Table(composite, SWT.BORDER | SWT.NONE);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        taskColumn = new TableColumn(table, SWT.LEFT);
        taskColumn.setAlignment(SWT.LEFT);
        taskColumn.setText("name");
        table.setSortColumn(taskColumn);
        table.setSortDirection(SWT.DOWN);

        commandColumn = new TableColumn(table, SWT.LEFT);
        commandColumn.setResizable(true);
        commandColumn.setAlignment(SWT.LEFT);
        commandColumn.setText("command");

        FormData tableFormData = new FormData();
        tableFormData.top = new FormAttachment(jobNameLabel, 5, SWT.BOTTOM);
        tableFormData.width = 550;
        tableFormData.height = 350;
        table.setLayoutData(tableFormData);

        setControl(composite);
    }

    public void onEnterPage() {
        j = (TaskFlowJob) ((FlatFileJobWizard) getWizard()).getCreatedJob();
        setDescription("Job Summary : " + j.getTasks().size() + " tasks.");

        //refill the table
        table.removeAll();
        table.setRedraw(false);

        ArrayList<Task> list = new ArrayList<Task>();
        for (Task t : j.getTasks()) {
            list.add(t);
        }
        Collections.sort(list, new TaskComparator());

        for (Task t : list) {
            String[] commandToDisplay = ((NativeTask) t).getCommandLine();
            String taskName = ((NativeTask) t).getName();
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { taskName, cmdArrayToString(commandToDisplay) });

        }
        taskColumn.pack();
        taskColumn.setWidth(taskColumn.getWidth() + 20);
        commandColumn.pack();
        table.setRedraw(true);
    }

    private String cmdArrayToString(String[] sArray) {
        String toReturn = "";
        for (String s : sArray) {
            toReturn += (s + " ");
        }
        return toReturn;
    }
}
