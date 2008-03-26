package org.objectweb.proactive.extensions.scheduler.ext.masterworker;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskIntern;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.JavaExecutable;


/**
 * Adapter to wrap Master/Worker tasks into Scheduler tasks
 * @author The ProActive Team
 *
 */
public class SchedulerExecutableAdapter extends JavaExecutable implements WorkerMemory {

    private TaskIntern<Serializable> task;

    private static Map<String, Object> memory = new HashMap<String, Object>();

    @Deprecated
    public SchedulerExecutableAdapter() {

    }

    /**
     * Wraps a Master/Worker task to a Scheduler task
     * @param task
     */
    public SchedulerExecutableAdapter(TaskIntern<Serializable> task) {
        this.task = task;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.extensions.scheduler.common.task.executable.Executable#execute(org.objectweb.proactive.extensions.scheduler.common.task.TaskResult[])
     */
    @Override
    public Object execute(TaskResult... results) throws Throwable {
        return task.run(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory#erase(java.lang.String)
     */
    public void erase(String name) {
        memory.remove(name);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory#load(java.lang.String)
     */
    public Object load(String name) {
        return memory.get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory#save(java.lang.String,
     *      java.lang.Object)
     */
    public void save(String name, Object data) {
        memory.put(name, data);

    }

}
