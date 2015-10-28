package org.ow2.proactive.scheduler.smartproxy.common;

import org.ow2.proactive.scheduler.common.SchedulerEventListener;


/**
 * A scheduler Event Listener. In addition to Scheduler events, supports data transfer related events.
 * To be used with {@link AbstractSmartProxy}.
 *
 * @author The ProActive Team
 */
public interface SchedulerEventListenerExtended extends SchedulerEventListener {

    //	public void pushDataFinished(String jobId, String pushLocation_URL);
    void pullDataFinished(String jobId, String taskName, String localFolderPath);

    void pullDataFailed(String jobId, String taskName, String remoteFolder_URL, Throwable t);

}
