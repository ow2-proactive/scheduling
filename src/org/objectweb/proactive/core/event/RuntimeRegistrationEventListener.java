package org.objectweb.proactive.core.event;


/**
 * <p>
 * A class implementating this interface is listener of <code>RuntimeRegistrationEvent</code>.
 * </p>
 *
 * @see RuntimeRegistrationEvent
 * @author  ProActive Team
 * @version 1.0,  2002/08/06
 * @since   ProActive 0.9.4
 *
 */
public interface RuntimeRegistrationEventListener extends ProActiveListener {

    /**
     * Signals that a registration occured on the runtime encapsulated in the event
     * @param event the creation event that details the registration on the runtime
     */
    public void runtimeRegistered(RuntimeRegistrationEvent event);
}
