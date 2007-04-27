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
package org.objectweb.proactive.extra.masterslave.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveConsumer;


/**
 * Utility class used by the slave manager <br/>
 * This object holds a set of consumers which expect a certain number of slaves each <br/>
 * The slaves will be distributed cyclically to the consumers <br/>
 * @author fviale
 *
 */
public class ConsumerQueue implements Serializable {
    private HashMap<SlaveConsumer, Integer> consumersMap = new HashMap<SlaveConsumer, Integer>();
    private ArrayList<SlaveConsumer> consumersList = new ArrayList<SlaveConsumer>();
    int currentConsumer = 0;

    public ConsumerQueue() {
    }

    /**
     * Adds a consumer to the queue, this consumer will reserve the given number of slaves
     * @param consumer consumer that will be notified of slave availablity
     * @param nbReservations number of slaves expected
     */
    public void addConsumer(SlaveConsumer consumer, int nbReservations) {
        if (consumersMap.containsKey(consumer)) {
            int nbResOld = consumersMap.get(consumer);
            consumersMap.put(consumer, nbResOld + nbReservations);
        } else {
            consumersMap.put(consumer, nbReservations);
            consumersList.add(consumer);
        }
    }

    /**
     * Tells if this queue has at least one consumer
     * @return
     */
    public boolean hasConsumers() {
        return consumersList.size() > 0;
    }

    /**
     * Gets the next consumer to which a new slave will be given
     * @return the next consumer
     * @throws IllegalStateException
     */
    public SlaveConsumer getNext() throws IllegalStateException {
        if (!hasConsumers()) {
            throw new IllegalStateException("no consumers");
        }
        SlaveConsumer consumer = consumersList.get(currentConsumer);
        int counter = consumersMap.get(consumer);
        counter--;
        if (counter > 0) {
            consumersMap.put(consumer, counter);
        } else {
            consumersMap.remove(consumer);
            consumersList.remove(currentConsumer);
        }
        currentConsumer = (currentConsumer + 1) % consumersList.size();
        return consumer;
    }
}
