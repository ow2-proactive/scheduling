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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.jmeter.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.tests.performance.jmeter.BaseJMeterClient;
import org.ow2.proactive.tests.performance.jmeter.JVMKillerThread;
import org.ow2.proactive.tests.performance.scheduler.SchedulerTestListener;
import org.ow2.proactive.tests.performance.scheduler.TestSchedulerProxy;


public class SchedulerScenarioSetupClient extends BaseJMeterClient {

    public static final String PARAM_LISTENERS_NUMBER = "schedulerListenersNumber";

    class DummyEventsMonitor implements SchedulerEventListener {

        private int schedulerState;

        private int jobSubmitted;

        private int jobStateUpdated;

        private int taskStateUpdated;

        private int userUpdated;

        @Override
        public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
            schedulerState++;
        }

        @Override
        public void jobSubmittedEvent(JobState job) {
            jobSubmitted++;
        }

        @Override
        public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
            jobStateUpdated++;
        }

        @Override
        public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
            taskStateUpdated++;
        }

        @Override
        public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
            userUpdated++;
        }

        public void printEventsNumber() {
            logInfo(String
                    .format(
                            "Events: scheduler state=%d, job submitted=%d, job state updated=%d, task state updated %d, user updated %d",
                            schedulerState, jobSubmitted, jobStateUpdated, taskStateUpdated, userUpdated));
        }
    }

    class SchedulerListenerClient {

        private final TestSchedulerProxy proxy;

        private final DummyEventsMonitor eventsMonitor;

        SchedulerListenerClient(SchedulerConnectionParameters parameters) throws Exception {
            logInfo(String.format("Connecting to the Scheduler (%s, %s, %s, %s)", parameters
                    .getSchedulerUrl(), parameters.getSchedulerLogin(), parameters.getSchedulerPassword(),
                    Thread.currentThread()));
            proxy = parameters.connectWithProxy(60000);
            eventsMonitor = new DummyEventsMonitor();
            SchedulerTestListener listener = SchedulerTestListener.createListener(eventsMonitor);
            proxy.addEventListener(listener, false);
        }

        void printEventsAndDisconnect() throws Exception {
            eventsMonitor.printEventsNumber();
            proxy.removeEventListener();
            proxy.disconnect();
        }

    }

    private List<SchedulerListenerClient> listenerClients = new ArrayList<SchedulerListenerClient>();

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = SchedulerConnectionParameters.getDefaultParameters();
        args.addArgument(PARAM_LISTENERS_NUMBER, "${schedulerListenersNumber}");
        return args;
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (listenerClients.size() > 0) {
            logInfo("Disconnecting " + listenerClients.size() + " listeners clients");
            for (SchedulerListenerClient client : listenerClients) {
                try {
                    client.printEventsAndDisconnect();
                } catch (Exception e) {
                    logError("Failed to disconnect listener: " + e, e);
                }
            }
            logInfo("All listeners clients are disconnected");
        }

        JVMKillerThread.startKillerThreadIfNonGUIMode(10000, getLogger());
    }

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context) throws Throwable {
        return null;
    }

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        int listenersNumber = context.getIntParameter(PARAM_LISTENERS_NUMBER);
        if (listenersNumber > 0) {
            SchedulerConnectionParameters parameters = new SchedulerConnectionParameters(context);
            logInfo("Registering " + listenersNumber + " scheduler listeners");

            for (int i = 0; i < listenersNumber; i++) {
                listenerClients.add(new SchedulerListenerClient(parameters));
            }

            logInfo("All listeners are registered");
        }
    }

}
