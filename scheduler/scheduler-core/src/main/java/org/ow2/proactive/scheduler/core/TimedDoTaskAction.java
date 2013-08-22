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
package org.ow2.proactive.scheduler.core;

import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.descriptor.TaskDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;
import org.ow2.proactive.threading.CallableWithTimeoutAction;


/**
 * TimedDoTaskAction is used to start the task execution in parallel.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class TimedDoTaskAction implements CallableWithTimeoutAction<Void> {

    private static final Logger logger = Logger.getLogger(TimedDoTaskAction.class);

    private final InternalJob job;

    private final TaskDescriptor taskDescriptor;

    private final InternalTask task;

    private final TaskLauncher launcher;

    private final SchedulingService schedulingService;

    private final TaskTerminateNotification terminateNotification;

    private final PrivateKey corePk;

    private boolean taskWasRestarted;

    /**
     * Create a new instance of TimedDoTaskAction
     *
     * @param task the internal task
     * @param launcher the launcher of the task
     * @param parameters the parameters to be given to the task
     */
    public TimedDoTaskAction(InternalJob job, TaskDescriptor taskDescriptor, TaskLauncher launcher,
            SchedulingService schedulingService, TaskTerminateNotification terminateNotification,
            PrivateKey corePk) {
        this.job = job;
        this.taskDescriptor = taskDescriptor;
        this.task = taskDescriptor.getInternal();
        this.launcher = launcher;
        this.schedulingService = schedulingService;
        this.terminateNotification = terminateNotification;
        this.corePk = corePk;
    }

    /**
     * {@inheritDoc}
     */
    public Void call() throws Exception {
        try {
            // Set to empty array to emulate varargs behavior (i.e. not defined is
            // equivalent to empty array, not null.
            TaskResult[] params = new TaskResult[0];
            //if job is TASKSFLOW, preparing the list of parameters for this task.
            int resultSize = taskDescriptor.getParents().size();
            if ((job.getType() == JobType.TASKSFLOW) && (resultSize > 0) && task.handleResultsArguments()) {
                params = new TaskResult[resultSize];
                List<TaskId> parentIds = new ArrayList<TaskId>(resultSize);
                for (int i = 0; i < resultSize; i++) {
                    parentIds.add(taskDescriptor.getParents().get(i).getTaskId());
                }
                Map<TaskId, TaskResult> taskResults = schedulingService.getInfrastructure().getDBManager()
                        .loadTasksResults(job.getId(), parentIds);
                for (int i = 0; i < resultSize; i++) {
                    params[i] = taskResults.get(taskDescriptor.getParents().get(i).getTaskId());
                }
            }

            // activate loggers for this task if needed
            schedulingService.getListenJobLogsSupport().activeLogsIfNeeded(job.getId(), launcher);

            fillContainerWithEncryption();
            // try launch the task
            launcher.doTask(terminateNotification, task.getExecutableContainer(), params);
        } catch (Throwable e) {
            logger.warn("Failed to start task: " + e.getMessage(), e);
            restartTask();
        }
        return null;
    }

    /**
     * If runAsMe is true, get the public key of the execution node,
     * decrypt user credentials, and re-encrypt them using the received public key.
     *
     * @throws KeyException if there was a problem while moving credentials
     * @throws NoSuchAlgorithmException if RSA is unknown
     */
    private void fillContainerWithEncryption() throws KeyException, NoSuchAlgorithmException {
        //do nothing if runAsMe is false or not set
        if (task.isRunAsMe()) {
            PublicKey pubkey = launcher.generatePublicKey();
            //decrypt user credential with core private key
            CredData credDataFromClient = job.getCredentials().decrypt(corePk);
            //cred becomes the credentials to be returned with new publicKey encryption
            Credentials credForNode = Credentials.createCredentials(credDataFromClient, pubkey);
            task.getExecutableContainer().setCredentials(credForNode);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void timeoutAction() {
        try {
            logger.warn("Task start timeout for task '" + task.getId() + "'");
            restartTask();
        } catch (Throwable e) {
            logger.warn("Exception during submit timeout handling: " + e.getMessage(), e);
        }
    }

    private synchronized void restartTask() {
        if (taskWasRestarted) {
            return;
        }
        logger.info("Trying to restart task '" + task.getId() + "'");
        schedulingService.restartTaskOnNodeFailure(task);
        taskWasRestarted = true;
    }
}
