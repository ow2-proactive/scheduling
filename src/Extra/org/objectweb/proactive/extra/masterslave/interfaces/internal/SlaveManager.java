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

import java.util.Collection;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * A SlaveManager keeps a pool of Slaves in the Master/Slave API <br/>
 * A SlaveManager is connected to a SlaveConsumer (i.e. the Master in the M/S API) which needs slaves.<br/>
 * @author fviale
 *
 */
public interface SlaveManager {

    /**
     * Returns a slave to the slave manager
     * @param slave slave to be returned
     * @return acceptation of the request (asynchronous)
     */
    public BooleanWrapper freeSlave(Slave slave);

    /**
     * Returns a collection of slaves to the slave manager
     * @param Collection of slaves to be returned
     * @return acceptation of the request (asynchronous)
     */
    public BooleanWrapper freeSlaves(Collection<Slave> nodes);

    /**
     * Ask the Slave Manager to reserve all available Slaves
     * @param consumer the consumer that will be notified of slave availability
     * @return acceptation of the request (asynchronous)
     */
    BooleanWrapper reserveAllSlaves(SlaveConsumer consumer);

    /**
     * Ask the Slave Manager to reserve a number of Slaves
     * @param consumer the consumer that will be notified of slave availability
     * @param nbToReserve number of slaves to reserve
     * @return acceptation of the request (asynchronous)
     */
    BooleanWrapper reserveSlaves(SlaveConsumer consumer, int nbToReserve);
}
