/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.examples.WaitAndPrint;

import functionalTests.FunctionalTest;


/**
 * This class tests a special behavior.
 * Multiple clients will submit job during a given time, and try to get the result each second until it gets it.
 * Jobs contain several tasks that do nothing.
 * The goal is to overload the scheduler of getJobResult request while submitting and scheduling jobs.
 * The important point is the number of useless requests that must be handle by the front-end.
 * 
 * @author The ProActive Team
 * @date 08 july 09
 * @since ProActive Scheduling 1.0
 */
public class TestMultipleUsersMakingMassivGetResultRequest extends FunctionalTest {

    private static int ThreadNumber = 8; //8 clients
    private static long jobSubmissionDuration = 60 * 1000; // 60 sec
    private static long getResultDelay = 1 * 1000; // 1 sec
    private static int taskPerJob = 10;
    private static int nbFinished = 0;//will be increase to count terminated threads

    /**
    * Tests start here.
    *
    * @throws Throwable any exception that can be thrown during the test.
    */
    @org.junit.Test
    public void run() throws Throwable {

        final SchedulerAuthenticationInterface auth = SchedulerTHelper.getSchedulerAuth();

        //create a job
        final TaskFlowJob job = new TaskFlowJob();
        for (int i = 0; i < taskPerJob; i++) {
            JavaTask task = new JavaTask();
            task.setName("jt" + i);
            task.setExecutableClassName(WaitAndPrint.class.getName());
            task.addArgument("sleepTime", "1");
            job.addTask(task);
        }

        //start threads
        for (int i = 0; i < ThreadNumber; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        //connect the scheduler
                        SchedulerTHelper.log(Thread.currentThread().getName() +
                            " -> Connecting the scheduler");
                        Credentials cred = Credentials.createCredentials(new CredData(
                            SchedulerTHelper.username, SchedulerTHelper.password), auth.getPublicKey());
                        Scheduler user = auth.login(cred);
                        SchedulerTHelper.log(Thread.currentThread().getName() + " -> Connected");
                        long start = System.currentTimeMillis();
                        int submitted = 1;
                        while (true) {
                            SchedulerTHelper.log(Thread.currentThread().getName() + " -> Submit (" +
                                submitted + ")");
                            JobId id = user.submit(job);
                            JobResult jr = null;
                            while (jr == null) {
                                Thread.sleep(getResultDelay);
                                jr = user.getJobResult(id);
                            }
                            if (System.currentTimeMillis() - start > jobSubmissionDuration) {
                                SchedulerTHelper.log(Thread.currentThread().getName() + " -> Terminate");
                                nbFinished++;
                                break;
                            }
                            submitted++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        while (nbFinished < ThreadNumber) {
            Thread.sleep(1000);
        }
        SchedulerTHelper.log("All threads terminated.");
    }
}