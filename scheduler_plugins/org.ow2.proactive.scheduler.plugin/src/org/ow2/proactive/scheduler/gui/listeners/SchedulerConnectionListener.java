package org.ow2.proactive.scheduler.gui.listeners;

/**
 * This in the interface for a listener that listens for the state of a connection to the scheduler
 * @author esalagea
 *
 */
public interface SchedulerConnectionListener {

    public void connectionCreatedEvent(String schedulerUrl, String user, String password);

    public void connectionLostEvent();

}
