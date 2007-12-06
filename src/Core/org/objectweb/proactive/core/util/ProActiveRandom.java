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
package org.objectweb.proactive.core.util;

import java.security.SecureRandom;


/**
 * Provides an easy to get a random values for a SecureRandom PRNG
 *
 * A single PRNG is shared for the whole ProActive Runtime.
 *
 * @see SecureRandom
 */
public class ProActiveRandom {
    static private SecureRandom prng = new SecureRandom();

    /** Returns the next pseudorandom, uniformly distributed boolean value from this random number generator's sequence. */
    synchronized static public boolean nextBoolean() {
        return prng.nextBoolean();
    }

    /** Generates random bytes and places them into a user-supplied byte array. */
    synchronized static public void nextBytes(byte[] bytes) {
        prng.nextBytes(bytes);
    }

    /** Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence. */
    synchronized static public double nextDouble() {
        return prng.nextDouble();
    }

    /**  Returns the next pseudorandom, uniformly distributed float  value between 0.0 and 1.0 from this random number generator's sequence. */
    synchronized static public float nextFloat() {
        return prng.nextFloat();
    }

    /** Returns the next pseudorandom, uniformly distributed int  value from this random number generator's sequence.*/
    synchronized static public int nextInt() {
        return prng.nextInt();
    }

    /** Returns the next pseudorandom, uniformly distributed positive int  value from this random number generator's sequence.*/
    synchronized static public int nextPosInt() {
        return prng.nextInt(Integer.MAX_VALUE);
    }

    /** Returns a pseudorandom, uniformly distributed int value between 0 (inclusive) and the specified value (exclusive), drawn from this random number generator's sequence. */
    synchronized static public int nextInt(int n) {
        return prng.nextInt(n);
    }

    /** Returns the next pseudorandom, uniformly distributed long  value from this random number generator's sequence. */
    synchronized static public long nextLong() {
        return prng.nextLong();
    }

    synchronized static public long nextPosLong() {
        long l = nextLong();
        while (l <= 0) {
            l = nextLong();
        }

        return l;
    }
}
