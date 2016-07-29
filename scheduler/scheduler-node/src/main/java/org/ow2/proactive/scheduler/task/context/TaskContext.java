/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.scheduler.task.context;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.containers.ExecutableContainer;
import org.ow2.proactive.scheduler.task.utils.Decrypter;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.utils.ClasspathUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class TaskContext implements Serializable {

    private final List<String> otherNodesURLs;
    private final List<String> nodesHosts;

    private final ExecutableContainer executableContainer;
    private final TaskLauncherInitializer initializer;
    private final Decrypter decrypter;
    private final TaskResult[] previousTasksResults;

    private final String scratchURI;

    private final String cacheURI;
    private final String inputURI;
    private final String outputURI;
    private final String userURI;
    private final String globalURI;
    private final String schedulerHome;

    private final String progressFilePath;


    public TaskContext(ExecutableContainer executableContainer, TaskLauncherInitializer initializer,
                       TaskResult[] previousTasksResults, String scratchURI, String cacheURI, String inputURI, String outputURI,
                       String userURI,
                       String globalURI, String progressFilePath, String currentNodeHostname) throws NodeException {
        this(executableContainer, initializer, previousTasksResults, scratchURI, cacheURI, inputURI, outputURI, userURI,
                globalURI, progressFilePath, currentNodeHostname, null);
    }


    public TaskContext(ExecutableContainer executableContainer, TaskLauncherInitializer initializer,
                       TaskResult[] previousTasksResults, String scratchURI, String cacheURI, String inputURI, String outputURI,
                       String userURI,
                       String globalURI, String progressFilePath, String currentNodeHostname,
                       Decrypter decrypter) throws NodeException {
        this.initializer = initializer;
        initializer.setNamingService(null);
        this.previousTasksResults = previousTasksResults;
        this.scratchURI = scratchURI;
        this.cacheURI = cacheURI;
        this.inputURI = inputURI;
        this.outputURI = outputURI;
        this.userURI = userURI;
        this.globalURI = globalURI;
        this.progressFilePath = progressFilePath;
        this.schedulerHome = ClasspathUtils.findSchedulerHome();
        this.executableContainer = executableContainer;
        this.decrypter = decrypter;

        // Added executableContainer != null for now. It is too much work to refactor all of this.
        // This class should not be so overloaded with parameters in the constructor.
        // And it should not be mutable and it should not change other instance's data inside this
        // constructor.
        // Rule is: max 3 parameters in the constructor
        // Target: Immutable
        if (executableContainer != null && executableContainer.getNodes() != null) {
            int nbNodes = executableContainer.getNodes().size();
            otherNodesURLs = new ArrayList<>(nbNodes);
            nodesHosts = new ArrayList<>(nbNodes + 1);

            nodesHosts.add(currentNodeHostname);
            for (Node node : executableContainer.getNodes()) {
                otherNodesURLs.add(node.getNodeInformation().getURL());
                nodesHosts.add(node.getNodeInformation().getVMInformation().getHostName());
            }
            executableContainer.setNodes(null);
        } else {
            otherNodesURLs = new ArrayList<>(0);
            nodesHosts = new ArrayList<>(0);
        }
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

    public Decrypter getDecrypter() {
        return decrypter;
    }

    public TaskResult[] getPreviousTasksResults() {
        return previousTasksResults;
    }

    public List<String> getOtherNodesURLs() {
        return otherNodesURLs;
    }

    public String getScratchURI() {
        return scratchURI;
    }

    public String getCacheURI() {
        return cacheURI;
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
