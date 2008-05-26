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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.AbstractExternalProcess;
import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.mpi.MPIProcess;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class MPISpmdImpl implements MPISpmd, java.io.Serializable {
    private final static Logger MPI_IMPL_LOGGER = ProActiveLogger.getLogger(Loggers.MPI);

    /**  name of the MPISpmd object */
    private String name;

    /**  MPI Process that the Virtual Node references */
    private ExternalProcess mpiProcess = null;

    /**  Virtual Node containing resources */
    private VirtualNodeInternal vn;
    private Map<String, LateDeploymentHelper> userClassToDeploy;

    // empty no-args constructor 
    public MPISpmdImpl() {
    }

    /**
     * Set the immediate services for this active object
     */
    public int setImmediateServices() {
        PAActiveObject.setImmediateService("killMPI");
        PAActiveObject.setImmediateService("getStatus");
        PAActiveObject.setImmediateService("isFinished");
        PAActiveObject.setImmediateService("getUserClassToDeploy");
        return 0; //synchronous call
    }

    /**
     * API method for creating a new MPISPMD object from an existing Virtual Node
     * @throws NodeException
     */
    public MPISpmdImpl(VirtualNodeInternal vn) throws RuntimeException, NodeException {
        MPI_IMPL_LOGGER.debug("[MPISpmd object] creating MPI SPMD active object: " + vn.getName());
        //  active
        if (!(vn.isActivated())) {
            vn.activate();
        }
        if (vn.hasMPIProcess()) {
            this.userClassToDeploy = new HashMap<String, LateDeploymentHelper>();
            this.mpiProcess = vn.getMPIProcess();
            this.name = vn.getName();
            this.vn = vn;
        } else {
            throw new RuntimeException(
                "!!! ERROR: Cannot create MPISpmd object Cause: No MPI process attached with the virtual node " +
                    vn.getName());
        }
    }

    /**
     * API method for starting the MPI program
     * @return MPIResult
     */
    public MPIResult startMPI() {
        String hostname = "undef";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        MPI_IMPL_LOGGER.debug("[MPISpmd Object] Start MPI -q attached to vn " + this.name + " on host " +
            hostname);
        MPI_IMPL_LOGGER.debug("[MPISpmd Object] " + mpiProcess);

        MPIResult result = new MPIResult();
        try {
            mpiProcess.startProcess();
            MPI_IMPL_LOGGER.debug("[MPISpmd Object] wait for mpirun attached to vn " + this.name +
                " on host " + hostname);
            mpiProcess.waitFor();
            MPI_IMPL_LOGGER.debug("[MPISpmd Object] MPIRun attached to vn " + this.name + " on host " +
                hostname + " ended");
            result.setReturnValue(mpiProcess.exitValue());
            MPI_IMPL_LOGGER.debug("[MPISpmd Object] " + result.getReturnValue());
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            MPI_IMPL_LOGGER.error("!!! ERROR startMPI: cannot start MPI process " + this.name +
                " with command " + mpiProcess.getCommand());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * API method for reStarting the MPI program - run a new computation independently
     * if the first one is currently running
     * @return MPIResult
     */
    public MPIResult reStartMPI() {
        MPI_IMPL_LOGGER.debug("[MPISpmd Object] reStart MPI Process ");
        reinitProcess();
        return this.startMPI();
    }

    /**
     * API method for killing MPI program -
     * Kills the MPI program. The MPI program represented by this MPISpmd object is forcibly terminated.
     * Only the computation is killed, the MPISpmd object is still alive (the computation can be reStarted).
     */
    public boolean killMPI() {
        MPI_IMPL_LOGGER.debug("[MPISpmd Object] Kill MPI Process ");
        // as killMPI is an immediate service method it's possible that
        // stop method is called before start method on process thus interrupted exceptionis launched. 
        try {
            mpiProcess.stopProcess();
            //the sleep might be needed for processes killed
            Thread.sleep(200);
            return true;
        } catch (IllegalStateException e) {
            MPI_IMPL_LOGGER.error("Exception caught, waiting process to start to kill it.");
            while (!mpiProcess.isStarted()) {
            }
            mpiProcess.stopProcess();
        } catch (InterruptedException e) {
            e.printStackTrace();
            MPI_IMPL_LOGGER.error("!!! ERROR killMPI: cannot kill MPI process " + this.name);
        }
        return false;
    }

    /**
     * API method for setting MPI program command arguments
     * @param arguments - the arguments of the MPI program
     */
    public void setMPICommandArguments(String arguments) {
        MPI_IMPL_LOGGER.debug(((AbstractExternalProcess) this.mpiProcess).getCommand());
        // check for the position of the MPIProcess in the mpiProcess
        int rank = getMPIProcessRank(this.mpiProcess);
        ExternalProcess tempProc = this.mpiProcess;
        while (rank != 0) {
            tempProc = ((AbstractExternalProcessDecorator) tempProc).getTargetProcess();
            rank--;
        }
        ((MPIProcess) tempProc).setMpiCommandOptions(arguments);
        MPI_IMPL_LOGGER.debug(((AbstractExternalProcess) this.mpiProcess).getCommand());
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n Class: ");
        sb.append(this.getClass().getName());
        sb.append("\n Name: ");
        sb.append(this.name);
        sb.append("\n Command: ");
        sb.append(getMPIProcess(this.mpiProcess).getCommand());
        sb.append("\n Processes number: ");
        sb.append(getMPIProcess(this.mpiProcess).getHostsNumber());
        return sb.toString();
    }

    public void reinitProcess() {
        this.killMPI();
        mpiProcess.setStarted(false);
        mpiProcess.setFinished(false);
    }

    public String getStatus() {
        return null;
    }

    //  returns MPI process 
    private MPIProcess getMPIProcess(ExternalProcess process) {
        while (!(process instanceof MPIProcess)) {
            process = ((ExternalProcess) ((AbstractExternalProcessDecorator) process).getTargetProcess());
        }
        return (MPIProcess) process;
    }

    // returns the rank of MPI process in the processes hierarchy
    private int getMPIProcessRank(ExternalProcess process) {
        int res = 0;
        while (!(process instanceof MPIProcess)) {
            res++;
            process = ((ExternalProcess) ((AbstractExternalProcessDecorator) process).getTargetProcess());
        }
        return res;
    }

    public boolean isFinished() {
        return mpiProcess.isFinished();
    }

    public VirtualNodeInternal getVn() {
        return vn;
    }

    //  ----+----+----+----+----+----+----+----+----+----+----+-------+----+----
    //  --+----+---- methods for the future wrapping with control ----+----+----
    //  ----+----+----+----+----+----+----+----+----+----+----+-------+----+----
    public void newActiveSpmd(String cl, Object[][] params) {
        if (params.length != vn.getNumberOfCreatedNodesAfterDeployment()) {
            throw new RuntimeException("!!! ERROR: mismatch between number of parameters and number of Nodes");
        }

        if (userClassToDeploy.containsKey(cl)) {
            MPI_IMPL_LOGGER.info("!!! ERROR newActiveSpmd: " + cl +
                " class has already been added to the list of user classes to instanciate ");
        } else {
            userClassToDeploy.put(cl, new LateDeploymentHelper(-1, params));
        }
    }

    public void newActive(String cl, Object[] params, int rank) throws ArrayIndexOutOfBoundsException {
        if (checkRankValidity(rank)) {
            LateDeploymentHelper di = userClassToDeploy.get(cl);
            if (di == null) {
                di = new LateDeploymentHelper();
                di.update(rank, params);
                userClassToDeploy.put(cl, di);
            } else {
                // check if rank is still available 
                if (!di.update(rank, params)) {
                    MPI_IMPL_LOGGER
                            .info("!!! ERROR newActiveSpmd: " +
                                cl +
                                " class has already been added to the list of user classes to instanciate for this rank " +
                                rank);
                }
            }
        } else {
            MPI_IMPL_LOGGER.info("!!! ERROR newActiveSpmd: rank " + rank + " is not valid ");
            throw new ArrayIndexOutOfBoundsException("Rank " + rank +
                " is out of range while trying to instanciate class " + cl);
        }
    }

    private boolean checkRankValidity(int rank) {
        //TODO avoid usage of getNumberOfCreatedNodesAfterDeployment
        return (rank >= 0) && (rank < this.vn.getNumberOfCreatedNodesAfterDeployment());
    }

    public String getRemoteLibraryPath() {
        return getMPIProcess(this.mpiProcess).getRemotePath();
    }

    public static class LateDeploymentHelper {
        private List<LateDeploymentHelper> userDefinedList;
        private int rank;
        private Object params;

        public List<LateDeploymentHelper> getUserClassesRank() {
            return userDefinedList;
        }

        public int getRank() {
            return rank;
        }

        public Object getParams() {
            return params;
        }

        public LateDeploymentHelper() {
            userDefinedList = new LinkedList<LateDeploymentHelper>();
        }

        public LateDeploymentHelper(int i, Object params) {
            this.rank = i;
            this.params = params;
        }

        public boolean isSpmd() {
            return (userDefinedList == null);
        }

        public boolean isUserClass() {
            return !isSpmd();
        }

        public boolean update(int rank, Object[] params) {
            if (isAssigned(rank)) {
                return false;
            } else {
                userDefinedList.add(new LateDeploymentHelper(rank, params));
                return true;
            }
        }

        private boolean isAssigned(int rank) {
            Iterator<LateDeploymentHelper> it = userDefinedList.iterator();
            while (it.hasNext()) {
                LateDeploymentHelper ri = it.next();
                if (rank == ri.rank) {
                    return true;
                }
            }
            return false;
        }
    }

    public Map<String, LateDeploymentHelper> getUserClassToDeploy() {
        return userClassToDeploy;
    }
}
