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
package org.ow2.proactive.scheduler.common.task;

import java.util.Arrays;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;


/**
 * Use this class to build a native task that will use a 'org.ow2.proactive.scheduler.task.NativeExecutable' and be integrated in a {@link TaskFlowJob}.</br>
 * A native task just includes a command line that can be set using {@link #setCommandLine(String[])}.</br>
 * You don't have to extend this class to launch your own native executable.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class NativeTask extends Task {

    /** Command line for this native task */
    private String[] commandLine = null;

    /* native executable launching directory (pwd) */
    private String workingDir = null;

    /**
     * Empty constructor.
     */
    public NativeTask() {
    }

    /**
     * Get the command line for this task.
     * 
     * @return the command line
     */
    public String[] getCommandLine() {
        return commandLine;
    }

    /**
     * Set the command line that will be executed in this task.
     * 
     * @param commandLine the commandLine to set
     */
    public void setCommandLine(String... commandLine) {
        this.commandLine = commandLine;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    @Override
    public String display() {
        String nl = System.getProperty("line.separator");
        String answer = super.display();
        return answer + nl + "\tCommandLine = " + Arrays.toString(commandLine) + nl +
            "\tWorkingDir = '" + workingDir + '\'';
    }
}
