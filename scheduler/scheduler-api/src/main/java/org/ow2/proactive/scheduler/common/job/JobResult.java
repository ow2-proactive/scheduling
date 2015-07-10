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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.job;

import java.io.Serializable;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * Result of the execution of a job which is a map of task result.<br>
 * The key of the map is the name of the task on which to get the result.<br>
 * To identify the job attached to this result, a JobId and its name are provide by this class.<br>
 * You can get the task results within 3 different ways :<ul>
 * <li> Every results of every tasks executed in the job.</li>
 * <li> Every results that have generated an exception.</li>
 * <li> Every results that you pointed out as precious result.</li>
 * </ul>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface JobResult extends Serializable {

    /**
     * To get the id of the job corresponding to this result.
     *
     * @return the id of the job corresponding to this result.
     */
    JobId getJobId();

    /**
     * To get the name of the job that has generate this result.
     *
     * @return the name of the job that has generate this result.
     */
    String getName();

    /**
     * Return the information of the corresponding job
     *
     * @return the information of the corresponding job
     */
    JobInfo getJobInfo();

    /**
     * Return every task results of this job as a mapping between
     * user task name (in XML job description) and its task result.<br>
     * User that wants to get a specific result may get this map and ask for a specific mapping
     * or use the {@link #getResult(String)} method.
     *
     * @return the task result as a map.
     */
    Map<String, TaskResult> getAllResults();

    /**
     * Return only the precious results of this job as a mapping between
     * user task name (in XML job description) and its task result.<br>
     * User that wants to get a specific result may get this map and ask for a specific mapping.
     *
     * @return the precious results as a map.
     */
    Map<String, TaskResult> getPreciousResults();

    /**
     * Return only the task results that have generated an exception.<br>
     * User that wants to get a specific result may get this map and ask for a specific mapping.
     *
     * @return the precious results as a map.
     */
    Map<String, TaskResult> getExceptionResults();

    /**
     * Return the task Result corresponding to the given name.
     * A result is returned if the taskName exist and if the result is available.
     * Null is returned if the task is not finished (has no result).
     *
     * @param taskName the task name of the result to get.
     * @return the result corresponding to the given task name, null if not found or not finished.
     * @throws UnknownTaskException if the given task name does not exist in this job
     */
    TaskResult getResult(String taskName) throws UnknownTaskException;

    /**
     * Remove the result of the given task in this jobResult.
     * 
     * @param taskName the identification name of the task.
     * @throws UnknownTaskException if the given task name does not exist in this job
     */
    void removeResult(String taskName) throws UnknownTaskException;

    /**
     * Returns true  if the job has generated an exception, false if not.
     *
     * @return true if the job has generated an exception.
     */
    boolean hadException();
}
