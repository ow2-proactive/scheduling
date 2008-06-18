package org.objectweb.proactive.extra.montecarlo.core;

import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;
import org.objectweb.proactive.extensions.masterworker.interfaces.DivisibleTask;
import org.objectweb.proactive.extra.montecarlo.Experience;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;


/**
 * MCTask
 *
 * @author The ProActive Team
 */
public class ExperienceTask implements Task<ArrayList<Double>> {

    private Experience exp;

    public ExperienceTask(Experience exp) {
        this.exp = exp;
    }

    public ArrayList<Double> run(WorkerMemory memory) throws Exception {
        final Random random = (Random) memory.load("prng");
        return exp.simulate(random);
    }
}