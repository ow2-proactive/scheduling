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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.gui.Internal;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;


/**
 * @author The ProActive Team
 */
public class SubmitJobAction extends SchedulerGUIAction {
    private Composite parent = null;
    private String lastDirectory = null;

    public SubmitJobAction(Composite parent) {
        this.parent = parent;
        this.setText("Submit an XML job file");
        this.setToolTipText("Submit job from an XML file containing a job description");
        this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_JOBSUBMIT));
        this.setEnabled(false);
    }

    @Override
    public void run() {
        FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.OPEN | SWT.MULTI);
        fileDialog.setFilterExtensions(new String[] { "*.xml" });
        if (lastDirectory != null) {
            fileDialog.setFilterPath(lastDirectory);
        }
        fileDialog.open();

        String[] filesNames = fileDialog.getFileNames();
        String directoryPath = fileDialog.getFilterPath();
        lastDirectory = directoryPath;

        //map of submitted jobs, for submission summary
        HashMap<JobId, String> submittedJobs = new HashMap<JobId, String>();
        //which creation or submission has failed
        HashMap<String, String> failedJobs = new HashMap<String, String>();

        String filePath = null;

        //create jobs
        try {
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
        } catch (Throwable t) {
            t.printStackTrace();
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

                final String pluginId = Activator.PLUGIN_ID;

                final MultiStatus ms = new MultiStatus(pluginId, 0,
                    "Creation or submission from some xml files has failed, see details.", null);

                for (Entry<JobId, String> entry : submittedJobs.entrySet()) {
                    String ErrorText = "file name : " + entry.getValue() + " submitted, job ID : " +
                        entry.getKey().toString();
                    ms.add(new Status(IStatus.INFO, pluginId, ErrorText));
                }
                for (Entry<String, String> entry : failedJobs.entrySet()) {
                    String ErrorText = "file name : " + entry.getKey() + "\n" + entry.getValue();
                    ms.add(new Status(IStatus.ERROR, pluginId, ErrorText));
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
