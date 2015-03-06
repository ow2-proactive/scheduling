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
package org.ow2.proactive.scheduler.newimpl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.scheduler.common.task.Decrypter;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
import org.ow2.proactive.scripting.Script;


public class TaskContext implements Serializable {
    private final Set<String> nodesURLs;
    private ExecutableContainer executableContainer;
    private TaskLauncherInitializer initializer;
    private Decrypter decrypter;
    private TaskResult[] previousTasksResults;

    private String scratchURI;
    private final String inputURI;
    private final String outputURI;
    private final String userURI;
    private final String globalURI;

    public TaskContext(ExecutableContainer executableContainer, TaskLauncherInitializer initializer) {
        this(executableContainer, initializer, null, "", "", "", "","");
    }

    public TaskContext(ExecutableContainer executableContainer,
            TaskLauncherInitializer initializer, TaskResult[] previousTasksResults) {
        this(executableContainer, initializer, previousTasksResults, "", "", "", "","");
        this.previousTasksResults = previousTasksResults;
    }

    public TaskContext(ExecutableContainer executableContainer,
                       TaskLauncherInitializer initializer, TaskResult[] previousTasksResults, String scratchURI, String inputURI, String outputURI, String userURI, String globalURI) {
        this.initializer = initializer; // copy?
        this.previousTasksResults = previousTasksResults;
        this.scratchURI = scratchURI;
        this.inputURI = inputURI;
        this.outputURI = outputURI;
        this.userURI = userURI;
        this.globalURI = globalURI;
        initializer.setNamingService(null);
        this.executableContainer = executableContainer;

        nodesURLs = new HashSet<String>();
        for (Node node : executableContainer.getNodes()) {
            nodesURLs.add(node.getNodeInformation().getURL());
        }
        executableContainer.setNodes(null);

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

    public Script<FlowAction> getControlFlowScript() {
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

    public Set<String> getNodesURLs() {
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
}
