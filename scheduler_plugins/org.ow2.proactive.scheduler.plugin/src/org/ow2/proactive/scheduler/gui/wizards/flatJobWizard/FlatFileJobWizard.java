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
    public void addPages() {
        mainPage = new MainPage();
        addPage(mainPage);
        summaryPage = new SummaryPage();
        addPage(summaryPage);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

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
