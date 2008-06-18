package org.objectweb.proactive.extra.montecarlo;

import java.util.ArrayList;
import java.util.Random;


/**
 * ExperienceSet
 *
 * This interface defines a Monte-Carlo set of successive experiences, using the provided random generator
 *
 * @author The ProActive Team
 */
public interface ExperienceSet {

    /**
     * Defines a Monte-Carlo set of successive experiences, a Random generator is given and will be used
     *
     * A list of double values is expected as output, result of the successive experiences.
     * These experiences can be independant or correlated, this choice is left to the user inside the implementation of this method.
     *
     * @param rng random number generator
     * @return a list of double values
     */
    ArrayList<Double> simulate(final Random rng);
}
