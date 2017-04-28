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
package org.ow2.proactive.scheduler.core.db.schedulerdb;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.db.SessionWork;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.db.DBTaskDataParameters;
import org.ow2.proactive.scheduler.core.db.TaskDBUtils;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Functional tests related to {@link TaskDBUtils}.
 * <p>
 * Since {@link TaskDBUtils#getTotalNumberOfTasks(DBTaskDataParameters)},
 * {@link TaskDBUtils#taskInfoSessionWork(DBTaskDataParameters)} and
 * {@link TaskDBUtils#taskStateSessionWork(DBTaskDataParameters)} are using the same filtering for the underlying query,
 * most of the tests are written against {@link TaskDBUtils#getTotalNumberOfTasks(DBTaskDataParameters)}. Tests written
 * against other methods are mainly there to check that expected return type is correct.
 *
 * @author ActiveEon Team
 */
public class TaskDBUtilsTest extends BaseSchedulerDBTest {

    private static final Logger log = LoggerFactory.getLogger(TaskDBUtils.class);

    private static final int NB_JOBS = 10;

    private static final int NB_JOBS_AS_DEMO_USER = NB_JOBS / 2; // must be <= NB_JOBS

    private static final int JOB_TO_BE_CROND = 2;

    private static final String DEMO_USERNAME = "demo";

    private static long REFERENCE_TIMESTAMP = System.currentTimeMillis();

    private static final long SCHEDULED_TIME = 42L;

    @Before
    public void setUp() throws Exception {

        /*
         * t refers to REFERENCE_TIMESTAMP in next explanations.
         *
         * Start time and finished time are set respectively
         * as follows per task:
         *
         * t-2 t-1
         * t-3 t-2
         * t-4 t-3
         * .
         * .
         * .
         * t-n-1 t-n
         */
        for (int jobCount = 1; jobCount <= NB_JOBS; jobCount++) {
            String user = DEFAULT_USER_NAME;

            if (jobCount <= NB_JOBS_AS_DEMO_USER) {
                user = DEMO_USERNAME;
            }

            InternalJob job = insertNewJob(user);
            InternalTask task = job.getITasks().get(0);

            job.setStatus(JobStatus.FINISHED);
            job.setNumberOfFinishedTasks(1);
            job.setRemovedTime(-1L);
            task.setStatus(TaskStatus.FINISHED);

            dbManager.updateJobAndTasksState(job);

            long startTime = getTimeRelativeToReference(-jobCount - 1);
            long endTime = getTimeRelativeToReference(-jobCount);

            dbManager.updateStartTime(job.getId().longValue(), task.getId().longValue(), startTime);

            dbManager.updateFinishedTime(job.getId().longValue(), task.getId().longValue(), endTime);

            if (jobCount == JOB_TO_BE_CROND) {
                dbManager.updateScheduledTime(job.getId().longValue(), task.getId().longValue(), SCHEDULED_TIME);
            }

            log.info("Job " + job.getId() + " with task " + task.getId() + " has startTime=" + startTime +
                     " and finishedTime=" + endTime);
        }
    }

    // shift is a number of days
    private long getTimeRelativeToReference(int shift) {
        return REFERENCE_TIMESTAMP + (shift * 86400000);
    }

    @Test
    public void testGetTotalNumberOfTasksFromParameter1() {
        DBTaskDataParameters.Builder builder = DBTaskDataParameters.Builder.create();
        builder.setFrom(getTimeRelativeToReference(-NB_JOBS));

        assertTotalNumberOfTasks(builder.build(), NB_JOBS);
    }

    @Test
    public void testGetTotalNumberOfTasksFromParameter2() {
        DBTaskDataParameters.Builder builder = DBTaskDataParameters.Builder.create();
        builder.setFrom(getTimeRelativeToReference(-NB_JOBS - 1));

        assertTotalNumberOfTasks(builder.build(), NB_JOBS);
    }

    @Test
    public void testGetTotalNumberOfTasksFromParameter3() {
        DBTaskDataParameters.Builder builder = DBTaskDataParameters.Builder.create();
        builder.setFrom(getTimeRelativeToReference(0));

        assertTotalNumberOfTasks(builder.build(), 0);
    }

    @Test
    public void testGetTotalNumberOfTasksFromParameter4() {
        DBTaskDataParameters.Builder builder = DBTaskDataParameters.Builder.create();
        builder.setFrom(getTimeRelativeToReference(-NB_JOBS / 2));

        assertTotalNumberOfTasks(builder.build(), NB_JOBS / 2);
    }

    @Test
    public void testGetTotalNumberOfTasksToParameter1() {
        DBTaskDataParameters.Builder builder = DBTaskDataParameters.Builder.create();
        builder.setTo(getTimeRelativeToReference(0));

        assertTotalNumberOfTasks(builder.build(), NB_JOBS);
    }

    @Test
    public void testGetTotalNumberOfTasksToParameter2() {
        DBTaskDataParameters.Builder builder = DBTaskDataParameters.Builder.create();
        builder.setFrom(getTimeRelativeToReference(-1));

        assertTotalNumberOfTasks(builder.build(), 1);
    }

    @Test
    public void testGetTotalNumberOfTasksToParameter3() {
        DBTaskDataParameters.Builder builder = DBTaskDataParameters.Builder.create();
        builder.setFrom(getTimeRelativeToReference(-NB_JOBS - 1));

        assertTotalNumberOfTasks(builder.build(), NB_JOBS);
    }

    @Test
    public void testGetTotalNumberOfTasksToParameter4() {
        DBTaskDataParameters.Builder builder = DBTaskDataParameters.Builder.create();
        builder.setFrom(getTimeRelativeToReference(-NB_JOBS / 2));

        assertTotalNumberOfTasks(builder.build(), NB_JOBS / 2);
    }

    @Test
    public void testGetTotalNumberOfTasksFromAndToParameter() {
        DBTaskDataParameters.Builder builder = DBTaskDataParameters.Builder.create();
        builder.setFrom(getTimeRelativeToReference((int) (-NB_JOBS * 0.25)));
        builder.setTo(getTimeRelativeToReference(0));

        assertTotalNumberOfTasks(builder.build(), NB_JOBS / 4);
    }

    @Test
    public void testGetTotalNumberOfTasksUserParameter() {
        DBTaskDataParameters.Builder builder = DBTaskDataParameters.Builder.create();
        builder.setUser(DEMO_USERNAME);

        assertTotalNumberOfTasks(builder.build(), NB_JOBS_AS_DEMO_USER);
    }

    @Test
    public void testGetTotalNumberOfTasksPendingRunningNotFinished() {
        DBTaskDataParameters.Builder builder = DBTaskDataParameters.Builder.create();
        builder.setPending(true);
        builder.setRunning(true);
        builder.setFinished(false);

        assertTotalNumberOfTasks(builder.build(), 0);
    }

    @Test
    public void testGetTotalNumberOfTasksLimitZeroOffsetZero() {
        DBTaskDataParameters.Builder builder = DBTaskDataParameters.Builder.create();
        builder.setLimit(0);
        builder.setLimit(0);

        assertTotalNumberOfTasks(builder.build(), NB_JOBS);
    }

    @Test
    public void testTaskInfoSessionWork() {
        DBTaskDataParameters.Builder builder = DBTaskDataParameters.Builder.create();
        builder.setOffset(2);
        builder.setLimit(NB_JOBS - 1);

        SessionWork<List<TaskInfo>> taskInfoSessionWork = TaskDBUtils.taskInfoSessionWork(builder.build());

        List<TaskInfo> result = run(taskInfoSessionWork);
        assertThat(result).hasSize(NB_JOBS - 2);
    }

    @Test
    public void testTaskStateSessionWork() {
        DBTaskDataParameters.Builder builder = DBTaskDataParameters.Builder.create();
        builder.setOffset(2);
        builder.setLimit(NB_JOBS - 1);

        SessionWork<List<TaskState>> taskStateSessionWork = TaskDBUtils.taskStateSessionWork(builder.build());

        List<TaskState> result = run(taskStateSessionWork);
        assertThat(result).hasSize(NB_JOBS - 2);
    }

    @Test
    public void testGetScheduledTasks() {
        DBTaskDataParameters.Builder builder = DBTaskDataParameters.Builder.create();
        builder.setFrom(SCHEDULED_TIME);
        builder.setTo(SCHEDULED_TIME);
        SessionWork<List<TaskState>> taskStateSessionWork = TaskDBUtils.taskStateSessionWork(builder.build());
        List<TaskState> result = run(taskStateSessionWork);
        assertThat(result).hasSize(1);
    }

    private void assertTotalNumberOfTasks(DBTaskDataParameters parameters, int expectedNumberOfTasks) {
        SessionWork<Integer> totalNumberOfTasks = TaskDBUtils.getTotalNumberOfTasks(parameters);

        assertThat(run(totalNumberOfTasks)).isEqualTo(expectedNumberOfTasks);
    }

    private <T> T run(SessionWork<T> sessionWork) {
        return dbManager.executeReadWriteTransaction(sessionWork);
    }

    public InternalJob insertNewJob(String user) throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();

        JavaTask javaTask = createDefaultTask("java task");
        javaTask.setExecutableClassName(TestDummyExecutable.class.getName());
        jobDef.addTask(javaTask);

        return defaultSubmitJob(jobDef, user);
    }

}
