/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
import org.objectweb.proactive.extensions.masterworker.interfaces.DivisibleTask;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskIntern;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


/**
 * Adapter to wrap Master/Worker tasks into Scheduler tasks
 * @author The ProActive Team
 *
 */
public class SchedulerExecutableAdapter extends JavaExecutable implements WorkerMemory {

    private TaskIntern<Serializable> task;

    private static Map<String, Object> memory = null;

    public SchedulerExecutableAdapter() {

    }

    public void init(Map<String, Serializable> args) throws Exception {
        Serializable code = args.get("taskCode");
        task = (TaskIntern<Serializable>) code;
        if (task.getTask() instanceof DivisibleTask) {
            throw new IllegalStateException("Divisible tasks can't be submitted to the ProActive Scheduler");
        }

        Serializable mem = args.get("workerMem");
        if (memory == null) {
            if (mem == null) {
                memory = new HashMap<String, Object>();
            } else {
                memory = (Map<String, Object>) mem;
            }
        }
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
