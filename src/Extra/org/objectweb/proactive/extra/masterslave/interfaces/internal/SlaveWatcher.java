/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.masterslave.interfaces.internal;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * A SlaveWatcher is responsible of watching slaves'activity<br/>
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
     * @return true if the object terminated successfully
     */
    public BooleanWrapper terminate();
}
