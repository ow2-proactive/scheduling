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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.task.containers.ExecutableContainer;
import org.ow2.proactive.scheduler.task.utils.Decrypter;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.utils.ClasspathUtils;


public class TaskContext implements Serializable {

    private final List<String> nodesURLs;
    private final List<String> nodesHosts;

    private ExecutableContainer executableContainer;
    private TaskLauncherInitializer initializer;
    private Decrypter decrypter;
    private TaskResult[] previousTasksResults;

    private String scratchURI;
    private final String inputURI;
    private final String outputURI;
    private final String userURI;
    private final String globalURI;
    private final String schedulerHome;

    private final String progressFilePath;

    public TaskContext(ExecutableContainer executableContainer, TaskLauncherInitializer initializer) throws
      NodeException {
        this(executableContainer, initializer, null, "", "", "", "", "", "",
          "");
    }

    public TaskContext(ExecutableContainer executableContainer,
            TaskLauncherInitializer initializer, TaskResult[] previousTasksResults) throws NodeException {
        this(executableContainer, initializer, previousTasksResults, "", "", "", "", "", "",
          "");
    }

    public TaskContext(ExecutableContainer executableContainer, TaskLauncherInitializer initializer,
      TaskResult[] previousTasksResults, String scratchURI, String inputURI, String outputURI, String userURI,
      String globalURI, String progressFilePath, String currentNodeHostname) throws NodeException {
        this.initializer = initializer;
        initializer.setNamingService(null);
        this.previousTasksResults = previousTasksResults;
        this.scratchURI = scratchURI;
        this.inputURI = inputURI;
        this.outputURI = outputURI;
        this.userURI = userURI;
        this.globalURI = globalURI;
        this.progressFilePath = progressFilePath;
        this.schedulerHome = ClasspathUtils.findSchedulerHome();
        this.executableContainer = executableContainer;

        if (executableContainer.getNodes() != null) {
            int nbNodes = executableContainer.getNodes().size();

            nodesURLs = new ArrayList<>(nbNodes);
            nodesHosts = new ArrayList<>(nbNodes + 1);

            nodesHosts.add(currentNodeHostname);
            for (Node node : executableContainer.getNodes()) {
                nodesURLs.add(node.getNodeInformation().getURL());
                nodesHosts.add(node.getNodeInformation().getVMInformation().getHostName());
            }

            executableContainer.setNodes(null);
        } else {
            nodesURLs = new ArrayList<>(0);
            nodesHosts = new ArrayList<>(0);
        }
    }

    public TaskContext(TaskContext context, ExecutableContainer container) throws NodeException {
        this(container, context.initializer, context.previousTasksResults,
                context.scratchURI, context.inputURI, context.outputURI, context.userURI, context.globalURI,
                context.progressFilePath,
          PAActiveObject.getNode().getNodeInformation().getVMInformation().getHostName());
    }

    public ExecutableContainer getExecutableContainer() {
        return executableContainer;
    }

    public Script<?> getPreScript() {
        return initializer.getPreScript();
    }

    public Script<?> getPostScript() {
        return initializer.getPostScript();
    }

    public FlowScript getControlFlowScript() {
        return initializer.getControlFlowScript();
    }

    public TaskId getTaskId() {
        return initializer.getTaskId();
    }

    public TaskLauncherInitializer getInitializer() {
        return initializer;
    }

    public void setDecrypter(Decrypter decrypter) {
        this.decrypter = decrypter;
    }

    public Decrypter getDecrypter() {
        return decrypter;
    }

    public TaskResult[] getPreviousTasksResults() {
        return previousTasksResults;
    }

    public List<String> getNodesURLs() {
        return nodesURLs;
    }

    public String getScratchURI() {
        return scratchURI;
    }

    public String getInputURI() {
        return inputURI;
    }

    public String getOutputURI() {
        return outputURI;
    }

    public String getUserURI() {
        return userURI;
    }

    public String getGlobalURI() {
        return globalURI;
    }

    public String getProgressFilePath() {
        return progressFilePath;
    }

    public boolean isRunAsUser() {
        return getExecutableContainer().isRunAsUser();
    }

    public String getSchedulerHome() {
        return schedulerHome;
    }

    public List<String> getNodesHosts() {
        return nodesHosts;
    }
}
