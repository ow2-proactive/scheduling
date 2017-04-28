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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Set;

import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


public class TaskUsingCredentialsTest extends SchedulerFunctionalTestNoRestart {
    private static URL jobDescriptor = TaskUsingCredentialsTest.class.getResource("/functionaltests/descriptors/Job_UsingCredentials.xml");

    @Test
    public void testTaskUsingCredentials() throws Exception {
        jobs_using_third_party_credentials();
        third_party_credentials_api();
    }

    private void jobs_using_third_party_credentials() throws Exception {
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        scheduler.putThirdPartyCredential("MY_APP_PASSWORD", "superpassword");

        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory()
                                                      .createJob(new File(jobDescriptor.toURI()).getAbsolutePath());

        if (OperatingSystem.getOperatingSystem() == org.objectweb.proactive.utils.OperatingSystem.unix) {
            NativeTask nativeTask = new NativeTask();
            nativeTask.setCommandLine("echo", "$credentials_MY_APP_PASSWORD");
            job.addTask(nativeTask);
        }

        JobId jobId = scheduler.submit(job);

        schedulerHelper.waitForEventJobFinished(jobId);

        JobResult jobResult = schedulerHelper.getJobResult(jobId);
        for (TaskResult taskResult : jobResult.getAllResults().values()) {
            assertTrue("task " + taskResult.getTaskId().getReadableName() + " did not print the credential",
                       taskResult.getOutput().getAllLogs(false).contains("superpassword"));
        }
    }

    public void third_party_credentials_api() throws Exception {
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        String longPassword = "-----BEGIN RSA PRIVATE KEY-----\n" +
                              "MIIEowIBAAKCAQEAxO0WhOyc4qv5aSNi8MMrDJOo3qmkpbkcOSvy4FJPOq4ZfnJ3\n" +
                              "w3zWocmdXbr6d3HKOYiJywGHf69Vah/9nbren4y17FW3UabRmPMIYAgbtvS8kf8z\n" +
                              "+WHCyYEm9eqZf7AWd3guMrbx2bzfOUVKmYNsxB04qzGwmSjQeE7YhAg45oBYwRY+\n" +
                              "QaD0XDcNsgJwDaYVU/D3Bc4G6tsiYoPSBYQ+10zEoT4S/3LduRsUWlxasm3eP8ED\n" +
                              "ougAIXjmScEkwjv/RedIa06WwIoE5Ci5HtsSngBdioaLFZOMO/e+BuN5pp3gny8g\n" +
                              "9EhQREd9Tx7s31yxt4MDvULimWFx1l45sVmkMwIDAQABAoIBAQCKGp+FXw7zZJoI\n" +
                              "Yvm7UZQ6QL/YT+6ZDoW9jpXJPdA0ne5hIFPfdAht9B/5oOyQoeuph5jjFtJ4+HSV\n" +
                              "dZP+bxQ7nonjEYX7rFsnwaEo/+a321D3rps7lJTvjjTNl9ZIlyxaYp07kdNw2SVP\n" +
                              "W8nieSnpK3kXjkSEVPxGszzi84U8GKDkDgXt3QdPzeN9ceFA3x06euvPFULMw7yV\n" +
                              "VGZGRE13hIOBhXRcH9jKxguJbheFUVFHf4gu5BrYsEK0yLLoFFobTuXLjtA5EGLH\n" +
                              "6OVF1M9873C07n9zUWUTRIPeW3rmh+OjKHS3nz0lryNRQKuNxP7bR5Kdy32www3S\n" +
                              "rmCYwcWhAoGBAPFse5Kqe+wRtXSGKtKimgRh8xeLZA2MwjTI7513Va/xR9XjLjrL\n" +
                              "s9kknEkRIf9jAdwFehPxiBProActiveSchedulingGhfRoHoFAFVqrs/oHScMfO5\n" +
                              "lIUFkgojGVPXwvIcilPTWzWaD4KUDY7QSUjTfmPS/wtBgtk2cuEd0t1DAoGBANDQ\n" +
                              "1isgPk7L2D4fh9aeA/6qwFcZInN+cdEdaIoHKc77TsR/86OXQIIg7O6uLI39JMbo\n" +
                              "p//11r5vm5ubjbAnykYJL+t5B+jWieTvngE4tEIJhcgqoSRya/DKL9ARn2tTjftm\n" +
                              "Hpxp2okHM5+r0q80c989PwD4IMO65h4wEAt3HLZRAoGAITaDaZH6qmdlRzqN+ZxV\n" +
                              "A/VVtA+BHDwZG5npHQilySawc0Rlv8D2ZREcTxEEVFYSk2pNeSDpTx/NUSarqR8E\n" +
                              "GukR7z9xH4CfIpAC8IwQiOIf+OrkFFubixFRHgPmIBq2vwgeH5ocGiuvpo8nrlYJ\n" +
                              "PvOZl7IXVD0W+zr6Yu3vbHECgYAjNgPXM9Gt4cut9g0m0HBmAg764N8hUIIKvAXD\n" +
                              "uJ+BKnlGwzinLjsPdlPdj3st2jDYZaTmkWLLq/A2Vg2XVa5TDvuInlkKFxsbgphH\n" +
                              "JnOm6wonDaEsjyrKaJ2VXVNferBnYvnocCUMlC1NUGDvcE3Vp/M2y6BiwOJK1tnt\n" +
                              "xQEPcQKBgFf7A3BlBLAOfnSFr1JUW5LqhNOwangXLAyMkLtMlL4QemGj+yhwzE/N\n" +
                              "YOoInV75eaD8In57HQlwgbRIazyJ9b8gDensPlDFlVAQ98ffOb42gR11QRinQ6PL\n" +
                              "VYXdGf7hRbfCSUqDDEYoJI18q8H0yomBE3pMoRRiuGX3A/YW6wyT\n" +
                              "-----END RSA PRIVATE KEY-----";

        scheduler.putThirdPartyCredential("MY_APP_PASSWORD", longPassword);
        Set<String> credentialsKeySet = scheduler.thirdPartyCredentialsKeySet();
        assertTrue(credentialsKeySet.contains("MY_APP_PASSWORD"));

        scheduler.removeThirdPartyCredential("MY_APP_PASSWORD");

        credentialsKeySet = scheduler.thirdPartyCredentialsKeySet();
        assertFalse(credentialsKeySet.contains("MY_APP_PASSWORD"));
    }

}
