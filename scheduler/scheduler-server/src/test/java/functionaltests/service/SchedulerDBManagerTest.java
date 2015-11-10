package functionaltests.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Page;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

/**
 * Functional tests to verify tasks pagination and criteria searches
 *
 */
public class SchedulerDBManagerTest extends BaseServiceTest {
    

    
    @Test
    public void testGetTasks() throws Throwable {

        int nbJobs = 10;
        int nbTasksPerJob = 20; // we need  nbTasksPerJob > page size
        int totalNbTasks = nbJobs * nbTasksPerJob;
        Page<TaskInfo> actualPage = null;
        List<InternalJob> lJobs = createTestJobs("TestGetJobs-Job", "TEST-TAG", nbJobs, nbTasksPerJob);

        List<Task> lAllTasks = new ArrayList<Task>();
        for (InternalJob j : lJobs) {
            service.submitJob(j);
            lAllTasks.addAll(j.getTasks());
        }
        assertEquals("Total number of tasks is incorrect", totalNbTasks, lAllTasks.size());

        // -------------------- pagination --------------------

        // no given statuses, no pagination, nothing should come up
        actualPage = dbManager.getTasks(0, 0, null, 0, 0, null, false, false, false, null);
        assertEquals("Returned number of tasks is incorrect", 0, actualPage.getList().size());
        assertEquals("Total number of tasks is incorrect", 0, actualPage.getSize());

        // all statuses, no pagination, everything should come up
        actualPage = dbManager.getTasks(0, 0, null, 0, 0, null, true, true, true, null);
        assertEquals("Returned number of tasks is incorrect", totalNbTasks, actualPage.getList().size());
        assertEquals("Total number of tasks is incorrect", totalNbTasks, actualPage.getSize());

        // all statuses, pagination [0,5[
        actualPage = dbManager.getTasks(0, 0, null, 0, 5, null, true, true, true, null);
        assertEquals("Returned number of tasks is incorrect", 5, actualPage.getList().size());
        assertEquals("Total number of tasks is incorrect", totalNbTasks, actualPage.getSize());

        // pending tasks only, no pagination
        actualPage = dbManager.getTasks(0, 0, null, 0, 0, null, true, false, false, null);
        assertEquals("Returned number of tasks is incorrect", totalNbTasks, actualPage.getList().size());
        assertEquals("Total number of tasks is incorrect", totalNbTasks, actualPage.getSize());
        
        // running tasks only, no pagination
        InternalJob startedJob = lJobs.get(0);
        startedJob.start();
        for (InternalTask task : startedJob.getITasks()) {
            task.setStatus(TaskStatus.RUNNING);
        }
        dbManager.updateJobAndTasksState(startedJob);
        actualPage = dbManager.getTasks(0, 0, null, 0, 0, null, false, true, false, null);
        assertEquals("Returned number of tasks is incorrect", nbTasksPerJob, actualPage.getList().size());
        assertEquals("Total number of tasks is incorrect", nbTasksPerJob, actualPage.getSize());
        
        // running tasks only, pagination [0,5[
        actualPage = dbManager.getTasks(0, 0, null, 0, 5, null, false, true, false, null);
        assertEquals("Returned number of tasks is incorrect", 5, actualPage.getList().size());
        assertEquals("Total number of tasks is incorrect", nbTasksPerJob, actualPage.getSize());
        
        // finished tasks only, no pagination
        InternalJob finishedJob = lJobs.get(1);
        finishedJob.start();
        for (InternalTask task : finishedJob.getITasks()) {
            task.setStatus(TaskStatus.FINISHED);
        }
        dbManager.updateJobAndTasksState(finishedJob);
        actualPage = dbManager.getTasks(0, 0, null, 0, 0, null, false, true, false, null);
        assertEquals("Returned number of tasks is incorrect", nbTasksPerJob, actualPage.getList().size());
        assertEquals("Total number of tasks is incorrect", nbTasksPerJob, actualPage.getSize());
        
        // finished tasks only, pagination [0,5[
        actualPage = dbManager.getTasks(0, 0, null, 0, 5, null, false, true, false, null);
        assertEquals("Returned number of tasks is incorrect", 5, actualPage.getList().size());
        assertEquals("Total number of tasks is incorrect", nbTasksPerJob, actualPage.getSize());
        
        
    }
    
    @Test
    @Ignore
    public void testGetTaskStates() throws Throwable {
        // TODO 
    }

    @Test
    @Ignore
    public void testgetTotalJobsCount() throws Throwable {
        // TODO 
    }

    @Test
    @Ignore
    public void testGetTotalTasksCount() throws Throwable {
        // TODO 
    }
    
    private List<InternalJob> createTestJobs(String jobName, String taskTag, int nbJobs, int nbTasks)
            throws Exception {

        List<InternalJob> lJobs = new ArrayList<InternalJob>(nbJobs);

        for (int i = 1; i <= nbJobs; i++) {
            lJobs.add(createTestJob(jobName + "-" + i, taskTag + "-" + i, nbTasks));
        }

        return lJobs;
    }

    private InternalJob createTestJob(String jobName, String taskTag, int nbTasks) throws Exception {

        TaskFlowJob job = new TaskFlowJob();
        job.setName(jobName);

        for (int i = 1; i <= nbTasks; i++) {
            JavaTask task = new JavaTask();
            task.setName(jobName + "-TASK-" + i + "/" + nbTasks);
            task.setExecutableClassName("class");
            task.setTag(taskTag);
            job.addTask(task);
        }

        return createJob(job);
    }

}
