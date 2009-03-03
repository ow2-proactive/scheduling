package org.ow2.proactive.scheduler.gui.data;

import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.job.JobStatus;


/**
 * 
 *
 * @author The ProActive Team
 */
public class ActionsState {

    private SchedulerStatus schedulerStatus;
    private JobStatus jobStatus;
    private boolean admin;
    private boolean connected;
    private boolean jobInFinishQueue;
    private boolean jobSelected;
    private boolean owner;
}
