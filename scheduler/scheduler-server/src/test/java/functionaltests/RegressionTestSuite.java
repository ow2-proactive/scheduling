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
package functionaltests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import functionaltests.api.TestJobInstantGetTaskResult;
import functionaltests.api.TestMultipleUsersMakingMassivGetResultRequest;
import functionaltests.dataspaces.TestDataspaceConcurrentKilling;
import functionaltests.dataspaces.TestDataspaceConcurrentTransfer;
import functionaltests.db.schedulerdb.SchedulerDbManagerConcurrencyTest;
import functionaltests.db.schedulerdb.TestJobRemove;
import functionaltests.db.schedulerdb.TestMultipleTasks;
import functionaltests.job.taskkill.TestJobKilled;
import functionaltests.job.taskkill.TestProcessTreeKiller;
import functionaltests.recover.TaskReconnectionWithForkedTaskExecutorTest;
import functionaltests.recover.TaskReconnectionWithInProcessTaskExecutorTest;
import functionaltests.recover.TaskRecoveryWhenNodesAreReservedInBatchTest;
import functionaltests.rm.TestOperationsWhenUnlinked;
import functionaltests.workflow.TestJobLegacySchemas;
import functionaltests.workflow.TestWorkflowSubmission;
import functionaltests.workflow.complex.TestWorkflowComplexJobs;
import functionaltests.workflow.complex.TestWorkflowComplexJobs2;
import functionaltests.workflow.complex.TestWorkflowComplexJobs3;
import functionaltests.workflow.complex.TestWorkflowComplexJobs4;
import functionaltests.workflow.complex.TestWorkflowComplexJobs5;
import functionaltests.workflow.complex.TestWorkflowIfJobs;
import functionaltests.workflow.complex.TestWorkflowLoopJobs;
import functionaltests.workflow.complex.TestWorkflowLoopJobs2;
import functionaltests.workflow.complex.TestWorkflowReplicateJobs;
import functionaltests.workflow.complex.TestWorkflowReplicateJobs2;
import functionaltests.workflow.complex.TestWorkflowReplicateJobs3;


@RunWith(Suite.class)
@Suite.SuiteClasses({
                      // DB Tests
                      SchedulerDbManagerConcurrencyTest.class, TestJobRemove.class, TestMultipleTasks.class,

                      // misc
                      TestOperationsWhenUnlinked.class,

                      // Tests without scheduler restart
                      TestJobInstantGetTaskResult.class, TestMultipleUsersMakingMassivGetResultRequest.class,
                      TestJobLegacySchemas.class,

                      // Tests with scheduler restart
                      TestProcessTreeKiller.class,

                      // Tests in non forked mode without scheduler restart
                      TestWorkflowSubmission.class, TestWorkflowComplexJobs.class, TestWorkflowComplexJobs2.class,
                      TestWorkflowComplexJobs3.class, TestWorkflowComplexJobs4.class, TestWorkflowComplexJobs5.class,
                      TestWorkflowIfJobs.class, TestWorkflowLoopJobs.class, TestWorkflowLoopJobs2.class,
                      TestWorkflowReplicateJobs.class, TestWorkflowReplicateJobs2.class,
                      TestWorkflowReplicateJobs3.class,

                      // Tests with different configurations (and restart)
                      TestJobKilled.class,

                      TestDataspaceConcurrentTransfer.class, TestDataspaceConcurrentKilling.class,
                      TaskReconnectionWithForkedTaskExecutorTest.class,
                      TaskReconnectionWithInProcessTaskExecutorTest.class,
                      TaskRecoveryWhenNodesAreReservedInBatchTest.class })

/**
 * @author ActiveEon Team
 * @since 10/01/2018
 */
public class RegressionTestSuite {
}
