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
package functionaltests;

import static functionaltests.RestFuncTHelper.getRestServerUrl;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.authentication.ConnectionInfo;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.*;
import org.ow2.proactive.scheduler.rest.ISchedulerClient;
import org.ow2.proactive.scheduler.rest.SchedulerClient;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;


public class RMClientTest extends AbstractRestFuncTestCase {

    protected static final long MAX_WAIT_TIME = 5 * 60 * 1000;

    @Test(timeout = MAX_WAIT_TIME)
    public void testSchedulerNodeClient() throws Throwable {
        ISchedulerClient client = clientInstance();
        Job job = nodeClientJob("/functionaltests/descriptors/rm_client_node.groovy", null, null);
        JobId jobId = submitJob(job, client);
        TaskResult tRes = client.waitForTask(jobId.toString(), "NodeClientTask", TimeUnit.MINUTES.toMillis(5));
        System.out.println(tRes.getOutput().getAllLogs(false));
        Assert.assertNotNull(tRes);
        Assert.assertTrue(((ArrayList) tRes.value()).get(0) instanceof RMNodeEvent);
    }

    protected ISchedulerClient clientInstance() throws Exception {
        ISchedulerClient client = SchedulerClient.createInstance();
        client.init(new ConnectionInfo(getRestServerUrl(), getLogin(), getPassword(), null, true));
        return client;
    }

    protected JobId submitJob(Job job, ISchedulerClient client) throws Exception {
        return client.submit(job);
    }

    protected Job nodeClientJob(String groovyScript, String forkScript, String cleaningScript) throws Exception {

        URL scriptURL = SchedulerClientTest.class.getResource(groovyScript);

        TaskFlowJob job = new TaskFlowJob();
        job.setName("NodeClientJob");
        ScriptTask task = new ScriptTask();
        task.setName("NodeClientTask");
        if (forkScript != null) {
            ForkEnvironment forkEnvironment = new ForkEnvironment();
            forkEnvironment.setEnvScript(new SimpleScript(IOUtils.toString(SchedulerClientTest.class.getResource(forkScript)
                                                                                                    .toURI()),
                                                          "groovy"));
            task.setForkEnvironment(forkEnvironment);
        }
        task.setScript(new TaskScript(new SimpleScript(IOUtils.toString(scriptURL.toURI()), "groovy")));
        //add CleanScript to test external APIs
        if (cleaningScript != null) {
            task.setCleaningScript(new SimpleScript(IOUtils.toString(SchedulerClientTest.class.getResource(cleaningScript)
                                                                                              .toURI()),
                                                    "groovy"));
        }
        job.addTask(task);
        return job;
    }
}
