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
import org.ow2.proactive.scheduler.policy.edf.EDFPolicyExtended;

import functionaltests.api.AuthenticationTest;
import functionaltests.api.SchedulerUsageTest;
import functionaltests.api.TestGetUsage;
import functionaltests.api.TestGetUsers;
import functionaltests.api.TestLoadJobs;
import functionaltests.api.TestSchedulerMiscEvents;
import functionaltests.credentials.TaskUsingCredentialsTest;
import functionaltests.credentials.TestThirdPartyCredentialsDefined;
import functionaltests.dataspaces.TaskProActiveDataspacesIntegrationTest;
import functionaltests.dataspaces.TestCacheSpace;
import functionaltests.dataspaces.TestCacheSpaceCleaning;
import functionaltests.dataspaces.TestCleanTaskWorkingDir;
import functionaltests.dataspaces.TestDataSpaceConfiguration;
import functionaltests.dataspaces.TestDataSpaceServiceStarter;
import functionaltests.dataspaces.TestDataspaceScripts;
import functionaltests.dataspaces.TestGlobalSpace;
import functionaltests.dataspaces.TestJobDataspaceSubmission;
import functionaltests.dataspaces.TestSpecialCharacterFileName;
import functionaltests.dataspaces.TestSubmitJobWithPartiallyUnaccessibleDataSpaces;
import functionaltests.dataspaces.TestSubmitJobWithUnaccessibleDataSpaces;
import functionaltests.dataspaces.TestUserSpace;
import functionaltests.dataspaces.TestWorkflowDataspace;
import functionaltests.db.RecoveredSchedulerStateTest;
import functionaltests.db.SchedulerStateRecoverHelperTest;
import functionaltests.db.TaskDataTest;
import functionaltests.db.TopologicalTaskSorterTest;
import functionaltests.db.TransactionHelperTest;
import functionaltests.db.schedulerdb.SchedulerDbManagerConcurrencyTest;
import functionaltests.db.schedulerdb.SchedulerTasksStateRecoverIntegrationTest;
import functionaltests.db.schedulerdb.TaskDBUtilsTest;
import functionaltests.db.schedulerdb.TestDataspaceSelectorsData;
import functionaltests.db.schedulerdb.TestInMemorySchedulerDB;
import functionaltests.db.schedulerdb.TestJobAttributes;
import functionaltests.db.schedulerdb.TestJobOperations;
import functionaltests.db.schedulerdb.TestJobRuntimeData;
import functionaltests.db.schedulerdb.TestLoadJobPeriod;
import functionaltests.db.schedulerdb.TestLoadJobResult;
import functionaltests.db.schedulerdb.TestLoadJobsPagination;
import functionaltests.db.schedulerdb.TestLoadSchedulerClientState;
import functionaltests.db.schedulerdb.TestReadSchedulerAccount;
import functionaltests.db.schedulerdb.TestReportingQueries;
import functionaltests.db.schedulerdb.TestRestoreWorkflowJobs;
import functionaltests.db.schedulerdb.TestRestoreWorkflowJobs2;
import functionaltests.db.schedulerdb.TestSetJobToBeRemoved;
import functionaltests.db.schedulerdb.TestTaskAttributes;
import functionaltests.db.schedulerdb.TestTaskIdGeneration;
import functionaltests.db.schedulerdb.TestTaskResultData;
import functionaltests.db.schedulerdb.TestTaskRuntimeData;
import functionaltests.db.schedulerdb.TestThirdPartyCredentials;
import functionaltests.db.schedulerdb.TestUsageData;
import functionaltests.jmx.SchedulerJMXTest;
import functionaltests.jmx.SchedulerRuntimeDataMBeanTest;
import functionaltests.job.*;
import functionaltests.job.error.TestErrorAndFailure;
import functionaltests.job.error.TestJobAborted;
import functionaltests.job.error.TestJobCanceledWithReplication;
import functionaltests.job.error.TestJobCanceledWithReplicationSchema33;
import functionaltests.job.error.TestTaskNotExecuted;
import functionaltests.job.error.TestTaskRestartOnNodeFailure;
import functionaltests.job.log.TestDisabledListenJobLogs;
import functionaltests.job.log.TestJobServerLogs;
import functionaltests.job.log.TestListenJobLogs;
import functionaltests.job.log.TestLoggers;
import functionaltests.job.log.TestPreciousLogs;
import functionaltests.job.multinodes.TestJobMultiNodesWalltime;
import functionaltests.job.multinodes.TestMultipleHostsRequest;
import functionaltests.job.recover.JobRecoverTest;
import functionaltests.job.taskkill.TestChildProcessOfNodeKilled;
import functionaltests.job.taskkill.TestJobKilled;
import functionaltests.job.taskkill.TestKillPendingTask;
import functionaltests.job.taskkill.TestKillTaskWhileExecutingScripts;
import functionaltests.job.taskkill.TestKillWhenInStoppedState;
import functionaltests.job.taskkill.TestKilledJobResult;
import functionaltests.job.taskkill.TestMarkedAsFinished;
import functionaltests.job.workingdir.TestForkedTaskWorkingDir;
import functionaltests.job.workingdir.TestWorkingDirStaticCommand;
import functionaltests.policy.edf.EDFPolicyExtendedTest;
import functionaltests.policy.edf.EDFPolicyTest;
import functionaltests.policy.license.TestLicensePolicy;
import functionaltests.policy.ram.TestRamPolicy;
import functionaltests.rm.TestNodeDiesAtSchedulerRestart;
import functionaltests.rm.TestRMProxy;
import functionaltests.rm.TestRMProxyRebind;
import functionaltests.rm.TestRMReconnectionWhileRunning;
import functionaltests.rm.nodesource.TestJobNodeAccessToken;
import functionaltests.runasme.TestRunAsMeLinuxKey;
import functionaltests.runasme.TestRunAsMeLinuxNone;
import functionaltests.runasme.TestRunAsMeLinuxPwd;
import functionaltests.runasme.TestRunAsMeWindows;
import functionaltests.scripts.TestDynamicGenericInfo;
import functionaltests.scripts.TestJobScriptVariables;
import functionaltests.scripts.TestScriptEngines;
import functionaltests.scripts.TestScriptForkEnvironment;
import functionaltests.scripts.TestScriptTask;
import functionaltests.scripts.TestTaskScriptVariables;
import functionaltests.scripts.TestUnauthorizedScripts;
import functionaltests.scripts.clean.TestCleaningScriptMultiNodes;
import functionaltests.scripts.clean.TestJobCleaningScriptVariables;
import functionaltests.scripts.nonforked.TestNonForkedScriptTask;
import functionaltests.scripts.selection.TestJobSelScriptSubmission;
import functionaltests.scripts.selection.TestJobSelScriptVariables;
import functionaltests.scripts.selection.TestJobWithInvalidSelectionScript;
import functionaltests.scripts.selection.TestTasksCompleteAfterSelectiontimeout;
import functionaltests.service.DatabaseSchemaTest;
import functionaltests.service.RestartAllInErrorTasksTest;
import functionaltests.service.SchedulerDBManagerTest;
import functionaltests.service.SchedulerDbManagerRecoveryTest;
import functionaltests.service.SchedulingServiceTest1;
import functionaltests.service.SchedulingServiceTest10;
import functionaltests.service.SchedulingServiceTest2;
import functionaltests.service.SchedulingServiceTest3;
import functionaltests.service.SchedulingServiceTest4;
import functionaltests.service.SchedulingServiceTest5;
import functionaltests.service.SchedulingServiceTest6;
import functionaltests.service.SchedulingServiceTest7;
import functionaltests.service.SchedulingServiceTest8;
import functionaltests.service.SchedulingServiceTest9;
import functionaltests.synchronization.TestTaskSynchronization;
import functionaltests.web.JettyStarterTest;
import functionaltests.workflow.TestGenericInformation;
import functionaltests.workflow.TestJobCoverage;
import functionaltests.workflow.TestTaskIdOrderSameAsDeclarationOrder;
import functionaltests.workflow.TestWorkflowFailedScript;
import functionaltests.workflow.TestWorkflowIterationAwareness;
import functionaltests.workflow.TestXMLTransformer;
import functionaltests.workflow.javatask.ComplexTypeArgsTest;
import functionaltests.workflow.nativetask.TestJobNativeSubmission;
import functionaltests.workflow.nativetask.TestNativeTaskPaths;
import functionaltests.workflow.variables.TestModifyPropagatedVariables;
import functionaltests.workflow.variables.TestNonForkedScriptTaskVariablePropagation;
import functionaltests.workflow.variables.TestPropagatedVariables;
import functionaltests.workflow.variables.TestPropagatedVariablesWalltime;
import functionaltests.workflow.variables.TestVariablesPatternLateBindings;
import functionaltests.workflow.variables.Test_SCHEDULING_2034;


