/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.gui.wizards.flatJobWizard;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.factories.FlatJobFactory;
import org.ow2.proactive.scheduler.common.job.Job;


public class MainPage extends WizardPage implements ModifyListener {

    private Text jobNameText = null;
    private Text commandPathText = null;
    private Text sScriptPathText = null;
    private Text logOutputPathText = null;
    private Composite parent = null;

    public MainPage() {
        super("Commands file job submission");
        setTitle("Commands file job submission");
        setDescription("Submit a job from a file containing commands to launch");
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createAdvancedControls(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        this.parent = parent;

        // create the composite to hold the widgets
        Composite composite = new Composite(parent, SWT.NULL);

        final Shell shell = composite.getShell();
        // create the desired layout for this wizard page

        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        composite.setLayout(layout);

        // job name
        Label jobNameLabel = new Label(composite, SWT.NONE);
        jobNameLabel.setText("job name :");

        jobNameText = new Text(composite, SWT.BORDER | SWT.NONE);
        FormData jobTextFormData = new FormData();
        jobTextFormData.top = new FormAttachment(jobNameLabel, 0, SWT.BOTTOM);
        jobTextFormData.width = 160;
        jobNameText.setLayoutData(jobTextFormData);

        //commands file Path
        Label FilePathLabel = new Label(composite, SWT.NONE);
        FilePathLabel.setText("commands file :");
        FormData CommandFileLabelFormData = new FormData();
        CommandFileLabelFormData.top = new FormAttachment(jobNameText, 20, SWT.BOTTOM);
        FilePathLabel.setLayoutData(CommandFileLabelFormData);

        commandPathText = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        FormData CommandFileTextlFormData = new FormData();
        CommandFileTextlFormData.top = new FormAttachment(FilePathLabel, 0, SWT.BOTTOM);
        CommandFileTextlFormData.width = 450;
        commandPathText.setLayoutData(CommandFileTextlFormData);

        Button chooseButton = new Button(composite, SWT.NONE);
        chooseButton.setText("browse");
        FormData chooseFormData = new FormData();
        chooseFormData.left = new FormAttachment(commandPathText, 5, SWT.RIGHT);
        chooseFormData.top = new FormAttachment(FilePathLabel, 0, SWT.BOTTOM);
        chooseButton.setLayoutData(chooseFormData);

        chooseButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
                fileDialog.setFilterExtensions(new String[] { "*.cmd" });
                fileDialog.setText("Choose a file containing commands to launch");
                String fileName = fileDialog.open();
                commandPathText.setText(fileName);
            }
        });

        //selection script
        Label sScriptPathLabel = new Label(composite, SWT.NONE);
        sScriptPathLabel.setText("selection Script file :");
        FormData sScriptPathLabelFormData = new FormData();
        sScriptPathLabelFormData.top = new FormAttachment(commandPathText, 20, SWT.BOTTOM);
        sScriptPathLabel.setLayoutData(sScriptPathLabelFormData);

        sScriptPathText = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        FormData sScriptPathTextFormData = new FormData();
        sScriptPathTextFormData.top = new FormAttachment(sScriptPathLabel, 0, SWT.BOTTOM);
        sScriptPathTextFormData.width = 450;
        sScriptPathText.setLayoutData(sScriptPathTextFormData);

        Button choosesScriptButton = new Button(composite, SWT.NONE);
        choosesScriptButton.setText("browse");
        FormData choosesScriptButtonFormData = new FormData();
        choosesScriptButtonFormData.top = new FormAttachment(sScriptPathLabel, 0, SWT.BOTTOM);
        choosesScriptButtonFormData.left = new FormAttachment(sScriptPathText, 5, SWT.RIGHT);
        choosesScriptButton.setLayoutData(choosesScriptButtonFormData);

        //output log file
        Label logOutputPathLabel = new Label(composite, SWT.NONE);
        logOutputPathLabel.setText("path for log file (log STDOUT and STDERR) :");
        FormData logOutputLabelFormData = new FormData();
        logOutputLabelFormData.top = new FormAttachment(sScriptPathText, 20, SWT.BOTTOM);
        logOutputPathLabel.setLayoutData(logOutputLabelFormData);

        logOutputPathText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        FormData logOutputPathTextFormData = new FormData();
        logOutputPathTextFormData.top = new FormAttachment(logOutputPathLabel, 0, SWT.BOTTOM);
        logOutputPathTextFormData.width = 450;
        logOutputPathText.setLayoutData(logOutputPathTextFormData);

        choosesScriptButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
                fileDialog.setFilterExtensions(new String[] { "*.js" });
                fileDialog.setText("Choose a selection script file");
                String fileName = fileDialog.open();
                sScriptPathText.setText(fileName);
            }
        });

        //cancel on exception button
        Button cancelOnExceptionButton = new Button(composite, SWT.CHECK);
        cancelOnExceptionButton.setText("Cancel whole job if one of the commands fails");
        FormData cancelButtonFormData = new FormData();
        cancelButtonFormData.top = new FormAttachment(logOutputPathText, 20, SWT.BOTTOM);
        cancelOnExceptionButton.setLayoutData(cancelButtonFormData);

        //add listeners
        this.jobNameText.addModifyListener(this);
        this.commandPathText.addModifyListener(this);

        // set the composite as the control for this page
        setControl(composite);
    }

    public void modifyText(ModifyEvent e) {
        getWizard().getContainer().updateButtons();
    }

    @Override
    public boolean canFlipToNextPage() {
        setErrorMessage(this.findMostSevere());
        if ("".equals(jobNameText.getText()) || "".equals(commandPathText.getText())) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public IWizardPage getNextPage() {
        String selectionScriptPath = null;

        String commandFilePath = commandPathText.getText();
        String jobName = jobNameText.getText();
        String logFilePath = null;
        if (!"".equals(sScriptPathText.getText())) {
            selectionScriptPath = sScriptPathText.getText();
        }
        if (!"".equals(logOutputPathText.getText())) {
            logFilePath = logOutputPathText.getText();
        }

        Job j;
        try {
            j = FlatJobFactory.getFactory().createNativeJobFromCommandsFile(commandFilePath, jobName,
                    selectionScriptPath, logFilePath, jobName);
            ((FlatFileJobWizard) getWizard()).setCreatedJob(j);
            SummaryPage page = ((FlatFileJobWizard) getWizard()).getSummaryPage();
            ((FlatFileJobWizard) getWizard()).setCreatedJob(j);
            page.onEnterPage();
            getWizard().getContainer().updateButtons();
            return page;
        } catch (JobCreationException e) {
            MessageDialog.openError(parent.getShell(), "Couldn't create job", "Couldn't create job : \n\n" +
                e.getMessage());
            ((FlatFileJobWizard) getWizard()).setCreatedJob(null);
            getWizard().getContainer().updateButtons();
            return null;
        }
    }

    public String findMostSevere() {
        if ("".equals(jobNameText.getText())) {
            return "enter a job name";
        } else if ("".equals(commandPathText.getText())) {
            return "choose a path to a commands file";
        } else
            return null;
    }
}
