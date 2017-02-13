/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common.task;

import java.util.Arrays;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;


/**
 * Use this class to build a native task that will use a 'org.ow2.proactive.scheduler.task.NativeExecutable' and be integrated in a {@link TaskFlowJob}.
 * <p>
 * A native task just includes a command line that can be set using {@link #setCommandLine(String[])}.
 * <p>
 * You don't have to extend this class to launch your own native executable.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class NativeTask extends Task {

    /** Command line for this native task */
    private String[] commandLine = null;

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

    @Override
    public String display() {
        String nl = System.lineSeparator();

        return super.display() + nl + "\tCommandLine=" + Arrays.toString(commandLine);
    }

}
