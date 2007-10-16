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
package org.objectweb.proactive.core.process.ssh;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.SimpleExternalProcess;
import org.objectweb.proactive.core.process.UniversalProcess;


/**
 * <p>
 * The SSHProcess class is able to start any class, of the ProActive library,
 * using ssh protocol.
 * </p><p>
 * For instance:
 * </p><pre>
 * .......
 * SSHProcess ssh = new SSHProcess(new SimpleExternalProcess("ls -lsa"));
 * ssh.setHostname("hostname.domain.fr");
 * ssh.startProcess();
 * .....
 * </pre>
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */
public class SSHProcess extends AbstractExternalProcessDecorator {
    public final static String DEFAULT_SSHPATH = "ssh";
    public final static String DEFAULT_SSH_COPYPROTOCOL = "scp";

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new SSHProcess
     * Used with XML Descriptor
     */
    public SSHProcess() {
        super();

        FILE_TRANSFER_DEFAULT_PROTOCOL = DEFAULT_SSH_COPYPROTOCOL;
        this.command_path = DEFAULT_SSHPATH;
    }

    /**
     * Creates a new SSHProcess
     * @param targetProcess The target process associated to this process. The target process
     * represents the process that will be launched after logging remote host with ssh protocol
     */
    public SSHProcess(ExternalProcess targetProcess) {
        super(targetProcess);

        FILE_TRANSFER_DEFAULT_PROTOCOL = DEFAULT_SSH_COPYPROTOCOL;
        this.command_path = DEFAULT_SSHPATH;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "ssh_" + targetProcess.getProcessId();
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

    public static void main(String[] args) {
        try {
            SSHProcess ssh = new SSHProcess(new SimpleExternalProcess("ls -lsa"));
            ssh.setHostname("galere1.inria.fr");
            ssh.startProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected String internalBuildCommand() {
        return buildSSHCommand() + buildEnvironmentCommand();
    }

    protected String buildSSHCommand() {
        StringBuilder command = new StringBuilder();
        command.append(command_path);
        // append username
        if (username != null) {
            command.append(" -l ");
            command.append(username);
        }

        // append host
        command.append(" ");
        command.append(hostname);
        command.append(" ");
        if (logger.isDebugEnabled()) {
            logger.debug(command.toString());
        }
        command.append(" -- ");
        return command.toString();
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
}
