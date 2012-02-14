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
package org.ow2.proactive.tests.performance.deployment.gethostsfromscheduler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.tests.performance.deployment.DeploymentTestUtils;
import org.ow2.proactive.tests.performance.deployment.TestExecutionException;
import org.ow2.proactive.tests.performance.deployment.process.ProcessExecutor;
import org.ow2.proactive.tests.performance.deployment.rm.TestRMDeployHelper;
import org.ow2.proactive.utils.NodeSet;


public class TestExecutionTask extends JavaExecutable {

    static final int CONNECT_TIMEOUT = 60000;

    static final int CHECK_STOP_TIMEOUT = 10000;

    private Integer rmHostsNumber;

    private String targetToRun;

    private String antScriptPath;

    private String communicationObjectUrl;

    private String jmeterhome;

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        try {
            NodeSet nodes = getNodes();
            System.out.println("Task received following hosts:");
            Set<String> hosts = new LinkedHashSet<String>();
            for (int i = 0; i < nodes.size(); i++) {
                String host = nodes.get(i).getVMInformation().getHostName();
                hosts.add(host);
                System.out.println(host);
            }

            TaskCommunicationObject communicationObject = PAActiveObject.lookupActive(
                    TaskCommunicationObject.class, communicationObjectUrl);
            System.out.println("Task is waiting for signal to start execution");
            while (true) {
                if (!communicationObject.canRunTest()) {
                    Thread.sleep(5000);
                } else {
                    break;
                }
            }

            try {
                runTest(hosts);
            } finally {
                killTestProcesses(hosts);
            }

            System.out.println("Task is finishing");
        } catch (Throwable t) {
            System.out.println("Error in the task reserving nodes: " + t);
            t.printStackTrace(System.out);
            throw t;
        }

        return null;
    }

    private void killTestProcesses(Set<String> hosts) throws Exception {
        StringBuilder hostsString = new StringBuilder();
        for (String host : hosts) {
            hostsString.append(host).append(',');
        }

        System.out.println("Killing test processes on the hosts: " + hosts);
        DeploymentTestUtils.killTestProcesses(hosts, TestRMDeployHelper.TEST_JVM_OPTION);
    }

    private void runTest(Set<String> hosts) throws Exception {
        String allHosts[] = hosts.toArray(new String[hosts.size()]);

        StringBuilder rmHosts = new StringBuilder();

        String mainHost = allHosts[0];

        for (int i = 1; i < rmHostsNumber + 1 && i < allHosts.length; i++) {
            rmHosts.append(allHosts[i]).append(",");
        }

        StringBuilder createNodeSourceHosts = new StringBuilder();
        for (int i = rmHostsNumber + 1; i < allHosts.length; i++) {
            createNodeSourceHosts.append(allHosts[i]).append(",");
        }

        List<String> command = new ArrayList<String>();
        command.add("ant");
        command.add("-buildfile");
        command.add(antScriptPath);
        command.add("-Dcompile.tests.disable=true");
        command.add("-Djmeterhome=" + jmeterhome);
        command.add("-Dtest.deploy.pamr.startNewRouter.host=" + mainHost);
        command.add("-Drm.deploy.rmHost=" + mainHost);
        command.add("-Dscheduler.deploy.schedulerHost=" + mainHost);
        command.add("-Drm.deploy.rmNodesHosts=" + rmHosts.toString());
        command.add("-DcreateNodeSourceHosts=" + createNodeSourceHosts.toString());

        command.add(targetToRun);

        System.out.println("Running tests " + command);

        ProcessExecutor processExecutor = new ProcessExecutor("Test run script", command, true, false);
        if (!processExecutor.executeAndWaitCompletion(Long.MAX_VALUE, false)) {
            System.out.println("Test execution process failed");
            throw new TestExecutionException("Test execution process failed");
        } else {
            System.out.println("Running execution finished succesfully");
        }
    }

}
