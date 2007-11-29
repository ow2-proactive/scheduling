/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.scheduler.common.job;

import java.io.Serializable;
import java.util.HashMap;

import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;


/**
 * Result of the execution of a job which is a map of task result.<br>
 * The key of the map is the name of the task on which to get the result.<br>
 * To identify the job attached to this result, a JobId and its name are provide by this class.<br>
 * You can get the task results within 3 different ways :<ul>
 * <li></li> Every results of every tasks executed in the job.
 * <li></li> Every results that have generated an exception.
 * <li></li> Every results that you pointed out as precious result.
 * <ul>
 *
 * @author jlscheef - ProActiveTeam
 * @version 1.0, Jul 5, 2007
 * @since ProActive 3.2
 * @publicAPI
 */
public interface JobResult extends Serializable {

    /**
     * To get the id of the job corresponding to this result.
     *
     * @return the id of the job corresponding to this result.
     */
    public JobId getId();

    /**
     * To get the name of the job that has generate this result.
     *
     * @return the name of the job that has generate this result.
     */
    public String getName();

    /**
     * Add a new task result to this job result.<br>
     * Used by the scheduler to fill your job result.
     *
     * @param taskName user define name (in XML) of the task.
     * @param taskResult the corresponding result of the task.
     * @param isFinal, true if this taskResult is a final one.
     */
    public void addTaskResult(String taskName, TaskResult taskResult,
        boolean isFinal);

    /**
     * Return every task results of this job as a mapping between
     * user task name (in XML job description) and its task result.<br>
     * User that wants to get a specific result may get this map and ask for a specific mapping.
     *
     * @return the task result as a map.
     */
    public HashMap<String, TaskResult> getAllResults();

    /**
     * Return only the precious results of this job as a mapping between
     * user task name (in XML job description) and its task result.<br>
     * User that wants to get a specific result may get this map and ask for a specific mapping.
     *
     * @return the precious results as a map.
     */
    public HashMap<String, TaskResult> getPreciousResults();

    /**
     * Return only the task results that have generated an exception.<br>
     * User that wants to get a specific result may get this map and ask for a specific mapping.
     *
     * @return the precious results as a map.
     */
    public HashMap<String, TaskResult> getExceptionResults();

    /**
     * Returns true  if the job has generated an exception, false if not.
     *
     * @return true if the job has generated an exception.
     */
    public boolean hadException();
}
