package org.ow2.proactive.scheduler.rest;

import org.ow2.proactive.scheduler.common.SchedulerEventListener;


/**
 * extended scheduler event listener to notify the disconnection event
 * 
 * @author ActiveEon team
 *
 */
public interface DisconnectionAwareSchedulerEventListener extends SchedulerEventListener {

    /**
     * notify the socket disconnection
     */
    void notifyDisconnection();
}