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
package org.objectweb.proactive.core.process.mpi;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.process.filetransfer.FileDependant;
import org.objectweb.proactive.core.process.filetransfer.FileTransferDefinition;


/**
 * MPI Process implementation.
 * This implementation works only for ProActive deployment, and not to submit single commands
 * @author The ProActive Team
 * @version 1.0,  2005/11/10
 * @since   ProActive 3.0
 */
public abstract class MPIProcess extends AbstractExternalProcessDecorator implements FileDependant {

    /**
     * Firsts parameters
     */
    protected static final String DEFAULT_HOSTSFILENAME_PATH = ".machinefile";
    protected static final String DEFAULT_MPICOMMAND_PATH = "/usr/bin/mpirun";
    protected static final String DEFAULT_FILE_LOCATION = System.getProperty("user.home");
    public final static String DEFAULT_SSH_COPYPROTOCOL = "scp";
    protected static final String DEFAULT_HOSTS_NUMBER = "1";
    protected int jobID;
    protected String mpiCommandOptions = null;
    protected String hostsFileName = DEFAULT_HOSTSFILENAME_PATH;
    protected String mpiFileName = null;
    protected String localPath = DEFAULT_FILE_LOCATION;
    protected String remotePath = null;
    protected String hostsNumber = DEFAULT_HOSTS_NUMBER;

    // TEMPORARY
    protected String PROACTIVE_HOME = DEFAULT_FILE_LOCATION + "/ProActive";

    /**
     * Create a new MPIProcess
     * Used with XML Descriptors
     */
    public MPIProcess() {
        super();
        setCompositionType(COPY_FILE_AND_APPEND_COMMAND);
        this.command_path = DEFAULT_MPICOMMAND_PATH;
        FILE_TRANSFER_DEFAULT_PROTOCOL = DEFAULT_SSH_COPYPROTOCOL;
    }

    public MPIProcess(ExternalProcess targetProcess) {
        super(targetProcess);
        this.command_path = DEFAULT_MPICOMMAND_PATH;
        FILE_TRANSFER_DEFAULT_PROTOCOL = DEFAULT_SSH_COPYPROTOCOL;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "mpi";
    }

    @Override
    protected String internalBuildCommand() {
        return buildMPICommand();
    }

    protected String buildMPICommand() {
        StringBuilder mpiSubCommand = new StringBuilder();
        mpiSubCommand.append(this.command_path).append(" ");
        if (remotePath != null) {
            mpiSubCommand.append("-machinefile").append(" ");
            mpiSubCommand.append(remotePath).append("/");
            mpiSubCommand.append(this.hostsFileName).append(" ");
            mpiSubCommand.append("-nolocal").append(" ");
        }

        mpiSubCommand.append("-np").append(" ");
        mpiSubCommand.append(this.hostsNumber).append(" ");
        if (remotePath != null) {
            mpiSubCommand.append(remotePath).append("/");
        } else {
            mpiSubCommand.append(localPath).append("/");
        }
        mpiSubCommand.append(this.mpiFileName).append(" ");
        if (mpiCommandOptions != null) {
            mpiSubCommand.append(this.mpiCommandOptions).append(" ");
        }
        return mpiSubCommand.toString();
    }

    public FileTransferDefinition getFileTransfertDefinition() {
        FileTransferDefinition ft = new FileTransferDefinition("mpiProcess");
        if (remotePath != null) {
            ft.addFile(localPath + "/" + hostsFileName, remotePath + "/" + hostsFileName);
            //    System.out.println(localPath + "/" + hostsFileName + " --> " +
            //                        remotePath + "/" + hostsFileName);
        }

        return ft;
    }

    /************************************************************************
     *                              GETTERS AND SETTERS                     *
     ************************************************************************/
    public UniversalProcess getFinalProcess() {
        return this.getTargetProcess();
    }

    /**
     * @return Returns the mpiFileName.
     */
    public String getMpiFileName() {
        return mpiFileName;
    }

    /**
     * @param mpiFileName The mpiFileName to set.
     */
    public void setMpiFileName(String mpiFileName) {
        this.mpiFileName = mpiFileName;
    }

    /**
     * @return Returns the hostsFileName.
     */
    public String getHostsFileName() {
        return hostsFileName;
    }

    /**
     * @param hostsFileName The hostsFileName to set.
     */
    public void setHostsFileName(String hostsFileName) {
        this.hostsFileName = hostsFileName;
    }

    /**
     * @return Returns the hostsNumber.
     */
    public String getHostsNumber() {
        return this.hostsNumber;
    }

    /**
     * @param hostsNumber The hostsNumber to set.
     */
    public void setHostsNumber(String hostsNumber) {
        this.hostsNumber = hostsNumber;
    }

    /**
     * @return Returns the localPath.
     */
    public String getLocalPath() {
        return localPath;
    }

    /**
     * @param localPath The localPath to set.
     */
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    /**
     * @return Returns the remotePath.
     */
    public String getRemotePath() {
        return remotePath;
    }

    /**
     * @param remotePath The remotePath to set.
     */
    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    /**
     * @param mpiCommandOptions The mpiCommandOptions to set.
     */
    public void setMpiCommandOptions(String mpiCommandOptions) {
        this.mpiCommandOptions = mpiCommandOptions;
    }

    /**
     * @return Returns the mpiCommandOptions.
     */
    public String getMpiCommandOptions() {
        return this.mpiCommandOptions;
    }

    /** @see org.objectweb.proactive.core.process.UniversalProcess#isDependent() */
    @Override
    public boolean isDependent() {
        return true;
    }

    public int getNodeNumber() {
        return 0;
    }

    /******************************************************************************************
     *                                END OF GETTERS AND SETTERS                              *
     ******************************************************************************************/
}
