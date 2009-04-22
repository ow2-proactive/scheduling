/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.ext.masterworker;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskIntern;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


/**
 * Adapter to wrap Master/Worker tasks into Scheduler tasks
 * @author The ProActive Team
 *
 */
public class SchedulerExecutableAdapter extends JavaExecutable implements WorkerMemory {

    /**
     *
     */
    private static final long serialVersionUID = 10L;

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
     * @see org.ow2.proactive.scheduler.common.task.executable.Executable#execute(org.ow2.proactive.scheduler.common.task.TaskResult[])
     */
    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
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
