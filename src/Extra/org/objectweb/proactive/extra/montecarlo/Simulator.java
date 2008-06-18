package org.objectweb.proactive.extra.montecarlo;

import org.objectweb.proactive.extensions.masterworker.TaskException;

import java.util.ArrayList;
import java.util.List;


/**
 * Simulator
 *
 * This interface represents an access to the Monte-Carlo engine for solving a list of experience sets in parallel.
 *
 * @author The ProActive Team
 */
public interface Simulator {

    /**
     * Asks the engine to solve a list of experience sets which will be run in parallel
     * @param experienceSets list of experience sets to solve
     * @return a list of double which is a concatenation of each list of double produced by each experience set. The order of the ouput list is guarantied to be coherent with the order of the experience list.
     * @throws TaskException if an exception occured inside the user code
     */
    public ArrayList<Double> solve(List<ExperienceSet> experienceSets) throws TaskException;
}
