package org.objectweb.proactive.extra.masterslave.interfaces.internal;


/**
 * A simple interface for objects which are identifiable by a numeric id
 * @author fviale
 *
 */
public interface Identifiable {

    /**
     * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
     * get the id of the task
     * @return the id
     */
    long getId();
}
