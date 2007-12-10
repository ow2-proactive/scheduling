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
package org.objectweb.proactive.extensions.scheduler.gui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.objectweb.proactive.extensions.scheduler.common.job.Job;
import org.objectweb.proactive.extensions.scheduler.common.job.JobFactory;
import org.objectweb.proactive.extensions.scheduler.gui.data.SchedulerProxy;


/**
 * @author FRADJ Johann
 */
public class SubmitJobAction extends Action {
    public static final boolean ENABLED_AT_CONSTRUCTION = false;
    private static SubmitJobAction instance = null;
    private Composite parent = null;

    private SubmitJobAction(Composite parent) {
        this.parent = parent;
        this.setText("Submit a job");
        this.setToolTipText("Submit a job to the scheduler");
        this.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "icons/job_submit.gif"));
        this.setEnabled(ENABLED_AT_CONSTRUCTION);
    }

    @Override
    public void run() {
        FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.OPEN);
        fileDialog.setFilterExtensions(new String[] { "*.xml" });
        String fileName = fileDialog.open();

        if (fileName != null) {
            try {
                // CREATE JOB
                Job job = JobFactory.getFactory().createJob(fileName);
                // SUBMIT JOB
                SchedulerProxy.getInstance().submit(job);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static SubmitJobAction newInstance(Composite parent) {
        instance = new SubmitJobAction(parent);
        return instance;
    }

    public static SubmitJobAction getInstance() {
        return instance;
    }
}
