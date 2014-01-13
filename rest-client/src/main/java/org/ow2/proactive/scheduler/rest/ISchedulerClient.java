/*
 *  
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.rest;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.apache.commons.io.IOUtils.copy;
import static org.ow2.proactive.scheduler.rest.ExceptionUtility.throwNCEOrPE;
import static org.ow2.proactive.scheduler.rest.ExceptionUtility.throwUJEOrNCEOrPEOrUTE;
import static org.ow2.proactive.scheduler.rest.data.DataUtility.taskState;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;

public interface ISchedulerClient extends Scheduler {

    /**
     * Initialize this instance.
     * 
     * @param url
     *            the REST server URL
     * @param login
     *            the login
     * @param password
     *            the password
     * @throws Exception
     *             if an error occurs during the initialization
     */
    public void init(String url, String login, String password)
            throws Exception;

    public boolean isJobFinished(JobId jobId) throws NotConnectedException,
            UnknownJobException, PermissionException;

    public boolean isJobFinished(String jobId) throws NotConnectedException,
            UnknownJobException, PermissionException;

    public JobResult waitForJob(JobId jobId, long timeout)
            throws NotConnectedException, UnknownJobException,
            PermissionException, TimeoutException;

    public JobResult waitForJob(String jobId, long timeout)
            throws NotConnectedException, UnknownJobException,
            PermissionException, TimeoutException;

    public boolean isTaskFinished(String jobId, String taskName)
            throws UnknownJobException, NotConnectedException,
            PermissionException, UnknownTaskException;

    public TaskResult waitForTask(String jobId, String taskName, long timeout)
            throws UnknownJobException, NotConnectedException,
            PermissionException, UnknownTaskException, TimeoutException;

    public List<JobResult> waitForAllJobs(List<String> jobIds, long timeout)
            throws NotConnectedException, UnknownJobException,
            PermissionException, TimeoutException;

    public Map.Entry<String, JobResult> waitForAnyJob(List<String> jobIds,
            long timeout) throws NotConnectedException, UnknownJobException,
            PermissionException, TimeoutException;

    public Entry<String, TaskResult> waitForAnyTask(String jobId,
            List<String> taskNames, long timeout) throws UnknownJobException,
            NotConnectedException, PermissionException, UnknownTaskException,
            TimeoutException;

    public List<Entry<String, TaskResult>> waitForAllTasks(String jobId,
            List<String> taskNames, long timeout) throws UnknownJobException,
            NotConnectedException, PermissionException, UnknownTaskException,
            TimeoutException;

    public boolean pushFile(String spacename, String pathname, String filename,
            String file) throws NotConnectedException, PermissionException;

    public void pullFile(String space, String pathname, String outputFile)
            throws NotConnectedException, PermissionException;

    public boolean deleteFile(String space, String pathname)
            throws NotConnectedException, PermissionException;
}
