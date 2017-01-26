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
package functionaltests.runasme;

import functionaltests.dataspaces.TestCacheSpaceCleaning;
import functionaltests.utils.RMTHelper;
import functionaltests.utils.SchedulerTHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.task.utils.ForkerUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assume.assumeTrue;


/**
 * Tests RunAsMe feature on linux configured with sudo impersonation
 *
 * @author ActiveEon Team
 * @since 05/01/2017
 */
public class TestRunAsMeLinuxNone extends TestRunAsMe {

    static URL jobDescriptor = TestCacheSpaceCleaning.class.getResource("/functionaltests/descriptors/Job_RunAsMe_Linux.xml");

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        assumeTrue(OperatingSystem.getOperatingSystem() == OperatingSystem.unix);

        setupUser();

        RMFactory.setOsJavaProperty();
        // start an empty scheduler and add a node source with modified properties
        schedulerHelper = new SchedulerTHelper(true, true);
        List<String> arguments = new ArrayList<>();
        arguments.addAll(RMTHelper.setup.getJvmParametersAsList());
        arguments.add("-D" + ForkerUtils.FORK_METHOD_KEY + "=" + ForkerUtils.ForkMethod.NONE.toString());

        schedulerHelper.createNodeSource("RunAsMeNSNone", 5, arguments);
    }

    @Test
    public void testRunAsMe() throws Exception {
        // connect to the scheduler using the runasme account
        Scheduler scheduler = schedulerHelper.getSchedulerInterface(username, password, null);
        JobId jobid = schedulerHelper.testJobSubmission(scheduler,
                                                        JobFactory.getFactory()
                                                                  .createJob(new File(jobDescriptor.toURI()).getAbsolutePath()),
                                                        false,
                                                        true);

        for (Map.Entry<String, TaskResult> entry : scheduler.getJobResult(jobid).getAllResults().entrySet()) {
            if (entry.getKey().contains("RunAsMeTask")) {
                Assert.assertTrue("RunAsMe task should display in the logs the correct system user",
                                  entry.getValue().getOutput().getStdoutLogs().contains(username));
            }
        }
    }
}
