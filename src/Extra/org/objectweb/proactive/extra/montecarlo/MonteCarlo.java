package org.objectweb.proactive.extra.montecarlo;

import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extra.montecarlo.core.ExperienceTask;
import org.objectweb.proactive.extra.montecarlo.core.EngineTaskAdapter;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;


/**
 * MonteCarloSolver
 *
 * @author The ProActive Team
 */
public class MonteCarlo {

    ProActiveMaster master = null;

    public MonteCarlo() {

    }

    public void init(URL descriptorURL, String masterVNName, String workersVNName) {
        if (masterVNName != null) {
            // Remote master
            master = new ProActiveMaster<EngineTaskAdapter, Serializable>(descriptorURL, masterVNName);
        } else {
            // Local master
            master = new ProActiveMaster<EngineTaskAdapter, Serializable>();
        }
    }

    public Serializable run(EngineTask toplevelTask) throws TaskException {
        ArrayList<EngineTaskAdapter> singletask = new ArrayList<EngineTaskAdapter>(1);
        singletask.add(new EngineTaskAdapter(toplevelTask));
        master.solve(singletask);
        return master.waitOneResult();
    }

}
