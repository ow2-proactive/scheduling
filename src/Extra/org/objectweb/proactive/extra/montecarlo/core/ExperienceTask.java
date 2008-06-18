package org.objectweb.proactive.extra.montecarlo.core;

import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;
import org.objectweb.proactive.extra.montecarlo.ExperienceSet;

import java.util.ArrayList;
import java.util.Random;


/**
 * ExperienceTask
 *
 * @author The ProActive Team
 */
public class ExperienceTask implements Task<ArrayList<Double>> {

    private ExperienceSet exp;

    public ExperienceTask(ExperienceSet exp) {
        this.exp = exp;
    }

    public ArrayList<Double> run(WorkerMemory memory) throws Exception {
        final Random random = (Random) memory.load("rng");
        return exp.simulate(random);
    }
}