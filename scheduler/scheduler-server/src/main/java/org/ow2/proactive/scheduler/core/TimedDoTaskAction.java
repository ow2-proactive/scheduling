/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.core;

import java.io.IOException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

import org.apache.commons.collections4.ListUtils;
import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil;
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil.HybridEncryptedData;
import org.ow2.proactive.scheduler.common.TaskDescriptor;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptorImpl;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.containers.ExecutableContainer;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.internal.InternalTaskParentFinder;
import org.ow2.proactive.scheduler.task.internal.TaskRecoveryData;
import org.ow2.proactive.scheduler.util.TaskConfiguration;
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

    private final TaskRecoveryData taskRecoveryData;

    private boolean taskWasRestarted;

    private final InternalTaskParentFinder internalTaskParentFinder;

    private final String sessionid;

    private ScriptExecutableContainer container = null;

    /**
     * Create a new instance of TimedDoTaskAction
     *
     * @param launcher the launcher of the task
     * @param taskRecoveryData data used for task recovery
     */
    public TimedDoTaskAction(InternalJob job, TaskDescriptor taskDescriptor, TaskLauncher launcher,
            SchedulingService schedulingService, TaskTerminateNotification terminateNotification,
            PrivateKey corePrivateKey, TaskRecoveryData taskRecoveryData, String sessionid) {
        this.job = job;
        this.taskDescriptor = taskDescriptor;
        this.task = ((EligibleTaskDescriptorImpl) taskDescriptor).getInternal();
        this.launcher = launcher;
        this.schedulingService = schedulingService;
        this.terminateNotification = terminateNotification;
        this.corePrivateKey = corePrivateKey;
        this.internalTaskParentFinder = InternalTaskParentFinder.getInstance();
        this.taskRecoveryData = taskRecoveryData;
        this.sessionid = sessionid;
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
                InternalTask internalTask = ((EligibleTaskDescriptorImpl) taskDescriptor).getInternal();
                internalTask.updateParentTasksResults(schedulingService);

                Set<TaskId> parentIds = new LinkedHashSet<>(resultSize);
                for (int i = 0; i < resultSize; i++) {
                    InternalTask parentTask = ((EligibleTaskDescriptorImpl) taskDescriptor.getParents()
                                                                                          .get(i)).getInternal();
                    parentIds.addAll(internalTaskParentFinder.getFirstNotSkippedParentTaskIds(parentTask));
                }

                params = new TaskResult[parentIds.size()];

                int i = 0;
                for (TaskId taskId : parentIds) {
                    params[i] = task.getParentTasksResults().get(taskId);
                    i++;
                }

            }

            // activate loggers for this task if needed
            schedulingService.getListenJobLogsSupport().activeLogsIfNeeded(job.getId(), launcher);

            fillContainer();

            // try launch the task
            launcher.doTask(container,
                            params,
                            terminateNotification,
                            taskRecoveryData.getTerminateNotificationNodeURL(),
                            taskRecoveryData.isTaskRecoverable());
        } catch (Throwable e) {
            logger.warn("Failed to start task: " + e.getMessage(), e);
            restartTask();
        }
        return null;
    }

    protected void fillContainer() throws KeyException, NoSuchAlgorithmException, IOException {
        container = new ScriptExecutableContainer((ScriptExecutableContainer) task.getExecutableContainer());
        boolean isRunAsMeEnabled = TaskConfiguration.isRunAsMeTask(task);

        container.setRunAsUser(isRunAsMeEnabled);

        createAndSetCredentials();
        container.getScript().setSessionid(sessionid);
        container.getScript().setOwner(job.getOwner());
    }

    private void createAndSetCredentials() throws KeyException, NoSuchAlgorithmException {
        CredData decryptedUserCredentials = job.getCredentials().decrypt(corePrivateKey);

        if (PASchedulerProperties.SCHEDULER_AUTH_GLOBAL_DOMAIN.isSet() &&
            decryptedUserCredentials.getDomain() == null) {
            decryptedUserCredentials.setDomain(PASchedulerProperties.SCHEDULER_AUTH_GLOBAL_DOMAIN.getValueAsString());
        }

        enrichWithThirdPartyCredentials(decryptedUserCredentials);

        PublicKey nodePublicKey = launcher.generatePublicKey();
        Credentials nodeEncryptedUserCredentials = Credentials.createCredentials(decryptedUserCredentials,
                                                                                 nodePublicKey);

        container.setCredentials(nodeEncryptedUserCredentials);
    }

    protected boolean areThirdPartyCredentialsDefined() {
        return schedulingService.getInfrastructure()
                                .getDBManager()
                                .hasThirdPartyCredentials(job.getJobInfo().getJobOwner());
    }

    private void enrichWithThirdPartyCredentials(CredData decryptedUserCredentials) throws KeyException {
        Map<String, HybridEncryptedData> thirdPartyCredentials = schedulingService.getInfrastructure()
                                                                                  .getDBManager()
                                                                                  .thirdPartyCredentialsMap(job.getJobInfo()
                                                                                                               .getJobOwner());
        for (Map.Entry<String, HybridEncryptedData> thirdPartyCredential : thirdPartyCredentials.entrySet()) {
            String decryptedValue = HybridEncryptionUtil.decryptString(thirdPartyCredential.getValue(), corePrivateKey);
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
