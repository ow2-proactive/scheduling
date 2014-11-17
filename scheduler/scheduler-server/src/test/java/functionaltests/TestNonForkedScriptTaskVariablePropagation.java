/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package functionaltests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory_stax;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.tests.FunctionalTest;

public class TestNonForkedScriptTaskVariablePropagation extends FunctionalTest {
    private static final long five_minutes = 5 * 60 * 1000;

    private String configPath;
    private String jobDescPath;

    @Before
    public void setup() throws Exception {
        configPath = absolutePath(SchedulerTHelper.class
                .getResource("config/scheduler-nonforkedscripttasks.ini"));
        jobDescPath = absolutePath(TestNonForkedScriptTaskVariablePropagation.class
                .getResource("/functionaltests/descriptors/Job_variable_propagation_with_non_forked_script_task.xml"));
        SchedulerTHelper.startScheduler(configPath);
    }

    @Test(timeout = five_minutes)
    public void propagateVariables_byNonForkedScriptTask_shouldSucceed() throws Throwable {
        SchedulerTHelper.testJobSubmissionAndVerifyAllResults(jobDescPath);
    }

    private String absolutePath(URL url) throws Exception {
        return (new File(url.toURI())).getAbsolutePath();
    }
}
