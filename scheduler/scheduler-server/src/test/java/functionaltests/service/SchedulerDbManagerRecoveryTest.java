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
package functionaltests.service;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.HsqldbServer;
import org.ow2.tests.ProActiveTest;

import functionaltests.utils.Jobs;


/**
 * The purpose of this class is to check that jobs' states are persisted in a database
 * as they should and that all states can be reloaded properly without loosing some.
 * <p>
 * The database is closed in a clean manner since the goal is not to check
 * the transaction feature of the database itself neither its durability property.
 *
 * @author ActiveEon Team
 */
public class SchedulerDbManagerRecoveryTest extends ProActiveTest {

    @Rule
    public TemporaryFolder dbFolder = new TemporaryFolder();

    private SchedulerDBManager dbManager;

    @Before
    public void setUp() throws Exception {
        dbManager = createDatabase(true);
    }

    @Test
    public void testJobRecoveryWithIf() throws Exception {
        testJobRecovery("recovery_if.xml");
    }

    @Test
    public void testJobRecoveryWithFor() throws Exception {
        testJobRecovery("recovery_for.xml");
    }

    /**
     * Regression test related to issue #1988:
     * <p>
     * https://github.com/ow2-proactive/scheduling/issues/1988
     */
    @Test
    public void testJobRecoveryWithIfInALoop() throws Exception {
        InternalJob reloadedJob = testJobRecovery("flow_if_in_a_loop.xml");

        // joined branches should be saved and restored
        InternalTask task = reloadedJob.getTask("Loop");

        assertThat(task).isNotNull();
        assertThat(task.getJoinedBranches()).isNotEmpty();
    }

    @Test
    public void testJobRecoveryWithReplicate() throws Exception {
        testJobRecovery("recovery_replicate.xml");
    }

    @Test
    public void testJobRecoveryWithReplicate2() throws Exception {
        testJobRecovery("recovery_replicate2.xml");
    }

    @Test
    public void testJobRecoveryWithPendingJob() throws Exception {
        testJobRecovery("recovery_pending_job.xml");
    }

    @Test
    public void testJobRecoveryWithSeveralTasks() throws Exception {
        testJobRecovery("recovery_tasks.xml");
    }

    private InternalJob testJobRecovery(String workflowFilename) throws Exception {
        Job job = Jobs.parseXml(this.getClass()
                                    .getResource("/functionaltests/workflow/descriptors/" + workflowFilename)
                                    .getPath());

        InternalJob submittedJob = Jobs.createJob(job);
        dbManager.newJobSubmitted(submittedJob);

        closeAndRestartDatabase();

        List<InternalJob> internalJobs = dbManager.loadJobs(true, submittedJob.getId());

        assertThat(internalJobs).hasSize(1);

        InternalJob loadedJob = internalJobs.get(0);
        loadedJob.equals(submittedJob);

        assertThat(Jobs.areEqual(submittedJob, loadedJob)).isTrue();

        return loadedJob;
    }

    /**
     * Regression test related to issue #1849.
     * <p>
     * https://github.com/ow2-proactive/scheduling/issues/1849
     */
    @Test
    public void testLoadNotFinishedForkEnvironment() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask task1 = new JavaTask();
        task1.setName("task1");
        task1.setExecutableClassName("MyClass");

        JavaTask task2 = new JavaTask();
        task2.setName("task2");
        task2.setExecutableClassName("MyClass");

        task2.addDependence(task1);
        job.addTask(task1);
        job.addTask(task2);

        ForkEnvironment forkEnvironment = new ForkEnvironment();
        forkEnvironment.addAdditionalClasspath("$USERSPACE/test.jar");

        for (Task task : job.getTasks()) {
            task.setForkEnvironment(forkEnvironment);
        }

        dbManager.newJobSubmitted(Jobs.createJob(job));

        closeAndRestartDatabase();

        List<InternalJob> internalJobs = dbManager.loadNotFinishedJobs(true);

        assertThat(internalJobs).hasSize(1);

        for (InternalTask internalTask : internalJobs.get(0).getITasks()) {
            List<String> additionalClasspath = internalTask.getForkEnvironment().getAdditionalClasspath();

            assertThat(additionalClasspath).hasSize(1);
            assertThat(additionalClasspath.get(0)).isEqualTo("$USERSPACE/test.jar");
        }
    }

    @After
    public void tearDown() {
        dbManager.close();
    }

    private void closeAndRestartDatabase() throws Exception {
        dbManager.close();
        dbManager = createDatabase(false);
    }

    private SchedulerDBManager createDatabase(boolean wipeOnStartup) throws URISyntaxException {
        String configureFilename = "hibernate-update.cfg.xml";

        if (wipeOnStartup) {
            configureFilename = "hibernate.cfg.xml";
        }

        Configuration config = new Configuration().configure(new File(this.getClass()
                                                                          .getResource("/functionaltests/config/" +
                                                                                       configureFilename)
                                                                          .toURI()));

        if (config.getProperty("hibernate.connection.url").contains(HsqldbServer.HSQLDB)) {
            String jdbcUrl = "jdbc:hsqldb:file:" + dbFolder.getRoot().getAbsolutePath() +
                             ";create=true;hsqldb.tx=mvcc;hsqldb.write_delay=false";

            config.setProperty("hibernate.connection.url", jdbcUrl);
        }

        return new SchedulerDBManager(config, wipeOnStartup);
    }

}
