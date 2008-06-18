package org.objectweb.proactive.extra.montecarlo;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: fviale
 * Date: 13 juin 2008
 * Time: 17:03:43
 * To change this template use File | Settings | File Templates.
 */
public interface Experience {

    ArrayList<Double> simulate(final Random rng);
}
