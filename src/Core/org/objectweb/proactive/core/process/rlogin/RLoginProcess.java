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
package org.objectweb.proactive.core.process.rlogin;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.SimpleExternalProcess;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.process.lsf.LSFBSubProcess;


/**
 * <p>
 * The RloginProcess class is able to start any class, of the ProActive library,
 * using rlogin command.
 * </p><p>
 * For instance:
 * </p><pre>
 * ...............
 * PrunSubProcess lsf = new PrunSubProcess(new SimpleExternalProcess("ls -lsa"));
 * RLoginProcess p = new RLoginProcess(lsf, false);
 * p.setHostname("cluster_front_end_name");
 * p.startProcess();
 * ...............
 * </pre>
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */
public class RLoginProcess extends AbstractExternalProcessDecorator {
    public final static String DEFAULT_RLOGINPATH = "/usr/bin/rlogin ";
    private boolean exitAfterCommand;
    private String username;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new RloginProcess
     * Used with XML Descriptors
     */
    public RLoginProcess() {
        super();
        setCompositionType(SEND_TO_OUTPUT_STREAM_COMPOSITION);
        this.command_path = DEFAULT_RLOGINPATH;
    }

    /**
     * Creates a new RloginProcess
     * @param targetProcess The target process associated to this process. The target process
     * represents the process that will be launched after logging remote host with rlogin
     */
    public RLoginProcess(ExternalProcess targetProcess) {
        this(targetProcess, false);
    }

    /**
     * Creates a new RloginProcess
     * @param targetProcess The target process associated to this process. The target process
     * represents the process that will be launched after logging remote host with rlogin
     * @param exitAfterCommand If true the process will finished once rlogin command is performed. The default value is false
     */
    public RLoginProcess(ExternalProcess targetProcess, boolean exitAfterCommand) {
        super(targetProcess, SEND_TO_OUTPUT_STREAM_COMPOSITION);
        this.exitAfterCommand = exitAfterCommand;
        this.command_path = DEFAULT_RLOGINPATH;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "rlogin_" + targetProcess.getProcessId();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getNodeNumber()
     */
    public int getNodeNumber() {
        return targetProcess.getNodeNumber();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getFinalProcess()
     */
    public UniversalProcess getFinalProcess() {
        checkStarted();
        return targetProcess.getFinalProcess();
    }

    /**
     * Method setExitAfterCommand
     * @param b If true the process will finished once rlogin command is performed. The default vaule is false
     */
    public void setExitAfterCommand(boolean b) {
        exitAfterCommand = b;
    }

    /**
     * Returns the value of the boolean telling that the process will finished after rlogin command or will wait
     * for another command to be pushed once logging on the remote host
     * @return boolean
     */
    public boolean getExitAfterCommand() {
        return exitAfterCommand;
    }

    public static void main(String[] args) {
        try {
            LSFBSubProcess lsf = new LSFBSubProcess(new SimpleExternalProcess(
                        "ls -lsa"));
            RLoginProcess p = new RLoginProcess(lsf, false);
            p.setHostname("galere1");
            p.startProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected String internalBuildCommand() {
        return buildEnvironmentCommand() + buildRLoginCommand();
    }

    protected String buildRLoginCommand() {
        String command;
        if (this.username == null) {
            command = DEFAULT_RLOGINPATH + hostname + " ";
        } else {
            command = DEFAULT_RLOGINPATH + "-l " + username + " " + hostname +
                " ";
        }
        return command;
    }

    @Override
    protected void internalStartProcess(String command)
        throws java.io.IOException {
        super.internalStartProcess(command);
        if (exitAfterCommand) {
            outputMessageSink.setMessage(null);
        }
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
}
