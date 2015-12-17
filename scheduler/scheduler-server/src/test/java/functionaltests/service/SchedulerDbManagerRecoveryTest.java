/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.service;

import functionaltests.utils.Jobs;
import org.hibernate.cfg.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.tests.ProActiveTest;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class SchedulerDbManagerRecoveryTest extends ProActiveTest {

    private SchedulerDBManager dbManager;

    @Before
    public void setUp() throws Exception {
        dbManager = createDatabase(true);
    }

    /**
     * Related to issue #1849.
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

    private void closeAndRestartDatabase() throws Exception {
        dbManager.close();
        dbManager = createDatabase(false);
    }

    private SchedulerDBManager createDatabase(boolean wipeOnStartup) throws URISyntaxException {
        String configureFilename = "hibernate-update.cfg.xml";

        if (wipeOnStartup) {
            configureFilename = "hibernate.cfg.xml";
        }

        Configuration config =
                new Configuration().configure(
                        new File(this.getClass().getResource("/functionaltests/config/" + configureFilename).toURI()));

        return new SchedulerDBManager(config, wipeOnStartup);
    }

}
