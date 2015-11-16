/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package functionaltests.scripts;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import functionaltests.SchedulerConsecutive;
import functionaltests.SchedulerTHelper;
import org.junit.Assert;


/**
 * TestScriptEngines
 *
 * This test checks that scripts from all used script engines can be executed
 *
 * @author The ProActive Team
 */
public class TestScriptEngines extends SchedulerConsecutive {

    private static String jobDescriptorsLoc = "JobScriptEngines.xml";

    private URL jobDescriptor = TestScriptEngines.class.getResource("/functionaltests/scripts/" +
        jobDescriptorsLoc);

    @org.junit.Test
    public void action() throws Throwable {
        logger.info("Testing submission of job descriptor : " + jobDescriptor);
        JobId id = SchedulerTHelper.testJobSubmission(new File(jobDescriptor.toURI()).getAbsolutePath());

        // check result are not null
        JobResult res = SchedulerTHelper.getJobResult(id);
        Assert.assertFalse("Had Exception : " + jobDescriptor.toString(), SchedulerTHelper.getJobResult(id)
                .hadException());

        Assert.assertEquals(4, res.getAllResults().size());
        for (Map.Entry<String, TaskResult> entry : res.getAllResults().entrySet()) {

            Assert.assertFalse("Had Exception : " + entry.getKey(), entry.getValue().hadException());

            Assert.assertNotNull("Result not null : " + entry.getKey(), entry.getValue().value());

            Assert.assertTrue("Hello World in Output : " + entry.getKey(), entry.getValue().getOutput()
                    .getAllLogs(false).contains("Hello World"));
        }

        SchedulerTHelper.removeJob(id);
        SchedulerTHelper.waitForEventJobRemoved(id);
    }
}