@RunWith(Suite.class)
@Suite.SuiteClasses({

                      // Db tests
                      SchedulerDbManagerConcurrencyTest.class, SchedulerTasksStateRecoverIntegrationTest.class,
                      TaskDBUtilsTest.class, TestDataspaceSelectorsData.class, TestInMemorySchedulerDB.class,
                      TestJobAttributes.class, TestJobOperations.class, TestJobRuntimeData.class,
                      TestLoadJobPeriod.class, TestLoadJobResult.class, TestLoadJobsPagination.class,
                      TestLoadSchedulerClientState.class, TestReadSchedulerAccount.class, TestReportingQueries.class,
                      TestRestoreWorkflowJobs.class, TestRestoreWorkflowJobs2.class, TestSetJobToBeRemoved.class,
                      TestTaskAttributes.class, TestTaskIdGeneration.class, TestTaskResultData.class,
                      TestTaskRuntimeData.class, TestThirdPartyCredentials.class, TestUsageData.class,
                      RecoveredSchedulerStateTest.class, SchedulerStateRecoverHelperTest.class, TaskDataTest.class,
                      TopologicalTaskSorterTest.class, TransactionHelperTest.class,

                      // Dataspace tests
                      TaskProActiveDataspacesIntegrationTest.class, TestDataSpaceServiceStarter.class,

                      // scheduler service
                      DatabaseSchemaTest.class, SchedulerDbManagerRecoveryTest.class, SchedulerDBManagerTest.class,
                      SchedulingServiceTest1.class, SchedulingServiceTest2.class, SchedulingServiceTest3.class,
                      SchedulingServiceTest4.class, SchedulingServiceTest5.class, SchedulingServiceTest6.class,
                      SchedulingServiceTest7.class, SchedulingServiceTest8.class, SchedulingServiceTest9.class,
                      SchedulingServiceTest10.class,

                      // misc
                      JettyStarterTest.class, TestXMLTransformer.class, TagTest.class,

                      // RM proxy tests
                      TestRMProxy.class, TestRMProxyRebind.class, TestRMReconnectionWhileRunning.class,

                      // Tests without scheduler restart
                      AuthenticationTest.class, ComplexTypeArgsTest.class, SchedulerJMXTest.class,
                      SchedulerRuntimeDataMBeanTest.class, SchedulerUsageTest.class, TaskUsingCredentialsTest.class,
                      TestCacheSpace.class, TestCleaningScriptMultiNodes.class, TestCleanTaskWorkingDir.class,
                      TestDataSpaceConfiguration.class, TestDataspaceScripts.class, TestDynamicGenericInfo.class,
                      TestErrorAndFailure.class, TestGenericInformation.class, TestGetUsage.class, TestGetUsers.class,
                      TestGlobalSpace.class, TestJobAborted.class, TestJobCanceledWithReplication.class,
                      TestJobCanceledWithReplicationSchema33.class, TestJobCleaningScriptVariables.class,
                      TestJobCoverage.class, TestJobDataspaceSubmission.class, TestJobMultiNodesWalltime.class,
                      TestJobNativeSubmission.class, TestJobNodeAccessToken.class, TestJobRemoved.class,
                      TestJobSchedulerHome.class, TestJobSelScriptSubmission.class, TestJobSelScriptVariables.class,
                      TestJobServerLogs.class, TestJobWalltime.class, TestJobWithInvalidSelectionScript.class,
                      TestKilledJobResult.class, TestLoadJobs.class, TestLoggers.class,
                      TestVariablesPatternLateBindings.class, TestModifyPropagatedVariables.class,
                      TestMultipleHostsRequest.class, TestNativeTaskPaths.class, TestPauseJob.class,
                      TestResumeJob.class, TestPreciousLogs.class, TestPropagatedVariables.class,
                      TestPropagatedVariablesWalltime.class, TestSchedulerMiscEvents.class, TestScriptEngines.class,
                      TestScriptForkEnvironment.class, TestScriptTask.class,
                      TestTaskIdOrderSameAsDeclarationOrder.class, TestTaskNotExecuted.class,
                      TestTaskNotRestarted.class, TestTaskNotStarted.class, TestTaskScriptVariables.class,
                      TestThirdPartyCredentialsDefined.class, TestUserSpace.class, TestWorkflowDataspace.class,
                      TestWorkflowIterationAwareness.class, TestWorkingDirStaticCommand.class,
                      Test_SCHEDULING_2034.class, TestJobSubmittedParallel.class, TestTaskSynchronization.class,
                      TestMarkedAsFinished.class, RestartAllInErrorTasksTest.class, TestJobWhenSchedulerPaused.class,

                      // Tests with scheduler restart
                      JobRecoverTest.class, TestForkedTaskWorkingDir.class, TestKillTaskWhileExecutingScripts.class,
                      TestKillWhenInStoppedState.class, TestSpecialCharacterFileName.class,

                      // Tests in non forked mode without scheduler restart
                      TestNonForkedScriptTask.class, TestNonForkedScriptTaskVariablePropagation.class,
                      TestWorkflowFailedScript.class,

                      // Tests with different configurations (and restart)
                      TestCacheSpaceCleaning.class, TestChildProcessOfNodeKilled.class, TestDisabledListenJobLogs.class,
                      TestExecuteScriptsOnBusyNode.class, TestJobKilled.class, TestJobScriptVariables.class,
                      TestKillPendingTask.class, TestLicensePolicy.class, TestListenJobLogs.class,
                      TestNodeDiesAtSchedulerRestart.class, TestPreemptRestartKillTask.class,
                      TestPreemptRestartKillTaskSchema33.class, TestRamPolicy.class, TestRunAsMeLinuxKey.class,
                      TestRunAsMeLinuxNone.class, TestRunAsMeLinuxPwd.class, TestRunAsMeWindows.class,
                      TestSubmitJobWithPartiallyUnaccessibleDataSpaces.class,
                      TestSubmitJobWithUnaccessibleDataSpaces.class, TestTaskRestartOnNodeFailure.class,
                      TestTasksCompleteAfterSelectiontimeout.class, TestUnauthorizedScripts.class,
                      TestVariablesPropagation.class, EDFPolicyTest.class, EDFPolicyExtendedTest.class })

/**
 * @author ActiveEon Team
 * @since 10/01/2018
 */
public class StandardTestSuite {
}
