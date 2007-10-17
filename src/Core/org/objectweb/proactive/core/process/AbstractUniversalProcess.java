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

import org.objectweb.proactive.core.process.filetransfer.FileTransferWorkShop;
import org.objectweb.proactive.core.util.HostsInfos;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public abstract class AbstractUniversalProcess implements UniversalProcess {
    protected static final String LOCALHOST = getLocalHost();
    public final static String DEFAULT_USERNAME = System.getProperty(
            "user.name");
    public final static String DEFAULT_HOSTNAME = LOCALHOST;
    protected String hostname = DEFAULT_HOSTNAME;
    protected String username;
    protected String[] environment;
    protected String command;
    protected String command_path;
    protected boolean isStarted;
    protected boolean isFinished;
    protected String certificateLocation;
    protected String privateKeyLocation;
    protected String securityFile;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    protected AbstractUniversalProcess() {
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements UniversalProcess -----------------------------------------------
    //
    public String getCommand() {
        if (isStarted) {
            // is started we cache the command
            if (command == null) {
                command = buildCommand();
            }
            return command;
        } else {
            return buildCommand();
        }
    }

    public void startFileTransfer() {
        if (ProActiveLogger.getLogger(Loggers.DEPLOYMENT_FILETRANSFER)
                               .isDebugEnabled()) {
            ProActiveLogger.getLogger(Loggers.DEPLOYMENT_FILETRANSFER)
                           .debug("FileTransfer initializations ");
        }

        FileTransferWorkShop ftwDeploy = getFileTransferWorkShopDeploy();
        FileTransferWorkShop ftwRetrieve = getFileTransferWorkShopRetrieve();

        //Set process envirorment information into the FileTransferWorkshop
        pushProcessAttributes(ftwDeploy.dstInfoParams);
        pushProcessAttributes(ftwRetrieve.srcInfoParams);

        internalStartFileTransfer(ftwDeploy);
    }

    /**
     * @return A FileTransferWorkShop instance for the deploy queue
     */
    abstract FileTransferWorkShop getFileTransferWorkShopDeploy();

    /**
     * @return A FileTransferWorkShop instance for the retrieve queue
     */
    abstract FileTransferWorkShop getFileTransferWorkShopRetrieve();

    public void setEnvironment(String[] environment) {
        checkStarted();
        this.environment = environment;
    }

    public String[] getEnvironment() {
        return environment;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        checkStarted();
        if (hostname == null) {
            throw new NullPointerException();
        }
        this.hostname = hostname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        checkStarted();
        this.username = username;
    }

    public void startProcess() throws java.io.IOException {
        checkStarted();
        isStarted = true;
        if (username != null) {
            HostsInfos.setUserName(hostname, username);
        }

        //before starting the process we execute the filetransfer
        startFileTransfer();

        if (logger.isDebugEnabled()) {
            logger.debug(getCommand());
        }
        internalStartProcess(getCommand());
    }

    public void stopProcess() {
        if (!isStarted) {
            throw new IllegalStateException("Process not yet started");
        }
        if (isFinished) {
            return;
        }
        internalStopProcess();
    }

    public int waitFor() throws InterruptedException {
        return internalWaitFor();
    }

    public int exitValue() throws IllegalThreadStateException {
        return internalExitValue();
    }

    public boolean isStarted() {
        return isStarted;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isHierarchical() {
        return false;
    }

    public boolean isSequential() {
        return false;
    }

    public boolean isDependent() {
        return false;
    }

    public void setCommandPath(String path) {
        this.command_path = path;
    }

    public String getCommandPath() {
        return command_path;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    public void setStarted(boolean isStarted) {
        this.isStarted = isStarted;
    }

    public void setFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected void toString(StringBuilder sb) {
        sb.append("Process ");
        sb.append(this.getClass().getName());
        sb.append(" hostname=");
        sb.append(hostname);
        sb.append(" username=");
        sb.append(username);
        sb.append(" isStarted=");
        sb.append(isStarted);
        sb.append(" isFinished=");
        sb.append(isFinished);
        sb.append("\n command=");
        sb.append(buildCommand());
        if (environment != null) {
            sb.append("\n environment=");
            for (int i = 0; i < environment.length; i++) {
                sb.append("\n    variable[");
                sb.append(i);
                sb.append("]=");
                sb.append(environment[i]);
            }
        }
        sb.append("\n");
    }

    protected void checkStarted() {
        if (isStarted) {
            throw new IllegalStateException("Process already started");
        }
    }

    protected abstract String buildCommand();

    protected abstract void internalStartProcess(String commandToExecute)
        throws java.io.IOException;

    /**
     * Starts the FileTransfer specified by the parameter.
     */
    protected abstract void internalStartFileTransfer(FileTransferWorkShop fts);

    protected abstract void internalStopProcess();

    protected abstract int internalWaitFor() throws InterruptedException;

    protected abstract int internalExitValue()
        throws IllegalThreadStateException;

    /**
     * This method sets attributes into the FileTransferWorkshop.StructureInformation
     * For now:  : hostname, username
     *
     * If the attributes already exists then it remains unchanged.
     *
     * This method should be overriden if the process want's to set
     * specific parameters to the FileTransferWorkshop before starting
     * the FileTransfer.
     * @param infoParams
     */
    protected void pushProcessAttributes(
        FileTransferWorkShop.StructureInformation infoParams) {
        if ((infoParams.getUsername().length() <= 0) && (username != null) &&
                (username.length() > 0)) {
            infoParams.setUsername(username);
        }

        if ((infoParams.getHostname().length() <= 0) && (hostname != null) &&
                (hostname.length() > 0) &&
                !hostname.equalsIgnoreCase(DEFAULT_HOSTNAME)) {
            infoParams.setHostname(hostname);
        }
    }

    // -- PRIVATE METHODS -----------------------------------------------
    //
    private static String getLocalHost() {
        try {
            return URIBuilder.getHostNameorIP(URIBuilder.getLocalAddress());
        } catch (java.net.UnknownHostException e) {
            return "localhost";
        }
    }
}
