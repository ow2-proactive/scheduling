/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.db.Condition;
import org.ow2.proactive.db.EmptyDatabaseManager;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.core.RecoverCallback;
import org.ow2.proactive.scheduler.job.InternalJob;


/**
 * This is an implementation of memory database.<br>
 * Everything is kept in memory. Can be used for testing purpose.<br>
 * <b>WARNING</B> this implementation can lead to "out of memory" error.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class SchedulerEmptyDatabaseManager extends EmptyDatabaseManager implements SchedulerDatabaseManager {

    private Map<JobId, InternalJob> jobs;

    /**
     * Create a new instance of EmptyDatabaseManager
     */
    public SchedulerEmptyDatabaseManager() {
        jobs = new HashMap<JobId, InternalJob>();
    }

    /**
     * Store the given object
     *
     * @param o the new object to store.
     */
    @Override
    public void register(Object o) {
        InternalJob job = (InternalJob) o;
        jobs.put(job.getId(), job);
    }

    /**
     * If egClass is JobResultImpl, return the jobResult corresponding to the given JobId in the given condition,
     * If egClass is TaskresultImpl, return the taskResult corresponding to the given TaskId in the given condition,
     * Otherwise, return an empty list
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> recover(Class<T> egClass, Condition... conditions) {
        if (conditions == null || conditions.length == 0) {
            return new ArrayList<T>();
        }
        if (JobResult.class.isAssignableFrom(egClass)) {
            ArrayList<T> res = new ArrayList<T>();
            res.add((T) jobs.get(((JobId) conditions[0].getValue())).getJobResult());
            return res;
        } else if (TaskResult.class.isAssignableFrom(egClass)) {
            ArrayList<T> res = new ArrayList<T>();
            TaskId id = (TaskId) conditions[0].getValue();
            try {
                res.add((T) jobs.get(id.getJobId()).getJobResult().getResult(id.getReadableName()));
            } catch (UnknownTaskException ute) {
                //should never happen : taskName is unknown
            }
            return res;
        } else {
            return new ArrayList<T>();
        }
    }

    /**
     * Return an empty list of InternalJob
     */
    public List<InternalJob> recoverAllJobs() {
        return recoverAllJobs(null);
    }

    /**
     * Return an empty list of InternalJob
     */
    public List<InternalJob> recoverAllJobs(RecoverCallback callback) {
        return new ArrayList<InternalJob>();
    }
}
