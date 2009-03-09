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
package org.ow2.proactive.scheduler.gui.actions;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.gui.Activator;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;


/**
 * @author The ProActive Team
 */
public class SubmitJobAction extends SchedulerGUIAction {
    private Composite parent = null;

    public SubmitJobAction(Composite parent) {
        this.parent = parent;
        this.setText("Submit an XML job file");
        this.setToolTipText("Submit job from an XML file containing a job description");
        this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "icons/job_submit.gif"));
        this.setEnabled(false);
    }

    @Override
    public void run() {
        FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.OPEN | SWT.MULTI);
        fileDialog.setFilterExtensions(new String[] { "*.xml" });
        fileDialog.open();

        String[] filesNames = fileDialog.getFileNames();
        String directoryPath = fileDialog.getFilterPath();

        //map of submitted jobs, for submission summary
        HashMap<JobId, String> submittedJobs = new HashMap<JobId, String>();
        //which creation or submission has failed
        HashMap<String, String> failedJobs = new HashMap<String, String>();

        String filePath = null;

        //create jobs
        for (String fileName : filesNames) {
            filePath = directoryPath + File.separator + fileName;
            try {
                Job job = JobFactory.getFactory().createJob(filePath);
                JobId id = SchedulerProxy.getInstance().submit(job);
                submittedJobs.put(id, fileName);
            } catch (JobCreationException e) {
                failedJobs.put(fileName, "Job creation error : " + e.getMessage());
            } catch (SchedulerException e) {
                failedJobs.put(fileName, "Job submission error : " + e.getMessage());
            }
        }

        if (failedJobs.size() != 0) {
            //one error for one job to submit : display a simple dialog box
            if (filesNames.length == 1) {
                MessageDialog.openError(parent.getShell(), "Job submission error", failedJobs
                        .get(filesNames[0]));
            } else {
                //display a dialog box with details for each job
                String text = "Submission summary : \n\n" + submittedJobs.size() + " submitted. \n" +
                    failedJobs.size() + " failed to submit.";

                MultiStatus ms = new MultiStatus(Activator.getPluginId(), 0,
                    "Creation or submission from some xml files has failed, see details.", null);

                for (Entry<JobId, String> entry : submittedJobs.entrySet()) {
                    String ErrorText = "file name : " + entry.getValue() + " submitted, job ID : " +
                        entry.getKey().toString();
                    ms.add(new Status(IStatus.INFO, Activator.getPluginId(), ErrorText));
                }
                for (Entry<String, String> entry : failedJobs.entrySet()) {
                    String ErrorText = "file name : " + entry.getKey() + "\n" + entry.getValue();
                    ms.add(new Status(IStatus.ERROR, Activator.getPluginId(), ErrorText));
                }
                ErrorDialog.openError(parent.getShell(), "Job submission error", text, ms, IStatus.ERROR |
                    IStatus.INFO);
            }
        }
    }

    @Override
    public void setEnabled(boolean connected, SchedulerStatus schedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        if (connected && (schedulerStatus != SchedulerStatus.KILLED) &&
            (schedulerStatus != SchedulerStatus.SHUTTING_DOWN) &&
            (schedulerStatus != SchedulerStatus.STOPPED))
            setEnabled(true);
        else
            setEnabled(false);
    }
}
