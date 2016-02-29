/*
 * ################################################################
 *
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.dataspaces;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import java.io.File;

import static org.junit.Assert.assertFalse;

public class TestCleanTaskWorkingDir extends SchedulerFunctionalTestNoRestart {

    @Test
    public void input_files_are_in_working_dir_for_forked_tasks() throws Throwable {
        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory().createJob(
                new File(TestCleanTaskWorkingDir.class.getResource(
                        "/functionaltests/descriptors/Job_fill_working_dir_script_task.xml").toURI())
                        .getAbsolutePath());

        JobId id = schedulerHelper.testJobSubmission(job);
        assertFalse(schedulerHelper.getJobResult(id).hadException());

        JobResult jobResult = schedulerHelper.getJobResult(id);
        TaskResult taskResult = jobResult.getResult("fill_working_dir_task");
        String workingDirPath = taskResult.getOutput().getAllLogs(false);
        Assert.assertFalse(new File(workingDirPath).exists());
    }

}
