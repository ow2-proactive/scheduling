package functionaltests;

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
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */

import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.rest.ISchedulerClient;
import org.ow2.proactive.scheduler.rest.SchedulerClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static functionaltests.RestFuncTHelper.getRestServerUrl;

public class SchedulerClientTest extends AbstractRestFuncTestCase {
    
    /** Maximum wait time of 5 minutes */
    private static final long max_wait_time = 5 * 60 * 1000;

    @BeforeClass
    public static void beforeClass() throws Exception {
        init(SchedulerClientTest.class.getSimpleName());
    }

    @Test(timeout = max_wait_time)
    public void testLogin() throws Exception {
        clientInstance();
    }

    @Test(timeout = max_wait_time)
    public void testRenewSession() throws Exception {
        ISchedulerClient client = clientInstance();
        SchedulerStatus status = client.getStatus();
        assertNotNull(status);
        // use an invalid session
        client.setSession("invalid-session-identifier");
        // client should automatically renew the session identifier
        status = client.getStatus();
        assertNotNull(status);
    }

    @Test(timeout = max_wait_time)
    public void testWaitForTerminatingJob() throws Exception {
        ISchedulerClient client = clientInstance();
        Job job = defaultJob();
        JobId jobId = submitJob(job, client);
        // should return immediately
        client.waitForJob(jobId, TimeUnit.SECONDS.toMillis(10));
    }

    @Test(timeout = max_wait_time, expected = TimeoutException.class)
    public void testWaitForNonTerminatingJob() throws Exception {
        ISchedulerClient client = clientInstance();
        Job job = pendingJob();
        JobId jobId = submitJob(job, client);
        client.waitForJob(jobId, TimeUnit.SECONDS.toMillis(10));
    }

    private ISchedulerClient clientInstance() throws Exception {
        ISchedulerClient client = SchedulerClient.createInstance();
        client.init(getRestServerUrl(), getLogin(), getPassword());
        return client;
    }

    private JobId submitJob(Job job, ISchedulerClient client) throws Exception {
        return client.submit(job);
    }
}
