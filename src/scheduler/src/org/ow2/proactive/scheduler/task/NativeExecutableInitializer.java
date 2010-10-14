/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task;

import java.util.List;

import org.ow2.proactive.scheduler.common.task.ExecutableInitializer;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher.OneShotDecrypter;
import org.ow2.proactive.scripting.GenerationScript;


/**
 * NativeExecutableInitializer is the class used to store context of native executable initialization
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 */
public class NativeExecutableInitializer implements ExecutableInitializer {

    /** Command that should be executed */
    protected String command[];

    /** Generation script for the given executable */
    protected GenerationScript generationScript;

    /** External process launching directory*/
    protected String workingDir = null;

    /** Store required node host name list */
    protected List<String> nodesHost;

    /** Decrypter from launcher */
    private OneShotDecrypter decrypter = null;

    /**
     * Get the command
     *
     * @return the command
     */
    public String[] getCommand() {
        return command;
    }

    /**
     * Set the command value to the given command value
     *
     * @param command the command to set
     */
    public void setCommand(String[] command) {
        this.command = command;
    }

    /**
     * Get the generation script
     *
     * @return the generation script
     */
    public GenerationScript getGenerationScript() {
        return generationScript;
    }

    /**
     * Set the generation script value to the given generation script value
     *
     * @param generated the generation script to set
     */
    public void setGenerationScript(GenerationScript generated) {
        this.generationScript = generated;
    }

    /**
     * Get the workingDir
     *
     * @return the workingDir
     */
    public String getWorkingDir() {
        return workingDir;
    }

    /**
     * Set the workingDir value to the given workingDir value
     *
     * @param workingDir the workingDir to set
     */
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * Get the nodesHost list
     *
     * @return the nodesHost list
     */
    public List<String> getNodesHost() {
        return nodesHost;
    }

    /**
     * Set the nodesHost value to the given nodesHost value
     *
     * @param nodesHost the nodesHost to set
     */
    public void setNodesHost(List<String> nodesHost) {
        this.nodesHost = nodesHost;
    }

    /**
     * {@inheritDoc}
     */
    public OneShotDecrypter getDecrypter() {
        return decrypter;
    }

    /**
     * {@inheritDoc}
     */
    public void setDecrypter(OneShotDecrypter decrypter) {
        this.decrypter = decrypter;
    }

}
