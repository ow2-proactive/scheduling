package org.ow2.proactive.scheduler.gui.data;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerState;
import org.ow2.proactive.scheduler.gui.actions.SchedulerGUIAction;
import org.ow2.proactive.scheduler.job.InternalJob;


/**
 * 
 *
 * @author The ProActive Team
 */
public class ActionsManager {

    private static ActionsManager instance = null;
    private List<SchedulerGUIAction> actions = null;
    private SchedulerState schedulerState = null;
    private boolean connected = false;

    private ActionsManager() {
        actions = new ArrayList<SchedulerGUIAction>();
        schedulerState = SchedulerState.KILLED;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setSchedulerState(SchedulerState schedulerState) {
        this.schedulerState = schedulerState;
    }

    public boolean addAction(SchedulerGUIAction action) {
        return this.actions.add(action);
    }

    public void update() {
        boolean jobSelected = false;
        boolean owner = false;
        boolean jobInFinishQueue = false;

        if (connected) {
            List<JobId> jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();
            if (jobsId.size() > 0) {
                List<InternalJob> jobs = JobsController.getLocalView().getJobsByIds(jobsId);
                if (jobs.size() > 0) {
                    InternalJob job = jobs.get(0);
                    jobSelected = true;
                    jobInFinishQueue = (job.getState() == JobState.CANCELED) ||
                        (job.getState() == JobState.FAILED) || (job.getState() == JobState.FINISHED) ||
                        (job.getState() == JobState.KILLED);
                    owner = SchedulerProxy.getInstance().isItHisJob(job.getOwner());
                    for (int i = 1; owner && (i < jobs.size()); i++) {
                        owner &= SchedulerProxy.getInstance().isItHisJob(jobs.get(i).getOwner());
                    }
                }
            }
        }

        for (SchedulerGUIAction action : actions)
            action.setEnabled(connected, schedulerState, SchedulerProxy.getInstance().isAnAdmin(),
                    jobSelected, owner, jobInFinishQueue);
    }

    public static ActionsManager getInstance() {
        if (instance == null)
            instance = new ActionsManager();
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }
}
