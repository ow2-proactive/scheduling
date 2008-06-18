package org.objectweb.proactive.extra.montecarlo.prng;

import java.io.Serializable;
import java.util.Random;


/**
 * RandomNumberGeneratorFactory
 *
 * @author The ProActive Team
 */
public interface RandomNumberGeneratorFactory extends Serializable {

    /**
     * Create a new RNG independant from the previously created ones
     *
     * @return RandomNumberGenerator
     */
    Random createRandomNumberGenerator();

}
