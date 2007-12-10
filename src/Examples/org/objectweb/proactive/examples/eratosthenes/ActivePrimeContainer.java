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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.eratosthenes;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;


/**
 * @author Jonathan Streit
 *
 * An active container for PrimeNumber objects. The class implements the
 * PrimeNumber interface so that it can be assigned to the preceeding
 * numbers' next-field. When the container has reached its maximum size,
 * it demands creation of a new container.
 * The container is migratable.
 */
public class ActivePrimeContainer implements PrimeNumber, java.io.Serializable,
    Slowable, RunActive {
    private PrimeNumber first;
    private int size;
    private int maxSize;
    private boolean sleep;
    private boolean isPreviousSleeping;
    private ActivePrimeContainerCreator activePrimeContainerCreator;
    private PrimeOutputListener outputListener;
    private Slowable previous;

    /**
     * Constructor for ActivePrimeContainer.
     */
    public ActivePrimeContainer() {
    }

    /** @param creator the creator of new containers (when this one is full)
     *  @param listener the output listener used to print out new prime numbers
     *  @param maxSize the maximum size of this container before a new one is created
     *  @param n the first number to store in this container
     *  @param previous the container or source that sends requests to this container
     * */
    public ActivePrimeContainer(ActivePrimeContainerCreator creator,
        PrimeOutputListener listener, Integer maxSize, Long n, Slowable previous) {
        super();
        this.maxSize = maxSize.intValue();
        this.activePrimeContainerCreator = creator;
        this.outputListener = listener;
        this.first = newPrimeNumber(n.longValue());
        this.previous = previous;
    }

    /** Asks this container to sleep in order to reduce the number of requests sent. */
    public void sleep(boolean sleep) {
        this.sleep = sleep;
    }

    /** Serves requests and looks after the length of the request queue from time to time.
     * Demands slowing down from previous container if the queue gets to long. */
    public void runActivity(Body b) {
        Service service = new Service(b);
        while (b.isActive()) {
            if (sleep) { // sleep in order to reduce charge on next container
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                service.serveAll("sleep"); // serve all sleep requests: maybe we can wake up
            } else {
                for (int i = 0; i < 50; i++)
                    service.blockingServeOldest(); // serve requests
            }

            int queueSize = b.getRequestQueue().size(); // test request queue length
            if (!isPreviousSleeping && (queueSize > 200)) { // make previous sleep
                previous.sleep(true);
                isPreviousSleeping = true;
            } else if (isPreviousSleeping && (queueSize < 100)) { // make previous wake up
                previous.sleep(false);
                isPreviousSleeping = false;
            }
        }
    }

    /** Asks the first PrimeNumber object in the container to try this number */
    public void tryModulo(long n) {
        first.tryModulo(n);
    }

    /** Returns the value of the first prime number. */
    public long getValue() {
        return first.getValue();
    }

    /** Creates a new PrimeNumber in this container or, when maximum size
     * has been reached, demands creation of a new container. */
    public PrimeNumber newPrimeNumber(long n) {
        size++;
        if (size <= maxSize) {
            outputListener.newPrimeNumberFound(n);
            return new PrimeNumberImpl(this, n);
        } else {
            return activePrimeContainerCreator.newActivePrimeContainer(n,
                (ActivePrimeContainer) PAActiveObject.getStubOnThis());
        }
    }
}
