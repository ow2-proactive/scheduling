/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.process;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * A class implementing this interface is able to start a process based on a command
 * to execute. The command is built from arbitrary parameters (up to the implementation) and
 * from the environment.
 * The external process can be customized up to the moment it is started. Once started the call
 * to methods to set the command throw an exception.
 */
public interface UniversalProcess extends java.io.Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT_PROCESS);

    /* Node count cannot be known  */
    public static int UNKNOWN_NODE_NUMBER = -19;

    /**
     * Returns the current environment for this process. Each
     * cell of the array contains the definition of one variable in a
     * syntax that is system dependant.
     * @return an array of string containing all environment variables or
     * null if the environment is empty
     */
    public String[] getEnvironment();

    /**
     * Set the environment for this process. Each
     * cell of the array contains the definition of one variable in a
     * syntax that is system dependant.
     * @param environment an array of string containing all environment variables or
     * null if the environment is empty
     */
    public void setEnvironment(String[] environment);

    /**
     * Return the hostname target of this process.
     * @return the hostname target of this process.
     */
    public String getHostname();

    /**
     * Set the hostname target of this process. By default the target host
     * is the localhost.
     * @param hostname the target hostname.
     */
    public void setHostname(String hostname);

    /**
     * Return the username that will be used to run the command.
     * @return the username that will be used to run the command.
     */
    public String getUsername();

    /**
     * Set the username that will be used to run the command.
     * By default the current username owner of the JVM process is used.
     * @param username the target username or null to use the default one.
     */
    public void setUsername(String username);

    /**
     * Returns the command that will be or has been execute by the process.
     * @return the command of this external process
     */
    public String getCommand();

    /**
     * Returns the id of the process
     * @return the id of the process. This id is just the first letters of the process
     * class, to be able to identify the sequence of processes used
     */
    public String getProcessId();

    /**
     * Returns the number of nodes targeted
     * @return the number of nodes targeted. Represents the number of nodes expected to use
     * when starting this process. If this number cannot be known, waiting for all available nodes
     * for example, UNKNOWN_NODE_NUMBER is returned.
     */
    public int getNodeNumber();

    /**
     * Returns the last process of the chain
     * @return the last process of the chain
     */
    public UniversalProcess getFinalProcess();

    /**
     * Starts the process by executing the command. The process can only
     * be started once.
     * @exception java.io.IOException if the process cannot be started.
     */
    public void startProcess() throws java.io.IOException;

    /**
     * Starts the FileTransfer if defined for this process.
     */
    public void startFileTransfer();

    /**
     * Stops the running process. If called on a stopped process this
     * method has no effect.
     */
    public void stopProcess();

    /**
     * Causes the current thread to wait until this Process has terminated.
     * This method returns immediately if the subprocess has already terminated.
     *  If the subprocess has not yet terminated, the calling thread will be blocked until the subprocess exits.
     * @return int exit value
     * @exception java.lang.InterruptedException if the current thread is interrupted by another thread while it is waiting.
     * Then the wait is ended and an InterruptedException is thrown
     */
    public int waitFor() throws InterruptedException;

    /**
     *  This method returns the exit value of the subprocess. By convention, the value 0 indicates normal termination.
     *  Subprocess has to be terminated, otherwise exception is thrown
     * @return int exit value
     * @exception java.lang.IllegalThreadStateException if the current thread is not yet terminated while it is trying to get a return value.
     */
    public int exitValue() throws IllegalThreadStateException;

    /**
     * Returns true if and only if this process has been started. A process that has been
     * started can be finished or running.
     */
    public boolean isStarted();

    /**
     * Returns true if and only if this process has been stopped. A process that has been
     * stopped has been started and is no more running.
     */
    public boolean isFinished();

    /**
     * Sets the path of the command to be executed by this process
     */
    public void setCommandPath(String path);

    /**
     * Returns the path of the command to be executed by this process
     * @return the path of the command to be executed by this process
     */
    public String getCommandPath();

    /**
     * Returns true if and only if this process is hierarchical
     */
    public boolean isHierarchical();

    /**
     * Returns true if and only if this process is sequential
     */
    public boolean isSequential();

    /**
     * Returns true if and only if this process is dependent
     */
    public boolean isDependent();

    /**
     * Sets the state started of this process
     */
    public void setStarted(boolean isStarted);

    /**
     * Sets the state finished of this process
     */
    public void setFinished(boolean isFinished);
}
