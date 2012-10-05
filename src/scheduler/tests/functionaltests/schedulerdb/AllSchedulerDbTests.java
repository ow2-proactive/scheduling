package functionaltests.schedulerdb;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@Ignore
@RunWith(Suite.class)
@SuiteClasses( { TestDataspaceSelectorsData.class, TestForkedJavaTaskData.class,
        TestInMemorySchedulerDB.class, TestJavaTaskData.class, TestJobAttributes.class,
        TestJobClasspath.class, TestJobRemove.class, TestJobRuntimeData.class, TestLoadJobResut.class,
        TestLoadSchedulerClientState.class, TestMultipleTasks.class, TestNativeTaskData.class,
        TestReadSchedulerAccount.class, TestReportingQueries.class, TestSchedulerTasksStateRecover.class,
        TestJobOperations.class, TestTaskAttributes.class, TestTaskIdGeneration.class,
        TestTaskResultData.class, TestTaskRuntimeData.class, TestRestoreWorkflowJobs.class,
        TestRestoreWorkflowJobs2.class })
public class AllSchedulerDbTests {

}
