package org.ow2.proactive.resourcemanager.gui.interfaces;

/**
 * @author The ProActive Team
 *
 */
public interface RMCoreEventListener {
    public void imKilledEvent();

    public void imShutDownEvent();

    public void imShuttingDownEvent();

    public void imStartedEvent();
}
