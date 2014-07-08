/*
 * ################################################################
 *
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import java.io.File;

import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory_stax;
import org.ow2.tests.FunctionalTest;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.ow2.proactive.utils.FileUtils.createTempDirectory;


public class TestForkedTaskWorkingDir extends FunctionalTest {

    @Test
    public void input_files_are_in_working_dir_for_forked_tasks() throws Throwable {
        scriptTask();
        nativeTask();
    }

    private void scriptTask() throws Exception {
        File input = createTempDirectory("test", ".input_script", null);
        File output = createTempDirectory("test", ".output_script", null);

        FileUtils.touch(new File(input, "inputFile_script.txt"));

        TaskFlowJob job = (TaskFlowJob) JobFactory_stax.getFactory().createJob(
          new File(TestForkedTaskWorkingDir.class
            .getResource("/functionaltests/descriptors/Job_forked_script_task_working_dir.xml").toURI()).getAbsolutePath());

        job.setInputSpace(input.toURI().toString());
        job.setOutputSpace(output.toURI().toString());

        SchedulerTHelper.testJobSubmission(job);

        assertTrue(new File(output, "outputFile_script.txt").exists());
    }

    private void nativeTask() throws Exception {
        if (OperatingSystem.getOperatingSystem() == OperatingSystem.unix) {

            File input = createTempDirectory("test", ".input_native", null);
            File output = createTempDirectory("test", ".output_native", null);

            FileUtils.touch(new File(input, "inputFile_native.txt"));

            TaskFlowJob job = (TaskFlowJob) JobFactory_stax.getFactory().createJob(
              new File(TestForkedTaskWorkingDir.class
                .getResource(
                  "/functionaltests/descriptors/Job_forked_native_task_working_dir.xml").toURI()).getAbsolutePath());

            job.setInputSpace(input.toURI().toString());
            job.setOutputSpace(output.toURI().toString());

            SchedulerTHelper.testJobSubmission(job);

            assertTrue(new File(output, "outputFile_native.txt").exists());
        }
    }

}
