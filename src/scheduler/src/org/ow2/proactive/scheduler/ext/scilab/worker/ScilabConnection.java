package org.ow2.proactive.scheduler.ext.scilab.worker;

import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabGlobalConfig;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig;
import org.ow2.proactive.scheduler.ext.scilab.common.exception.ScilabInitException;
import org.ow2.proactive.scheduler.ext.scilab.common.exception.ScilabTaskException;

import java.io.File;


/**
 * ScilabConnection
 *
 * @author The ProActive Team
 */
public interface ScilabConnection {

    /**
     * Each time this method is called creates a new SCILAB process using
     * the scilabcontrol API.
     *
     * @param scilabExecutablePath The full path to the SCILAB executable
     * @param workingDir the directory where to start SCILAB
     * @throws ScilabInitException if SCILAB could not be initialized
     */
    public void acquire(String scilabExecutablePath, File workingDir, PASolveScilabGlobalConfig paconfig,
            PASolveScilabTaskConfig tconfig) throws ScilabInitException;

    /**
     * Used to send initialization scilab commands to the connection (in case of command grouping)
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
     * @throws ScilabTaskException If unable to evaluate the command
     */
    public void evalString(final String command) throws ScilabTaskException;

    /**
     * Extract a variable from the workspace.
     *
     * @param variableName name of the variable
     * @return value of the variable
     * @throws ScilabTaskException if unable to get the variable
     */
    public Object get(String variableName) throws ScilabTaskException;

    /**
     * Push a variable in to the workspace.
     *
     * @param variableName name of the variable
     * @param value the value of the variable
     * @throws ScilabTaskException if unable to set a variable
     */
    public void put(final String variableName, final Object value) throws ScilabTaskException;

    /**
     * Used to send finalization scilab commands to the connection and launch the command buffer (in case of command grouping)
     */
    public void launch() throws Exception;
}
