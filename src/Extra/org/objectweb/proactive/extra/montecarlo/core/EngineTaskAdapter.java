package org.objectweb.proactive.extra.montecarlo.core;

import org.objectweb.proactive.extensions.masterworker.interfaces.DivisibleTask;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;
import org.objectweb.proactive.extra.montecarlo.EngineTask;

import java.io.Serializable;


/**
 * EngineTaskAdapter
 *
 * @author The ProActive Team
 */
public class EngineTaskAdapter implements DivisibleTask<Serializable> {

    private EngineTask task;

    public EngineTaskAdapter(EngineTask task) {
        this.task = task;
    }

    public Serializable run(WorkerMemory memory) throws Exception {
        return new UnsupportedOperationException();
    }

    public Serializable run(WorkerMemory memory, SubMaster master) throws Exception {
        return task.run(new SimulatorImpl(master), new ExecutorImpl(master));
    }
}