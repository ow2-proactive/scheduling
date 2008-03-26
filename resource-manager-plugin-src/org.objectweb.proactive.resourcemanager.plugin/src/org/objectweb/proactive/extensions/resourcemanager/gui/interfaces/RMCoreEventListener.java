package org.objectweb.proactive.extensions.resourcemanager.gui.interfaces;

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
