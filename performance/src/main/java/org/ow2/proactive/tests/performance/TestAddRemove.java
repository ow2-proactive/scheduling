/*
 *  *
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.tests.performance;

import java.io.File;
import java.security.Policy;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.AccessType;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.tests.performance.rm.NodesDeployWaitCondition;
import org.ow2.proactive.tests.performance.rm.RMEventsMonitor;
import org.ow2.proactive.tests.performance.rm.RMTestListener;
import org.ow2.proactive.tests.performance.rm.RMWaitCondition;
import org.ow2.proactive.tests.performance.rm.TestRMProxy;
import org.ow2.proactive.utils.FileToBytesConverter;


public class TestAddRemove {

    private static final int NODES_NUMBER = 4;

    public static void main(String[] args) throws Exception {

        System.setProperty("java.security.policy",
                "/home/ybonnaffe/src/build/dist/scheduling/performance/src/main/resources/config/grant.all.java"
                    + ".policy");
        Policy.getPolicy().refresh();

        TestRMProxy rmProxy = PAActiveObject.newActive(TestRMProxy.class, new Object[] {});
        rmProxy.init("pnp://jily:64738", new CredData(CredData.parseLogin("admin"), CredData
                .parseDomain("admin"), "admin"));

        RMEventsMonitor eventsMonitor = new RMEventsMonitor();
        RMTestListener listener = RMTestListener.createRMTestListener(eventsMonitor);
        rmProxy.syncAddEventListener(listener);

        //        StringBuilder hostsListBuilder = new StringBuilder();
        //        for (String hostName : new String[] { "localhost" }) {
        //            hostsListBuilder.append(String.format("%s %d\n", hostName, 4));
        //        }
        //
        //        String hostsList = hostsListBuilder.toString();
        //        int timeout = 20000;
        //        int attempt = 1;
        //        String sshOptions = "";
        //        String targetOs = "UNIX";
        //        Object schedulingPath = "/home/ybonnaffe/src/build/dist/scheduling";
        //        String javaOptions = " -Dproactive.home=" + schedulingPath + " -Dpa.rm.home=" +
        // schedulingPath;
        //
        //        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(schedulingPath +
        //            "/config/authentication/rm.cred"));
        //
        //        String javaPath = "/home/ybonnaffe/Downloads/ProActiveWorkflowsScheduling-6.0
        // .0-RC1/jre/bin/java";
        //        Object[] infrastructureParameters = new Object[] { hostsList.getBytes(), timeout,
        // attempt, 22,
        // targetOs,
        //                javaOptions };
        //
        //        Object[] policyParameters = new Object[] { "users=dummyUser", AccessType.ME.toString() };
        //
        //        BooleanWrapper result = rmProxy.createNodeSource("TestSSH",
        // SSHInfrastructureV2.class.getName(),
        //                infrastructureParameters, StaticPolicy.class.getName(), policyParameters);
        //
        //        System.out.println("Node source created " + result);
        //
        //        RMWaitCondition nodesDeployWaitCondition = eventsMonitor
        //                .addWaitCondition(new NodesDeployWaitCondition("TestSSH", 1));
        //
        //        eventsMonitor.waitFor(nodesDeployWaitCondition, 5 * 60000, null);
        //
        //        Thread.sleep(5000);
        //
        //        BooleanWrapper removedRequest = rmProxy.removeNodeSource("TestSSH", true);
        //
        //        System.out.println("Node source Removed " + removedRequest);

        String schedulingPath = "/home/ybonnaffe/src/build/dist/scheduling";
        byte[] creds = FileToBytesConverter.convertFileToByteArray(
          new File(schedulingPath + "/config/authentication/rm.cred"));
        Object[] infrastructureParameters = new Object[] { creds, NODES_NUMBER, 50000, "" };

        Object[] policyParameters = new Object[] { "users=dummyUser", AccessType.ME.toString() };

        for (int i = 0; i < 10; i++) {
            String sourceName = "LocalTests" + i;

            BooleanWrapper result = rmProxy.createNodeSource(sourceName,
              LocalInfrastructure.class.getName(), infrastructureParameters, StaticPolicy.class.getName(),
              new Object[] { "ALL", "ALL" });

            System.out.println("NodeSource created " + result);

            RMWaitCondition nodesDeployWaitCondition = eventsMonitor.addWaitCondition(
              new NodesDeployWaitCondition(sourceName, NODES_NUMBER));
            eventsMonitor.waitFor(nodesDeployWaitCondition, 5 * 60000, null);

            BooleanWrapper removedRequest = rmProxy.removeNodeSource(sourceName, true);

            System.out.println("NodeSource removed " + removedRequest);

        }

        boolean booleanValue = rmProxy.disconnect().getBooleanValue();

        PAActiveObject.terminateActiveObject(listener, true);
        System.out.println(booleanValue);

        RuntimeFactory.getDefaultRuntime().killRT(true);
    }
}
