package org.ow2.proactive.scheduler.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;


public class ClientJobStateTest {

    @BeforeClass
    public static void configureLogger() throws Exception {
        BasicConfigurator.configure(new NullAppender());
    }

    @Test
    public void taskUpdate_ShouldUpdate_NumberOfTasksInJobInfo() throws Exception {
        JobInfoImpl jobInfo = createJobInfo();
        ClientJobState jobState = new ClientJobState(createJobState(jobInfo));

        jobInfo.setNumberOfFinishedTasks(3);
        jobInfo.setNumberOfPendingTasks(2);
        jobInfo.setNumberOfRunningTasks(1);
        TaskInfoImpl updatedTask = createTaskInfo(jobInfo);

        jobState.update(updatedTask);

        assertEquals(1, jobState.getJobInfo().getNumberOfRunningTasks());
        assertEquals(2, jobState.getJobInfo().getNumberOfPendingTasks());
        assertEquals(3, jobState.getJobInfo().getNumberOfFinishedTasks());
    }

    private JobInfoImpl createJobInfo() {
        JobInfoImpl jobInfo = new JobInfoImpl();
        JobIdImpl jobId = new JobIdImpl(1000, "job");
        jobInfo.setJobId(jobId);
        return jobInfo;
    }

    private TaskInfoImpl createTaskInfo(JobInfoImpl jobInfo) {
        TaskInfoImpl updatedTask = new TaskInfoImpl();

        updatedTask.setJobInfo(jobInfo);
        updatedTask.setTaskId(TaskIdImpl.createTaskId(jobInfo.getJobId(), "task", 1));
        return updatedTask;
    }

    private JobState createJobState(final JobInfoImpl jobInfo) {
        return new JobState() {
            @Override
            public void update(TaskInfo info) {

            }

            @Override
            public void update(JobInfo jobInfo) {

            }

            @Override
            public JobInfo getJobInfo() {
                return jobInfo;
            }

            @Override
            public ArrayList<TaskState> getTasks() {
                ArrayList<TaskState> tasks = new ArrayList<>();
                tasks.add(new TaskState() {
                    @Override
                    public void update(TaskInfo taskInfo) {

                    }

                    @Override
                    public List<TaskState> getDependences() {
                        return null;
                    }

                    @Override
                    public TaskInfo getTaskInfo() {
                        TaskInfoImpl taskInfo = new TaskInfoImpl();
                        taskInfo.setJobInfo(jobInfo);
                        taskInfo.setTaskId(TaskIdImpl.createTaskId(jobInfo.getJobId(), "task", 1));
                        return taskInfo;
                    }

                    @Override
                    public int getMaxNumberOfExecutionOnFailure() {
                        return 0;
                    }

                    @Override
                    public TaskState replicate() throws Exception {
                        return null;
                    }

                    @Override
                    public int getIterationIndex() {
                        return 0;
                    }

                    @Override
                    public int getReplicationIndex() {
                        return 0;
                    }
                });
                return tasks;
            }

            @Override
            public Map<TaskId, TaskState> getHMTasks() {
                return null;
            }

            @Override
            public String getOwner() {
                return null;
            }

            @Override
            public JobType getType() {
                return null;
            }
        };
    }
}