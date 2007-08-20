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
        return prng.nextInt();
    }

    /** Returns a pseudorandom, uniformly distributed int value between 0 (inclusive) and the specified value (exclusive), drawn from this random number generator's sequence. */
    synchronized static public int nextInt(int n) {
        return prng.nextInt(n);
    }

    /** Returns the next pseudorandom, uniformly distributed long  value from this random number generator's sequence. */
    synchronized static public long nextLong() {
        return prng.nextLong();
    }
}
