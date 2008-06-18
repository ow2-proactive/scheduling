package org.objectweb.proactive.extra.montecarlo.core;

import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extra.montecarlo.ExperienceSet;
import org.objectweb.proactive.extra.montecarlo.Simulator;

import java.util.ArrayList;
import java.util.List;


/**
 * SimulatorImpl
 *
 * @author The ProActive Team
 */
public class SimulatorImpl implements Simulator {

    SubMaster master;

    public SimulatorImpl(SubMaster master) {
        this.master = master;
    }

    public ArrayList<Double> solve(List<ExperienceSet> experienceSets) throws TaskException {
        ArrayList<ExperienceTask> adpaterTasks = new ArrayList<ExperienceTask>(experienceSets.size());
        master.solve(adpaterTasks);
        List<ArrayList<Double>> results = master.waitAllResults();
        int bigsize = 0;
        for (ArrayList<Double> chunk : results) {
            bigsize += chunk.size();
        }
        ArrayList<Double> finalResults = new ArrayList<Double>(bigsize);
        for (ArrayList<Double> chunk : results) {
            finalResults.addAll(chunk);
        }
        return finalResults;
    }
}
