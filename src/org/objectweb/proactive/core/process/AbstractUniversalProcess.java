/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.process;

import org.objectweb.proactive.core.util.HostsInfos;
import org.objectweb.proactive.core.util.UrlBuilder;


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

    public boolean isStarted() {
        return isStarted;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isHierarchical() {
        return false;
    }

    public void setCommandPath(String path) {
        this.command_path = path;
    }

    public String getCommandPath() {
        return command_path;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        toString(sb);
        return sb.toString();
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected void toString(StringBuffer sb) {
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

    protected abstract void internalStopProcess();

    protected abstract int internalWaitFor() throws InterruptedException;

    // -- PRIVATE METHODS -----------------------------------------------
    //
    private static String getLocalHost() {
        try {
            return UrlBuilder.getHostNameorIP(java.net.InetAddress.getLocalHost());
        } catch (java.net.UnknownHostException e) {
            return "localhost";
        }
    }
}
