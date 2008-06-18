package org.objectweb.proactive.extra.montecarlo.prng.mt;

import edu.cornell.lassp.houle.RngPack.Ranmar;
import org.objectweb.proactive.extra.montecarlo.prng.RandomNumberGeneratorFactory;

import java.io.Serializable;
import java.util.Random;


/**
 * MersenneTwisterRNGFactory
 *
 * @author The ProActive Team
 */
public class MersenneTwisterRNGFactory implements RandomNumberGeneratorFactory, Serializable {

    private Ranmar seedGenerator;

    public MersenneTwisterRNGFactory() {
        this.seedGenerator = new Ranmar();
    }

    public Random createRandomNumberGenerator() {
        // TODO this is a naive implementation, LF generators have the property that two successive values are well separated, thus we use these successive values for seeding a MT generator.
        long seed = (long) ((seedGenerator.raw() * 2.0 - 1.0) * Long.MAX_VALUE);
        return new MersenneTwisterFast(seed);
    }

}
