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

import org.ow2.proactive.db.DatabaseManager;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.core.RecoverCallback;
import org.ow2.proactive.scheduler.job.InternalJob;


/**
 * DatabaseManager is responsible of every database transaction.<br />
 * It provides method to register, delete, synchronize objects in database.
 * Each method will work on the object and its inheritance.
 * An implementation of empty DB that keep everything in memory is also provided.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public interface SchedulerDatabaseManager extends DatabaseManager {

    /**
     * This method is specially design to recover all jobs that have to be recovered.
     * It is also a way to get them without taking care of conditions.
     * This method also ensure that every returned jobs have their @unloadable fields unloaded.
     *
     * @return The list of unloaded job entities.
     */
    public abstract List<InternalJob> recoverAllJobs();

    /**
     * This method is specially design to recover all jobs that have to be recovered.
     * It is also a way to get them without taking care of conditions.
     * This method also ensure that every returned jobs have their @unloadable fields unloaded.
     *
     * @param callback use a callback to be notified of the recovering process.
     * 			this method will first call the init method to set the number of job to recover,
     * 			then it will call the jobRecovered method each time a job is recovered.
     * @return The list of unloaded job entities.
     */
    public abstract List<InternalJob> recoverAllJobs(RecoverCallback callback);
}
