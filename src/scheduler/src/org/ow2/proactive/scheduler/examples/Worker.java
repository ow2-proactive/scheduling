/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.examples;

import java.util.ArrayList;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * Prime worker.
 *
 * @author The ProActive Team
 * @since ProActive 3.9
 *
 */
public class Worker implements java.io.Serializable {
    // primeNumbers already known by the worker
    private ArrayList<Integer> primeNumbers = new ArrayList<Integer>();

    /** ProActive empty constructor */
    public Worker() {
    }

    /**
     * Return true if the given number is prime, false if not.
     *
     * @param num the number to test.
     * @return true if the given number is prime, false if not.
     */
    public BooleanWrapper isPrime(int num) {
        for (Integer n : primeNumbers) {
            if ((num % n) == 0) {
                return new BooleanWrapper(false);
            }
        }

        return new BooleanWrapper(true);
    }

    /**
     * Add a prime number to the list of primes.
     *
     * @param num the number to add.
     */
    public void addPrimeNumber(int num) {
        primeNumbers.add(num);
    }
}
