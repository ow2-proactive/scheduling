package org.objectweb.proactive.extensions.scheduler.gui.data;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.scheduler.common.job.JobId;
import org.objectweb.proactive.extensions.scheduler.common.job.JobState;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerState;
import org.objectweb.proactive.extensions.scheduler.gui.actions.SchedulerGUIAction;
import org.objectweb.proactive.extensions.scheduler.job.InternalJob;


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

        JobId jobId = TableManager.getInstance().getLastJobIdOfLastSelectedItem();

        if (jobId != null) {
            InternalJob job = JobsController.getLocalView().getJobById(jobId);
            if (job != null) {
                jobSelected = true;
                owner = SchedulerProxy.getInstance().isItHisJob(job.getOwner());
                jobInFinishQueue = (job.getState() == JobState.CANCELLED) ||
                    (job.getState() == JobState.FAILED) || (job.getState() == JobState.FINISHED);
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
