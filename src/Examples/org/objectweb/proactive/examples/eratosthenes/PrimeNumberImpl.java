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

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author Jonathan Streit
 *
 * One prime number. This object is owned by an ActivePrimeContainer.
 */
public class PrimeNumberImpl implements PrimeNumber, java.io.Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private long value;
    private PrimeNumber next;
    private ActivePrimeContainer container;

    /** This value serves only for debugging: tryModulo may not be called
     * with a number inferior to a prior call. */
    private long nextValue;

    /**
     * Constructor for PrimeNumberImpl.
     */
    public PrimeNumberImpl(ActivePrimeContainer container, long value) {
        this.container = container;
        this.value = value;
    }

    /**
     * Tries to divide value by n. Returns if this succeeds, calls
     * tryModulo on the next prime number if not. If there is no next
     * prime number, a new prime has been found and is created.
     * For debugging, a check is made on the order of the requests.
     * @see org.objectweb.proactive.examples.eratosthenes.PrimeNumber#tryModulo(long)
     */
    public void tryModulo(long n) {
        if ((n % value) != 0) {
            if (next == null) {
                nextValue = n;
                next = container.newPrimeNumber(n);
            } else {
                if (n <= nextValue) {
                    logger
                            .fatal("Requests arrived out of order. Should never occur when using FIFO serving.");
                    System.exit(2);
                }
                next.tryModulo(n);
            }
        }
    }

    /**
     * @see org.objectweb.proactive.examples.eratosthenes.PrimeNumber#getValue()
     */
    public long getValue() {
        return value;
    }
}
