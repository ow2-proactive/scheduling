package org.ow2.proactive.scheduler.common.job;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.tests.ProActiveTest;


/**
 * Expected behavior on paginated methods:
 *  - when an index is invalid, it is replaced by a default value
 *    (0 for the offset, MAX_PAGE_SIZE for the limit)
 *  - when the limit is beyond the actual size of the tasks list,
 *    all tasks are retrieved.
 * @author paraita
 *
 */
public class JobStateTest extends ProActiveTest {

    private JobState jobState;

    @Before
    public void setUp() throws Exception {
        jobState = new JobState() {

            private static final long serialVersionUID = 1L;

            @Override
            public ArrayList<TaskState> getTasks() {
                ArrayList<TaskState> list = new ArrayList<TaskState>();
                for (int i = 0; i < 10; i++) {
                    list.add(mock(TaskState.class));
                }
                return list;
            }

            @Override
            public void update(TaskInfo info) {
            }

            @Override
            public void update(JobInfo jobInfo) {
            }

            @Override
            public JobInfo getJobInfo() {
                return null;
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

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetTasksPaginated() throws Exception {
        assertEquals(10, jobState.getTasksPaginated(0, 10).size());
        assertEquals(10, jobState.getTasksPaginated(0, 50).size());
        assertEquals(10, jobState.getTasksPaginated(0, 5000000).size());
        assertEquals(10, jobState.getTasksPaginated(-1000, 50).size());
        assertEquals(9, jobState.getTasksPaginated(1, -1).size());
        assertEquals(10, jobState.getTasksPaginated(0, 0).size());
        assertEquals(1, jobState.getTasksPaginated(0, 1).size());
        assertEquals(2, jobState.getTasksPaginated(0, 2).size());
        assertEquals(3, jobState.getTasksPaginated(0, 3).size());
        assertEquals(10, jobState.getTasksPaginated(50, 5000).size());
    }

    @Test
    public void testGetTaskByTagPaginated() {
        assertEquals(10, jobState.getTasksPaginated(0, 10).size());
        assertEquals(10, jobState.getTasksPaginated(0, 50).size());
        assertEquals(10, jobState.getTasksPaginated(0, 5000000).size());
        assertEquals(10, jobState.getTasksPaginated(-1000, 50).size());
        assertEquals(9, jobState.getTasksPaginated(1, -1).size());
        assertEquals(10, jobState.getTasksPaginated(0, 0).size());
        assertEquals(1, jobState.getTasksPaginated(0, 1).size());
        assertEquals(2, jobState.getTasksPaginated(0, 2).size());
        assertEquals(3, jobState.getTasksPaginated(0, 3).size());
        assertEquals(10, jobState.getTasksPaginated(50, 5000).size());
    }

}
