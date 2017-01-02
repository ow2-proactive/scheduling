/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil;
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil.HybridEncryptedData;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.descriptor.TaskDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.internal.InternalTaskParentFinder;
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

    private final PrivateKey corePrivateKey;

    private boolean taskWasRestarted;

    private final InternalTaskParentFinder internalTaskParentFinder;

    /**
     * Create a new instance of TimedDoTaskAction
     *
     * @param launcher the launcher of the task
     */
    public TimedDoTaskAction(InternalJob job, TaskDescriptor taskDescriptor, TaskLauncher launcher,
            SchedulingService schedulingService, TaskTerminateNotification terminateNotification,
            PrivateKey corePrivateKey) {
        this.job = job;
        this.taskDescriptor = taskDescriptor;
        this.task = taskDescriptor.getInternal();
        this.launcher = launcher;
        this.schedulingService = schedulingService;
        this.terminateNotification = terminateNotification;
        this.corePrivateKey = corePrivateKey;
        this.internalTaskParentFinder = InternalTaskParentFinder.getInstance();
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

                Set<TaskId> parentIds = new HashSet<>(resultSize);
                for (int i = 0; i < resultSize; i++) {
                    parentIds.addAll(internalTaskParentFinder.getFirstNotSkippedParentTaskIds(
                            taskDescriptor.getParents().get(i).getInternal()));
                }

                params = new TaskResult[parentIds.size()];

                Map<TaskId, TaskResult> taskResults = schedulingService.getInfrastructure().getDBManager()
                        .loadTasksResults(job.getId(), new ArrayList<>(parentIds));

                int i = 0;
                for (TaskId taskId : parentIds) {
                    params[i] = taskResults.get(taskId);
                    i++;
                }

            }

            // activate loggers for this task if needed
            schedulingService.getListenJobLogsSupport().activeLogsIfNeeded(job.getId(), launcher);

            fillContainer();
            // try launch the task
            launcher.doTask(task.getExecutableContainer(), params, terminateNotification);
        } catch (Throwable e) {
            logger.warn("Failed to start task: " + e.getMessage(), e);
            restartTask();
        }
        return null;
    }

    protected void fillContainer() throws KeyException, NoSuchAlgorithmException {
        boolean isRunAsMeEnabled = task.isRunAsMe();

        task.getExecutableContainer().setRunAsUser(isRunAsMeEnabled);

        createAndSetCredentials();
    }

    private void createAndSetCredentials() throws KeyException, NoSuchAlgorithmException {
        CredData decryptedUserCredentials = job.getCredentials().decrypt(corePrivateKey);

        enrichWithThirdPartyCredentials(decryptedUserCredentials);

        PublicKey nodePublicKey = launcher.generatePublicKey();
        Credentials nodeEncryptedUserCredentials = Credentials.createCredentials(decryptedUserCredentials,
                nodePublicKey);

        task.getExecutableContainer().setCredentials(nodeEncryptedUserCredentials);
    }

    protected boolean areThirdPartyCredentialsDefined() {
        return schedulingService.getInfrastructure().getDBManager()
                .hasThirdPartyCredentials(job.getJobInfo().getJobOwner());
    }

    private void enrichWithThirdPartyCredentials(CredData decryptedUserCredentials) throws KeyException {
        Map<String, HybridEncryptedData> thirdPartyCredentials = schedulingService.getInfrastructure()
                .getDBManager().thirdPartyCredentialsMap(job.getJobInfo().getJobOwner());
        for (Map.Entry<String, HybridEncryptedData> thirdPartyCredential : thirdPartyCredentials.entrySet()) {
            String decryptedValue = HybridEncryptionUtil.decryptString(thirdPartyCredential.getValue(),
                    corePrivateKey);
            decryptedUserCredentials.addThirdPartyCredential(thirdPartyCredential.getKey(), decryptedValue);
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
