package org.objectweb.proactive.extra.masterslave.interfaces.internal;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * A SlaveWatcher is responsible of watching slaves'activity
 * @author fviale
 */
public interface SlaveWatcher {

    /**
     * adds a slave to be watched
     * @param slave
     */
    public void addSlaveToWatch(Slave slave);

    /**
     * stops watching a slave
     * @param slave
     */
    public void removeSlaveToWatch(Slave slave);

    /**
     * terminates the watcher's activity
     * @return
     */
    public BooleanWrapper terminate();
}
