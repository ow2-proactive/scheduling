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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task.executable.internal;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;


/**
 * JavaStandaloneExecutableInitializer is the class used to store context of java standalone executable initialization
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class JavaStandaloneExecutableInitializer {

    /** Arguments of the java task */
    protected Map<String, byte[]> serializedArguments;

    /** Propagated variables from parent tasks */
    protected Map<String, byte[]> propagatedVariables;
    protected List<String> nodes;
    protected TaskId taskId;

    private Map<String, String> thirdPartyCredentials;
    private PrintStream outputSink;
    private PrintStream errorSink;

    /**
     * @throws java.io.IOException if the deserialization of the value cannot be performed.
     * @throws ClassNotFoundException if the value's class cannot be loaded.
     */
    public Map<String, Serializable> getArguments(ClassLoader cl) throws IOException, ClassNotFoundException {
        return SerializationUtil.deserializeVariableMap(this.serializedArguments, cl);
    }

    /**
     * Set the arguments value to the given arguments value
     *
     * @param serializedArguments the arguments to set
     */
    public void setSerializedArguments(Map<String, byte[]> serializedArguments) {
        this.serializedArguments = serializedArguments;
    }

    /**
     * Sets the propagated variable map for the current Java task.
     *
     * @param propagatedVariables
     *            a map of propagated variables
     */
    public void setPropagatedVariables(Map<String, byte[]> propagatedVariables) {
        this.propagatedVariables = propagatedVariables;
    }

    /**
     * Returns the propagated variables map of the current Java task.
     *g 
     * @return a map of variables
     */
    public Map<String, byte[]> getPropagatedVariables() {
        return propagatedVariables;
    }

    public Map<String, String> getThirdPartyCredentials() {
        return thirdPartyCredentials;
    }

    public void setThirdPartyCredentials(Map<String, String> thirdPartyCredentials) {
        this.thirdPartyCredentials = thirdPartyCredentials;
    }

    public List<String> getNodesURL() {
        return nodes;
    }

    public void setNodesURL(List<String> nodes) {
        this.nodes = nodes;
    }

    public TaskId getTaskId() {
        return taskId;
    }

    public void setTaskId(TaskId taskId) {
        this.taskId = taskId;
    }

    public PrintStream getOutputSink() {
        return outputSink;
    }

    public void setOutputSink(PrintStream redirectedStdout) {
        this.outputSink = redirectedStdout;
    }

    public PrintStream getErrorSink() {
        return errorSink;
    }

    public void setErrorSink(PrintStream errorSink) {
        this.errorSink = errorSink;
    }
}
