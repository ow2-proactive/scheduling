package org.ow2.proactive.scheduler.common.job;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.tests.ProActiveTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Expected behaviour on paginated methods:
 *  - when an index is invalid, it is replaced by a default value
 *    (0 for the offset, MAX_PAGE_SIZE for the limit)
 *  - when the limit is beyond the actual size of the tasks list,
 *    all tasks are retrieved.
 *
 * @author ActiveEon Team
 */
public class JobStateTest extends ProActiveTest {

    private JobState jobState;

    private static final String TAG = "test";

    private static final int NB_TASKS = 10; // must be > 3

    private static final int NB_TASKS_WITH_TAG = NB_TASKS / 2;

    private static final int DEFAULT_TASKS_PAGE_SIZE = 100;

    @Before
    public void setUp() throws Exception {
        PASchedulerProperties.TASKS_PAGE_SIZE.updateProperty(Integer.toString(DEFAULT_TASKS_PAGE_SIZE));

        jobState = new JobState() {

            private static final long serialVersionUID = 1L;

            @Override
            public ArrayList<TaskState> getTasks() {
                ArrayList<TaskState> list = new ArrayList<>(NB_TASKS);
                for (int i = 0; i < NB_TASKS; i++) {
                    final TaskState mock = mock(TaskState.class);
                    list.add(mock);

                    if (i < NB_TASKS_WITH_TAG) {
                        when(mock.getTag()).thenReturn(TAG);
                    }
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

    @Test
    public void testGetTasksPaginated() throws Exception {
        assertThat(getTaskStates(0, 10)).hasSize(NB_TASKS);
        assertThat(getTaskStates(0, 50)).hasSize(NB_TASKS);
        assertThat(getTaskStates(0, 5000000)).hasSize(NB_TASKS);
        assertThat(getTaskStates(-1000, 50)).hasSize(NB_TASKS);
        assertThat(getTaskStates(1, -1)).hasSize(NB_TASKS - 1);
        assertThat(getTaskStates(0, 0)).hasSize(NB_TASKS);
        assertThat(getTaskStates(0, 1)).hasSize(1);
        assertThat(getTaskStates(0, 2)).hasSize(2);
        assertThat(getTaskStates(0, 3)).hasSize(3);
        assertThat(getTaskStates(50, 5000)).hasSize(NB_TASKS);
    }

    private List<TaskState> getTaskStates(int offset, int limit) {
        return jobState.getTasksPaginated(offset, limit).getTaskStates();
    }

    @Test
    public void testGetTaskByTagPaginated() {
        assertThat(getTaskStatesByTag(0, 10)).hasSize(NB_TASKS_WITH_TAG);
        assertThat(getTaskStatesByTag(0, 50)).hasSize(NB_TASKS_WITH_TAG);
        assertThat(getTaskStatesByTag(0, 5000000)).hasSize(NB_TASKS_WITH_TAG);
        assertThat(getTaskStatesByTag(-1000, 50)).hasSize(NB_TASKS_WITH_TAG);
        assertThat(getTaskStatesByTag(1, -1)).hasSize(NB_TASKS_WITH_TAG - 1);
        assertThat(getTaskStatesByTag(0, 0)).hasSize(NB_TASKS_WITH_TAG);
        assertThat(getTaskStatesByTag(0, 1)).hasSize(1);
        assertThat(getTaskStatesByTag(0, 2)).hasSize(2);
        assertThat(getTaskStatesByTag(0, 3)).hasSize(3);
        assertThat(getTaskStatesByTag(50, 5000)).hasSize(NB_TASKS_WITH_TAG);
    }

    private List<TaskState> getTaskStatesByTag(int offset, int limit) {
        return jobState.getTaskByTagPaginated(TAG, offset, limit).getTaskStates();
    }

}
