package org.objectweb.proactive.extra.montecarlo.core;

import org.objectweb.proactive.extensions.masterworker.interfaces.MemoryFactory;
import org.objectweb.proactive.extra.montecarlo.prng.mt.MersenneTwisterRNGFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * MCMemoryFactory
 *
 * @author The ProActive Team
 */
public class MCMemoryFactory implements MemoryFactory {

    private MersenneTwisterRNGFactory prngfactory;

    public MCMemoryFactory() {
        prngfactory = new MersenneTwisterRNGFactory();
    }

    public Map<String, Serializable> newMemoryInstance() {
        HashMap map = new HashMap(1);
        map.put("rng", prngfactory.createRandomNumberGenerator());
        return map;
    }
}
