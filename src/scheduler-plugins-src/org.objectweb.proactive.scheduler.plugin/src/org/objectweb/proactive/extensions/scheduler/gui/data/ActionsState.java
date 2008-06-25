package org.objectweb.proactive.extensions.scheduler.gui.data;

import org.objectweb.proactive.extensions.scheduler.common.job.JobState;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerState;


/**
 * 
 *
 * @author The ProActive Team
 */
public class ActionsState {

    private SchedulerState schedulerState;
    private JobState jobState;
    private boolean admin;
    private boolean connected;
    private boolean jobInFinishQueue;
    private boolean jobSelected;
    private boolean owner;
}
