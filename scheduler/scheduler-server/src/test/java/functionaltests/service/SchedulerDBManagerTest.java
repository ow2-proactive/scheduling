package functionaltests.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Page;
import org.ow2.proactive.scheduler.common.TaskFilterCriteria;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
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
        TaskFilterCriteria crit = null;
        List<InternalJob> lJobs = createTestJobs("TestGetJobs-Job", "TEST-TAG", nbJobs, nbTasksPerJob);

        List<Task> lAllTasks = new ArrayList<Task>(totalNbTasks);
        for (InternalJob j : lJobs) {
            service.submitJob(j);
            lAllTasks.addAll(j.getTasks());
        }
        assertEquals("Total number of tasks is incorrect", totalNbTasks, lAllTasks.size());
        
        // no given statuses, no pagination, nothing should come up
        crit = TaskFilterCriteria.TFCBuilder.newInstance().running(false).pending(false).finished(false).criterias();
        actualPage = getTasks(crit);
        assertSizes(actualPage, 0, 0);
        
        // all statuses, no pagination, everything should come up
        crit = TaskFilterCriteria.TFCBuilder.newInstance().criterias();
        actualPage = getTasks(crit);
        assertSizes(actualPage, totalNbTasks, totalNbTasks);

        // all statuses, pagination [0,5[
        crit = TaskFilterCriteria.TFCBuilder.newInstance().limit(5).criterias();
        actualPage = getTasks(crit);
        assertSizes(actualPage, 5, totalNbTasks);
        
        // pending tasks only, no pagination
        crit = TaskFilterCriteria.TFCBuilder.newInstance().running(false).finished(false).criterias();
        actualPage = getTasks(crit);
        assertSizes(actualPage, totalNbTasks, totalNbTasks);
        
        // running tasks only, no pagination
        InternalJob startedJob = lJobs.get(0);
        startedJob.start();
        for (InternalTask task : startedJob.getITasks()) {
            task.setStatus(TaskStatus.RUNNING);
        }
        dbManager.updateJobAndTasksState(startedJob);
        crit = TaskFilterCriteria.TFCBuilder.newInstance().pending(false).finished(false).criterias();
        actualPage = getTasks(crit);
        assertSizes(actualPage, nbTasksPerJob, nbTasksPerJob);
        
        // running tasks only, pagination [0,5[
        crit = TaskFilterCriteria.TFCBuilder.newInstance().limit(5).pending(false).finished(false).criterias();
        actualPage = getTasks(crit);
        assertSizes(actualPage, 5, nbTasksPerJob);
        
        // finished tasks only, no pagination
        InternalJob finishedJob = lJobs.get(1);
        finishedJob.start();
        for (InternalTask task : finishedJob.getITasks()) {
            task.setStatus(TaskStatus.FINISHED);
        }
        dbManager.updateJobAndTasksState(finishedJob);
        crit = TaskFilterCriteria.TFCBuilder.newInstance().pending(false).running(false).criterias();
        actualPage = getTasks(crit);
        //actualPage = dbManager.getTasks(0, 0, null, 0, 0, null, false, true, false, null);
        assertSizes(actualPage, nbTasksPerJob, nbTasksPerJob);
        
        // finished tasks only, pagination [0,5[
        crit = TaskFilterCriteria.TFCBuilder.newInstance().limit(5).pending(false).running(false).criterias();
        actualPage = getTasks(crit);
        assertSizes(actualPage, 5, nbTasksPerJob);
        
    }
    
    @Test
    @Ignore
    public void testGetTaskStates() throws Throwable {
        // default criteria
        
        // all statuses, no pagination, everything should come up
        
        // only pending 
        
        // 
        
        // 
        
        
        
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

    private Page<TaskInfo> getTasks(TaskFilterCriteria tfc) {
        return dbManager.getTasks(tfc.getFrom(), tfc.getTo(), tfc.getTag(),
                tfc.getOffset(), tfc.getLimit(), tfc.getUser(), tfc.isPending(),
                tfc.isRunning(), tfc.isFinished(), tfc.getSortParameters());
    }
    
    // TODO PARAITA 
    private Page<TaskState> getTaskStates(TaskFilterCriteria criterias) {
        return null;
    }
    
    private void assertSizes(Page<?> page, int nbTasksInPage, int totalNbTasks) {
        assertEquals("Returned number of tasks is incorrect", nbTasksInPage, page.getList().size());
        assertEquals("Total number of tasks is incorrect", totalNbTasks, page.getSize());
    }
    
    // TODO PARAITA
    private void assertTaskInfoPage(Page<TaskInfo> page, TaskFilterCriteria criterias) {
        
    }
    
    // TODO PARAITA
    private void assertTaskStatePage(Page<TaskState> page, TaskFilterCriteria criterias) {
        
    }
    

    
    
}
