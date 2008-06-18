package org.objectweb.proactive.extra.montecarlo.core;

import org.objectweb.proactive.extra.montecarlo.*;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extensions.masterworker.TaskException;

import java.io.Serializable;
import java.util.List;


/**
 * ExecutorImpl
 *
 * @author The ProActive Team
 */
public class ExecutorImpl implements Executor {

    SubMaster master;

    public ExecutorImpl(SubMaster master) {
        this.master = master;
    }

    public List<Serializable> solve(List<EngineTask> engineTasks) throws TaskException {
        master.solve(engineTasks);
        return master.waitAllResults();
    }
}
