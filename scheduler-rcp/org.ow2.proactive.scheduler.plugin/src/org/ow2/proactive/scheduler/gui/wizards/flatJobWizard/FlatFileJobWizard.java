/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;


public class FlatFileJobWizard extends Wizard implements IWorkbenchWizard {

    private MainPage mainPage;
    private SummaryPage summaryPage;
    private Job createdJob = null;

    private boolean canFinish = false;

    public FlatFileJobWizard() {
        super();
        setWindowTitle("Commands file job submission");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        if (createdJob != null) {
            try {
                // SUBMIT JOB
                SchedulerProxy.getInstance().submit(createdJob);
                return true;
            } catch (SchedulerException e) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(), "Couldn't submit job",
                        "Couldn't submit job due to : " + e.getCause());
                return false;
            }
        } else {
            return false;
        }

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    @Override
    public void addPages() {
        mainPage = new MainPage();
        addPage(mainPage);
        summaryPage = new SummaryPage();
        addPage(summaryPage);
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    @Override
    public boolean canFinish() {
        return canFinish;
    }

    public MainPage getMainPage() {
        return mainPage;
    }

    public SummaryPage getSummaryPage() {
        return summaryPage;
    }

    public Job getCreatedJob() {
        return createdJob;
    }

    public void setCreatedJob(Job createdJob) {
        this.createdJob = createdJob;
        if (createdJob == null)
            setCanFinish(false);
        else
            setCanFinish(true);
    }

    public void setCanFinish(boolean canFinish) {
        this.canFinish = canFinish;
    }

}
