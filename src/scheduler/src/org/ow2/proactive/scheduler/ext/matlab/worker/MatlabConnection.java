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
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matlab.worker;

import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabGlobalConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabTaskConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabInitException;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabTaskException;

import java.io.File;


/**
 * This interface defines the connection to the matlab engine. This connection can either be via Matlab Control (live connection)
 * or via Matlab batch mode. In the first case, commands sent are executed interactively by the matlab engine, in the latter case,
 * they are scheduled and run as one in a generated matlab script.
 */
public interface MatlabConnection {

    /**
     * Each time this method is called creates a new MATLAB process using
     * either the matlabcontrol API or matlab batch mode.
     *
     * @param matlabExecutablePath The full path to the MATLAB executable
     * @param workingDir the directory where to start MATLAB
     * @throws MatlabInitException if MATLAB could not be initialized
     */
    public void acquire(String matlabExecutablePath, File workingDir, PASolveMatlabGlobalConfig paconfig,
            PASolveMatlabTaskConfig tconfig) throws MatlabInitException;

    /**
     * Used to send initialization matlab commands to the connection (in case of command grouping)
     */
    public void init();

    /**
     * Releases the connection, after a call to this method
     * the connection becomes unusable !
     */
    public void release();

    /**
     * Evaluate the given string in the workspace.
     *
     * @param command the command to evaluate
     * @throws MatlabTaskException If unable to evaluate the command
     */
    public void evalString(final String command) throws MatlabTaskException;

    /**
     * Extract a variable from the workspace.
     *
     * @param variableName name of the variable
     * @return value of the variable
     * @throws MatlabTaskException if unable to get the variable
     */
    public Object get(String variableName) throws MatlabTaskException;

    /**
     * Push a variable in to the workspace.
     *
     * @param variableName name of the variable
     * @param value the value of the variable
     * @throws MatlabTaskException if unable to set a variable
     */
    public void put(final String variableName, final Object value) throws MatlabTaskException;

    /**
     * Used to send finalization matlab commands to the connection and launch the command buffer (in case of command grouping)
     */
    public void launch() throws Exception;

    /**
     * Checks if toolboxes used by this task are available. Throws exceptions otherwise
     * @param command command which checks the toolboxes
     */
    void execCheckToolboxes(String command) throws Exception;
}