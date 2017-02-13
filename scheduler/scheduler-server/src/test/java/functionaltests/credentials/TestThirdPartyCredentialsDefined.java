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
package functionaltests.credentials;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


public class TestThirdPartyCredentialsDefined extends SchedulerFunctionalTestNoRestart {

    @Test
    public void testThatCredentialsAreCreatedIfThirdPartyCredentialHaveBeenDefined() throws Exception {
        schedulerHelper.getSchedulerInterface().putThirdPartyCredential("mykey", "myvalue");
        assertEquals("{mykey: myvalue}", createAndSubmitTaskPrintingCredentials());
    }

    public String createAndSubmitTaskPrintingCredentials() throws Exception {
        ScriptTask scriptTask = new ScriptTask();
        scriptTask.setName("task");
        scriptTask.setScript(new TaskScript(new SimpleScript("print credentials", "python")));

        TaskFlowJob job = new TaskFlowJob();
        job.addTask(scriptTask);

        JobId id = schedulerHelper.submitJob(job);

        schedulerHelper.waitForEventJobFinished(id);

        JobResult jobResult = schedulerHelper.getJobResult(id);

        TaskResult result = jobResult.getResult(scriptTask.getName());

        return result.getOutput().getStdoutLogs(false).replaceAll("\n|\r", "");
    }

}
