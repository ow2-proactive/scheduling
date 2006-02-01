/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.mpi;

import org.apache.log4j.Logger;

import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.process.AbstractExternalProcess;
import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.mpi.MPIDependentProcess;
import org.objectweb.proactive.core.process.mpi.MPIProcess;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.io.IOException;


public class MPISpmd {
    private final static Logger MPI_LOGGER = ProActiveLogger.getLogger(Loggers.MPI);
    private ExternalProcess mpiProcess = null;

    /** the name of this mpi process */
    private String name;

    /** the status of this mpi process */
    private String status;

    /**
     * API method for creating a new MPISPMD object from an existing Virtual Node
     */
    public MPISpmd(VirtualNode vn) throws RuntimeException {
        //    	 active
        if (!(vn.isActivated())) {
            vn.activate();
        }
        if (vn.hasMPIProcess()) {
            this.mpiProcess = vn.getMPIProcess();
            this.name = vn.getName();
            setStatus(MPIConstants.MPI_DEFAULT_STATUS);
        } else {
            throw new RuntimeException(
                " ERROR: Cannot create MPISpmd object Cause: No MPI process attached with the virtual node " +
                vn.getName());
        }
    }

    /**
     * API method for creating a new MPISPMD object from an existing Virtual Node
     */
    public MPISpmd(ExternalProcess mpiProcess, String name)
        throws RuntimeException {
        if (mpiProcess != null) {
            this.mpiProcess = mpiProcess;
            this.name = name;
            setStatus(MPIConstants.MPI_DEFAULT_STATUS);
        } else {
            throw new RuntimeException(
                " ERROR: Cannot create MPISpmd object Cause: MPI process is null or invalid ");
        }
    }

    /**
     * API method for starting MPI program
     */
    public void startMPI() throws IllegalStateException {
        MPI_LOGGER.debug(" Start MPI Process: " + mpiProcess.toString());
        // status UNSTARTED or DEFAULT
        if (status.equals(MPIConstants.MPI_UNSTARTED)) {
            setStatus(MPIConstants.MPI_RUNNING);
            try {
                mpiProcess.startProcess();
            } catch (IOException e) {
                e.printStackTrace();
                MPI_LOGGER.error("ERROR: cannot start MPI process " +
                    this.name + " with command " + mpiProcess.getCommand());
            }
        } // status RUNNING/KILLED/FINISHED
        else {
            mpiProcess.stopProcess();
            throw new IllegalStateException("ERROR: cannot start MPI process " +
                this.name +
                " Caused by: MPI process has already been started once ");
        }
    }

    /**
     * API method for restarting MPI program -
     * it has to be finished to be restarted
     */
    public void reStartMPI() throws IllegalStateException {
        MPI_LOGGER.debug(" ReStart MPI Process: " + mpiProcess.toString());
        // status RUNNING
        if (status.equals(MPIConstants.MPI_RUNNING)) {
            mpiProcess.stopProcess();
            throw new IllegalStateException(
                "ERROR: cannot restart MPI process " + this.name +
                " Caused by: mpi process is already running");
        } // status UNSTARTED
        else if (status.equals(MPIConstants.MPI_UNSTARTED)) {
            throw new IllegalStateException(
                "ERROR: cannot restart MPI process " + this.name +
                " Caused by: no mpi process has been started once before");
        } // status KILLED/FINISHED
        else {
            reinitProcess();
            setStatus(MPIConstants.MPI_RUNNING);
            try {
                mpiProcess.startProcess();
            } catch (IOException e) {
                e.printStackTrace();
                MPI_LOGGER.error("ERROR: cannot restart MPI process " +
                    this.name + "with command" + mpiProcess.getCommand());
            }
        }
    }

