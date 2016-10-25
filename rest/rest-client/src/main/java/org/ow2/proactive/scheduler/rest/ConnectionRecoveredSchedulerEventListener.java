package org.ow2.proactive.scheduler.rest;

import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.exception.ConnectionException;


/**
 * extended scheduler event listener to have a dedicated method trying to recover the socket
 * connection
 * 
 * @author ActiveEon team
 *
 */
public interface ConnectionRecoveredSchedulerEventListener extends SchedulerEventListener {

    /**
     * recover the socket connection
     * 
     * @throws ConnectionException throw an exception if the connection cannot be done
     */
    void recoverConnection() throws ConnectionException;
}
