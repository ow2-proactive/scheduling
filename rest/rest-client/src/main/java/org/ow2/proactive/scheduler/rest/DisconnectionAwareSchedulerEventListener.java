package org.ow2.proactive.scheduler.rest;

import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.exception.ConnectionException;


/**
 * extended scheduler event listener to notify the disconnection event
 * 
 * @author ActiveEon team
 *
 */
public interface DisconnectionAwareSchedulerEventListener extends SchedulerEventListener {

    /**
     * notify the socket disconnection
     * 
     * @throws ConnectionException throw an exception if the connection cannot be done
     */
    void notifyDisconnection();
}