    /**
     * API method for killing MPI program -
     * Kills the MPI program. The MPI program represented by this MPISpmd object is forcibly terminated.
     */
    public void killMPI() throws IllegalStateException {
        MPI_LOGGER.debug(" Killing MPI Process: " + mpiProcess.toString());
        this.checkProcessTermination();
        // status RUNNING 
        if (status.equals(MPIConstants.MPI_RUNNING)) {
            setStatus(MPIConstants.MPI_KILLED);
            mpiProcess.stopProcess();
            //the sleep might be needed for processes killed
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                MPI_LOGGER.error("ERROR: cannot kill MPI process " + this.name);
            }
        } // status KILLED/FINISHED/UNSTARTED 
        else {
            throw new IllegalStateException("ERROR: cannot kill MPI process " +
                this.name + " Caused by: no mpi process is running");
        }
    }

    /**
     * API method for waiting for MPI program termination -
     * causes the current MPI program to wait if necessary, until the MPI program represented
     * has terminated. This method returns immediately if the MPI program has already terminated.
     * If the program has not yet terminated, the calling thread will be blocked until the program exits.
     *
     */
    public void waitFor() throws IllegalStateException {
        MPI_LOGGER.debug(" Wait for MPI Process to finish...");
        //this.checkProcessTermination();
        // status RUNNING/FINISHED
        if (status.equals(MPIConstants.MPI_RUNNING) ||
                status.equals(MPIConstants.MPI_FINISHED)) {
            try {
                setStatus(MPIConstants.MPI_FINISHED);
                mpiProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
                MPI_LOGGER.error("ERROR: cannot wait for MPI process " +
                    this.name);
            }
        } // status KILLED/UNSTARTED 
        else {
            throw new IllegalStateException(
                "ERROR: cannot wait for MPI process " + this.name +
                " Caused by: no mpi process is running");
        }
    }

    /**
     * API method for getting MPI program status -
     */
    public String getStatus() {
        this.checkProcessTermination();
        return status;
    }

    /**
     * API method for getting MPI program return value
     * @return int - the return value of the MPI program
     */
    public int getReturnValue() throws IllegalStateException {
        if (status.equals(MPIConstants.MPI_FINISHED)) {
            return mpiProcess.exitValue();
        } else {
            mpiProcess.stopProcess();
            throw new IllegalStateException("ERROR: cannot return MPI process " +
                this.name +
                " termination value Caused by: no mpi process has finished yet");
        }
    }

    /**
     * API method for setting MPI program name
     * @param name - the name of the MPI program
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * API method for setting MPI program command arguments
     * @param arguments - the arguments of the MPI program
     */
    public void setCommandArguments(String arguments) {
        MPI_LOGGER.debug(((AbstractExternalProcess) this.mpiProcess).getCommand());
        // check for the position of the MPIProcess in the mpiProcess
        int rank = getMPIProcessRank(this.mpiProcess);
        ExternalProcess tempProc = this.mpiProcess;
        while (rank != 0) {
            tempProc = ((AbstractExternalProcessDecorator) tempProc).getTargetProcess();
            rank--;
        }
        ((MPIProcess) tempProc).setMpiCommandOptions(arguments);
        MPI_LOGGER.debug(((AbstractExternalProcess) this.mpiProcess).getCommand());
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" -  ");
        sb.append("\n Class: ");
        sb.append(this.getClass().getName());
        sb.append("\n Name: ");
        sb.append(this.name);
        sb.append("\n Status: ");
        sb.append(this.status);
        sb.append("\n Command: ");
        sb.append(getMPIProcess(this.mpiProcess).getCommand());
        sb.append("\n Process number: ");
        sb.append(getMPIProcess(this.mpiProcess).getHostsNumber());
        sb.append("\n -  ");
        return sb.toString();
    }

    //  returns MPI process 
    private MPIProcess getMPIProcess(ExternalProcess process) {
        while (!(process instanceof MPIProcess)) {
            process = ((ExternalProcess) ((AbstractExternalProcessDecorator) process).getTargetProcess());
        }
        return (MPIProcess) process;
    }

    // returns the rank of MPI process in the processes hierarchie
    private int getMPIProcessRank(ExternalProcess process) {
        int res = 0;
        while (!(process instanceof MPIProcess)) {
            res++;
            process = ((ExternalProcess) ((AbstractExternalProcessDecorator) process).getTargetProcess());
        }
        return res;
    }

    private void reinitProcess() {
        mpiProcess.setStarted(false);
        mpiProcess.setFinished(false);
    }

    private void checkProcessTermination() {
        if ((status.equals(MPIConstants.MPI_RUNNING)) &&
                (mpiProcess.isFinished())) {
            status = MPIConstants.MPI_FINISHED;
        }
    }

    private void setStatus(String status) {
        this.status = status;
    }
}
