package org.ow2.proactive.scheduler.common.util.dsclient;

import org.ow2.proactive.scheduler.common.SchedulerEventListener;


/**
 * A scheduler Event Listener. In addition to Scheduler events, supports data transfer related events.
 * To be used with {@link SchedulerProxyUIWithDSupport}
 * 
 * @author esalagea
 *
 */
public interface ISchedulerEventListenerExtended extends SchedulerEventListener {

    //	public void pushDataFinished(String jobId, String pushLocation_URL);
    public void pullDataFinished(String jobId, String localFolderPath);

    public void pullDataFailed(String jobId, String remoteFolder_URL, Throwable t);

}
