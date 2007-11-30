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
package org.objectweb.proactive.extra.scheduler.examples;

import java.util.ArrayList;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * Worker prime.
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Jul 17, 2007
 * @since ProActive 3.9
 *
 */
public class Worker implements java.io.Serializable {
    private static final long serialVersionUID = 6479587603886940747L;

    // primeNumbers already known by the worker
    private ArrayList<Integer> primeNumbers = new ArrayList<Integer>();

    /** ProActive empty constructor */
    public Worker() {
    }

    public BooleanWrapper isPrime(int num) {
        for (Integer n : primeNumbers) {
            if ((num % n) == 0) {
                return new BooleanWrapper(false);
            }
        }

        return new BooleanWrapper(true);
    }

    public void addPrimeNumber(int num) {
        primeNumbers.add(num);
    }
}
