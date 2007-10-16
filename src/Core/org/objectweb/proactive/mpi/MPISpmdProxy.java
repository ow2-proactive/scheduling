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
package org.objectweb.proactive.mpi;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class MPISpmdProxy implements MPISpmd, java.io.Serializable {
    protected final static Logger MPI_PROXY_LOGGER = ProActiveLogger.getLogger(Loggers.MPI);

    /** the status of this MPISpmd object */
    private String status = MPIConstants.MPI_DEFAULT_STATUS;

    /** the name of the MPISpmd object */
    private String name;

    /** the MPISpmd Active object on which this proxy is pointing */
    private MPISpmdImpl target;

    /**
     * API method for creating a new MPISpmd proxy object from an existing Virtual Node
     */
    public MPISpmdProxy(VirtualNode vn) throws RuntimeException {
        try {
            target = (MPISpmdImpl) ProActiveObject.newActive(MPISpmdImpl.class.getName(),
                    new Object[] { vn });
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
        name = target.getName();
        MPI_PROXY_LOGGER.debug("[MPI Proxy] creating MPI SPMD active object: " +
            name);
        MPI_PROXY_LOGGER.debug("[MPI Proxy] status : " + status);
        target.setImmediateServices();
    }

    /**
     * API method for starting MPI program
     * @return MPIResult
     */
    public MPIResult startMPI() throws IllegalMPIStateException {
        MPI_PROXY_LOGGER.debug(
            "[MPI Proxy] call start method on active object ");
        MPI_PROXY_LOGGER.debug("[MPI Proxy] status : " + status);
        // UNSTARTED/DEFAULT status
        if (status.equals(MPIConstants.MPI_UNSTARTED)) {
            setStatus(MPIConstants.MPI_RUNNING);
            return target.startMPI();
        } else {
            ProActiveObject.terminateActiveObject(target, true);
            throw new IllegalMPIStateException(
                "!!! ERROR: cannot start MPI process " + this.name +
                " Caused by: MPI process has already been started once ");
        }
    }

    /**
     * API method for reStarting MPI program - run a new computation independently
     * if the first one is currently running
     * @return MPIResult
     */
    public MPIResult reStartMPI() throws IllegalMPIStateException {
        MPI_PROXY_LOGGER.debug(
            "[MPI Proxy] call reStart method on active object ");
        MPI_PROXY_LOGGER.debug("[MPI Proxy] status : " + status);
        // check if program is already finished and change status if yes
        checkTerminationStatus();
        // UNSTARTED
        if (!status.equals(MPIConstants.MPI_UNSTARTED)) {
            setStatus(MPIConstants.MPI_RUNNING);
            target.reinitProcess();
            return target.startMPI();
        } else {
            ProActiveObject.terminateActiveObject(target, true);
            throw new IllegalMPIStateException(
                "!!! ERROR: cannot restart MPI process " + this.name +
                " Caused by: no mpi process has been started once before");
        }
    }

    /**
     * API method for killing MPI program -
     * Kills the MPI program. The MPI program represented by this MPISpmd object is forcibly terminated.
     * @return boolean - true if program has been correctly been killed, false otherwise
     */
    public boolean killMPI() {
        MPI_PROXY_LOGGER.debug("[MPI Proxy] Kill MPI Process ");
        checkTerminationStatus();
        // RUNNING status
        if (status.equals(MPIConstants.MPI_RUNNING)) {
            setStatus(MPIConstants.MPI_KILLED);
            return target.killMPI();
        } // FINISHED/KILLED status
        else if (status.equals(MPIConstants.MPI_FINISHED) ||
                status.equals(MPIConstants.MPI_KILLED)) {
            return false;
        }
        // UNSTARTED status
        else {
            ProActiveObject.terminateActiveObject(target, true);
            throw new IllegalMPIStateException(
                "!!! ERROR: cannot kill MPI process " + this.name +
                " Caused by: no mpi process has been started once before!");
        }
    }

    /**
     * API method for getting MPI program status -
     */
    public String getStatus() {
        checkTerminationStatus();
        return status;
    }

    /**
     * API method for setting MPI program command arguments
     * @param arguments - the arguments of the MPI program
     */
    public void setMPICommandArguments(String arguments) {
        target.setMPICommandArguments(arguments);
    }

    /**
     * API method for getting MPI program name
     * @param String - the name of the MPI program
     */
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(target.toString());
        sb.append("\n Status: ");
        sb.append(getStatus());
        return sb.toString();
    }

    //
    // -- PRIVATE METHODS FOR SERIALIZATION -------------------------------------------------
    //
    //    private void writeObject(java.io.ObjectOutputStream out)
    //        throws IllegalMPIStateException {
    //        target.killMPI();
    //        ProActive.terminateActiveObject(target, true);
    //        throw new IllegalMPIStateException(
    //            "Copy's not allowed for MPI proxy object");
    //    }
    private void setStatus(String status) {
        this.status = status;
    }

    private void checkTerminationStatus() {
        if (target.isFinished() && (!status.equals(MPIConstants.MPI_FINISHED)) &&
                (!status.equals(MPIConstants.MPI_KILLED))) {
            status = MPIConstants.MPI_FINISHED;
        }
    }

    public VirtualNodeInternal getVn() {
        return this.target.getVn();
    }

    //  ----+----+----+----+----+----+----+----+----+----+----+-------+----+----
    //  --+----+---- methods for the future wrapping with control ----+----+----
    //  ----+----+----+----+----+----+----+----+----+----+----+-------+----+----
    public void newActiveSpmd(String cl) {
        this.target.newActiveSpmd(cl);
    }

    public void newActiveSpmd(String cl, Object[] params) {
        this.target.newActiveSpmd(cl, params);
    }

    public void newActiveSpmd(String cl, Object[][] params) {
        this.target.newActiveSpmd(cl, params);
    }

    public void newActive(String cl, Object[] params, int rank) {
        this.target.newActive(cl, params, rank);
    }

    public ArrayList getClasses() {
        return this.target.getClasses();
    }

    public ArrayList getSpmdClasses() {
        return this.target.getSpmdClasses();
    }

    public Hashtable getClassesParams() {
        return this.target.getClassesParams();
    }

    public Hashtable getSpmdClassesParams() {
        return this.target.getSpmdClassesParams();
    }

    public String getRemoteLibraryPath() {
        return this.target.getRemoteLibraryPath();
    }
}
