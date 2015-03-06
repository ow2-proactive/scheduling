/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
package org.ow2.proactive.scheduler.common.task.executable.internal;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.dataspaces.LocalSpace;
import org.ow2.proactive.scheduler.common.task.dataspaces.RemoteSpace;


/**
 * DefaultStandaloneExecutableInitializer
 *
 * @author The ProActive Team
 **/
public class DefaultStandaloneExecutableInitializer implements StandaloneExecutableInitializer {

    protected List nodes;
    protected TaskId taskId;
    private PrintStream outputSink;
    private PrintStream errorSink;

    @Override
    public List getNodesURL() {
        return nodes;
    }

    @Override
    public void setNodesURL(List nodes) {
        this.nodes = nodes;
    }

    /** spaces **/
    protected LocalSpace localSpace;
    protected RemoteSpace inputSpace;
    protected RemoteSpace outputSpace;
    protected RemoteSpace globalSpace;
    protected RemoteSpace userSpace;

    public LocalSpace getLocalSpace() {
        return localSpace;
    }

    public void setLocalSpace(LocalSpace localSpace) {
        this.localSpace = localSpace;
    }

    public RemoteSpace getInputSpace() {
        return inputSpace;
    }

    public void setInputSpace(RemoteSpace inputSpace) {
        this.inputSpace = inputSpace;
    }

    public RemoteSpace getOutputSpace() {
        return outputSpace;
    }

    public void setOutputSpace(RemoteSpace outputSpace) {
        this.outputSpace = outputSpace;
    }

    public RemoteSpace getGlobalSpace() {
        return globalSpace;
    }

    public void setGlobalSpace(RemoteSpace globalSpace) {
        this.globalSpace = globalSpace;
    }

    public RemoteSpace getUserSpace() {
        return userSpace;
    }

    public void setUserSpace(RemoteSpace userSpace) {
        this.userSpace = userSpace;
    }

    @Override
    public TaskId getTaskId() {
        return taskId;
    }

    @Override
    public void setTaskId(TaskId taskId) {
        this.taskId = taskId;
    }

    @Override
    public PrintStream getOutputSink() {
        return outputSink;
    }

    @Override
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
