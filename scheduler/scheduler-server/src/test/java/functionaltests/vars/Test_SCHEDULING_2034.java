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
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package functionaltests.vars;

import static functionaltests.SchedulerTHelper.testJobSubmissionAndVerifyAllResults;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;

import functionaltests.SchedulerConsecutive;
import functionaltests.SchedulerTHelper;
import functionaltests.executables.PropagateVariablesExec;

public class Test_SCHEDULING_2034 extends SchedulerConsecutive {

    private static URL job_desc_unix = TestModifyPropagatedVariables.class
            .getResource("/functionaltests/descriptors/Job_SCHEUDLING_2034_unix.xml");

    @Test
    public void run() throws Throwable {
        if (OperatingSystem.unix == OperatingSystem.getOperatingSystem()) {
            SchedulerTHelper.testJobSubmissionAndVerifyAllResults(absolutePath(job_desc_unix));
        }
    }

    private String absolutePath(URL file) throws Exception {
        return ((new File(file.toURI())).getAbsolutePath());
    }
}
