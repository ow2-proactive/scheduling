package org.objectweb.proactive.extra.masterslave.interfaces.internal;


/**
 * @author fviale
 * An object implementing this interface expects to be notified when a Slave is missing
 */
public interface SlaveDeadListener {

    /**
     * Callback function called when a slave is missing
     * @param slave the missing slave
     */
    void isDead(Slave slave);
}
