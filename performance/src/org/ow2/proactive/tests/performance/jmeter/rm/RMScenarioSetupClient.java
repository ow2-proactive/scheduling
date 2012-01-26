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
package org.ow2.proactive.tests.performance.jmeter.rm;

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.tests.performance.jmeter.BaseJMeterClient;
import org.ow2.proactive.tests.performance.jmeter.JVMKillerThread;
import org.ow2.proactive.tests.performance.rm.RMTestListener;
import org.ow2.proactive.tests.performance.rm.TestRMProxy;


public class RMScenarioSetupClient extends BaseJMeterClient {

    public static final String PARAM_LISTENERS_NUMBER = "rmListenersNumber";

    private static class DummyEventsMonitor extends RMEventsMonitor {

        private int nodeEventCounter;

        private int nodeSourceEventCounter;

        private int rmEventCounter;

        @Override
        public void rmEvent(RMEvent event) {
            rmEventCounter++;
        }

        @Override
        public void nodeSourceEvent(RMNodeSourceEvent event) {
            // System.out.println("------- Event " + event.getEventType() + " " + event.getSourceName());

            nodeSourceEventCounter++;
        }

        @Override
        public void nodeEvent(RMNodeEvent event) {
            // System.out.println("--------- Event " + event.getEventType() + " " + event.getNodeState() + " " + event.getNodeSource());

            nodeEventCounter++;
        }

        public void printEventsInfo() {
            System.out.println(String.format(
                    "Received events: rm events %d, node events %d,  node source events %d", rmEventCounter,
                    nodeEventCounter, nodeSourceEventCounter));
        }

    }

    private static class RMListenerClient {

        private final TestRMProxy rm;

        private final RMTestListener listener;

        private final DummyEventsMonitor eventsMonitor;

        public RMListenerClient(RMConnectionParameters parameters) throws Exception {
            rm = parameters.connectWithProxyUserInterface();
            eventsMonitor = new DummyEventsMonitor();
            listener = RMTestListener.createRMTestListener(eventsMonitor);
            rm.syncAddEventListener(listener);
        }

        public void disconnect() {
            try {
                rm.removeEventListener();
            } catch (RMException e) {
                throw new RuntimeException("Failed to remove listener: " + e, e);
            }
            BooleanWrapper result = rm.disconnect();
            PAFuture.waitFor(result);
            eventsMonitor.printEventsInfo();
        }

    }

    private List<RMListenerClient> rmClients = new ArrayList<RMListenerClient>();

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = RMConnectionParameters.getDefaultParameters();
        args.addArgument(PARAM_LISTENERS_NUMBER, "${rmListenersNumber}");
        return args;
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (rmClients.size() > 0) {
            System.out.println("Disconnecting " + rmClients.size() + " listeners clients");
            for (RMListenerClient rmClient : rmClients) {
                rmClient.disconnect();
            }
            System.out.println("All listeners clients are disconnected");
        }

        JVMKillerThread.startKillerThreadIfNonGUIMode(5000);
    }

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        RMConnectionParameters parameters = new RMConnectionParameters(context);

        int listenersNumber = context.getIntParameter(PARAM_LISTENERS_NUMBER);
        if (listenersNumber > 0) {
            System.out.println("Registering " + listenersNumber + " listeners");

            for (int i = 0; i < listenersNumber; i++) {
                rmClients.add(new RMListenerClient(parameters));
            }

            System.out.println("All listeners are registered");
        }
    }

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context) throws Throwable {
        return null;
    }

}
