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
package org.objectweb.proactive.mpi.control;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFileTransfer;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.filetransfer.RemoteFile;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.mpi.MPIResult;
import org.objectweb.proactive.mpi.MPISpmd;
import org.objectweb.proactive.mpi.MPISpmdImpl.LateDeploymentHelper;


public class ProActiveMPIManager implements Serializable {
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.MPI_CONTROL_MANAGER);
    public final static String DEFAULT_LIBRARY_NAME = "libProActiveMPIComm.so";

    /** number of jobs */
    private static int currentJobNumber = 0;

    /** list of MPISpmd object */
    private List<MPISpmd> mpiSpmdList;

    /*  Map<jobID, ProActiveCoupling []> */
    private Map<Integer, ProActiveMPICoupling[]> proxyMap;

    /*  Map<jobID, PASPMD ProActiveMPICoupling> */
    private Map<Integer, ProActiveMPICoupling> spmdProxyMap;

    /*  Map<jobID, Map<class, PASPMD user class || user proxy array>> */
    private Map<Integer, Map<String, Object>> userProxyMap;

    /*  ackToStart[jobID] = number of proxy registered */
    private int[] ackToStart;

    /*  ackToRecvlist[jobID] = number of proxy ready to begin activities */
    private int[] ackToRecv;
    private boolean debugWaitForInit = false;

    public ProActiveMPIManager() {
    }

    public void deploy(List<MPISpmd> spmdList) {
        this.mpiSpmdList = spmdList;
        this.proxyMap = new Hashtable<Integer, ProActiveMPICoupling[]>();
        this.spmdProxyMap = new Hashtable<Integer, ProActiveMPICoupling>();
        this.userProxyMap = new Hashtable<Integer, Map<String, Object>>();
        this.ackToStart = new int[spmdList.size()];
        this.ackToRecv = new int[spmdList.size()];

        // loop on the MPISpmd object list
        try {
            for (int i = 0; i < spmdList.size(); i++) {
                VirtualNode vn = ((MPISpmd) spmdList.get(currentJobNumber)).getVn();
                Node[] allNodes = vn.getNodes();
                String remoteLibraryPath = ((MPISpmd) spmdList.get(currentJobNumber)).getRemoteLibraryPath();

                ClassLoader cl = this.getClass().getClassLoader();
                java.net.URL u = cl
                        .getResource("org/objectweb/proactive/mpi/control/" + DEFAULT_LIBRARY_NAME);

                if (remoteLibraryPath != null) {
                    File remoteDest = new File(remoteLibraryPath + "/" + DEFAULT_LIBRARY_NAME);
                    File localSource = new File(u.getFile());

                    RemoteFile filePushed = PAFileTransfer.push(localSource, allNodes[0], remoteDest);
                    filePushed.waitFor();
                }
                //else 
                // we assume local and remote path to reach the shared library were the same
                //TODO Where do we update the LD_LIBRARY_PATH ?
                ackToStart[i] = allNodes.length - 1;
                ackToRecv[i] = allNodes.length - 1;
                Object[][] params = new Object[allNodes.length][];

                // create parameters
                // "Comm" is the name of the JNI Library
                for (int j = 0; j < params.length; j++) {
                    params[j] = new Object[] { "ProActiveMPIComm",
                            (ProActiveMPIManager) PAActiveObject.getStubOnThis(), currentJobNumber, j };
                }

                if (logger.isInfoEnabled()) {
                    logger.info("[MANAGER] Create SPMD Proxy for jobID: " + currentJobNumber);
                }

                ProActiveMPICoupling spmdCouplingProxy = (ProActiveMPICoupling) PASPMD.newSPMDGroup(
                        ProActiveMPICoupling.class.getName(), params, vn.getNodes());

                // create PASPMD proxy
                this.spmdProxyMap.put(currentJobNumber, spmdCouplingProxy);

                if (logger.isInfoEnabled()) {
                    logger.info("[MANAGER] Initialize remote environments");
                }

                // initialise queues & semaphores and start thread
                Ack ack = spmdCouplingProxy.initEnvironment();
                PAFuture.waitFor(ack);

                if (logger.isInfoEnabled()) {
                    logger.info("[MANAGER] Activate remote thread for communication");
                }

                // once environment is ready, start thread to get mpi process rank  
                spmdCouplingProxy.createRecvThread();
                // initialise joblist & and userProxyList table
                //TODO why would we need this proxyMap as we already have the spmdCouplingProxy ??
                this.proxyMap.put(currentJobNumber, new ProActiveMPICoupling[allNodes.length]);

                this.userProxyMap.put(currentJobNumber, new Hashtable<String, Object>());

                currentJobNumber++;
                //TODO Why don't we return a reference on the ProActiveMpiCoupling in order to avoid manipulate hazardous jobId ?
            }
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void register(int jobID, int rank) {
        // ack of corresponding job is null means that the 
        // job is ready to recv message from another job
        if (logger.isInfoEnabled()) {
            logger.info("[MANAGER] JobID #" + jobID + " rank " + rank +
                "has notified its mpi interface is ready (" +
                (this.proxyMap.get(jobID).length - ackToRecv[jobID]) + "/" + this.proxyMap.get(jobID).length +
                ")");
        }

        // Mpi process of that rank has been initialised 
        if (ackToRecv[jobID] == 0) {
            for (int i = 0; i < currentJobNumber; i++) {
                // we wait for all jobs to finish Mpi initialisation
                if (ackToRecv[i] != 0) {
                    return;
                }
            }
            for (int i = 0; i < currentJobNumber; i++) {
                ((ProActiveMPICoupling) spmdProxyMap.get(i)).wakeUpThread();
            }
        } else {
            // we decrease the number of remaining ack to receive
            ackToRecv[jobID]--;
        }
    }

    // insert Comm Active Object at the correct location
    public void register(int jobID, int rank, ProActiveMPICoupling activeProxyComm) {
        if (jobID < currentJobNumber) {
            ProActiveMPICoupling[] mpiCouplingArray = ((ProActiveMPICoupling[]) this.proxyMap.get(jobID));

            mpiCouplingArray[rank] = activeProxyComm;

            // test if this job is totally registered
            boolean deployUserSpmdObject = true;
            for (int i = 0; i < mpiCouplingArray.length; i++) {
                if (mpiCouplingArray[i] == null) {
                    // not totally registered
                    deployUserSpmdObject = false;
                }
            }

            //  all proxy are registered
            if (deployUserSpmdObject) {
                // create a new array of nodes well ordered
                Node[] orderedNodes = new Node[mpiCouplingArray.length];
                for (int i = 0; i < orderedNodes.length; i++) {
                    try {
                        orderedNodes[i] = mpiCouplingArray[i].getNode();
                    } catch (NodeException e) {
                        e.printStackTrace();
                    }
                }
                Hashtable<String, Object> userProxyList = new Hashtable<String, Object>();
                try {
                    deployUserSpmdClasses(jobID, orderedNodes, userProxyList);
                    deployUserClasses(jobID, orderedNodes, userProxyList);
                } catch (ClassNotReifiableException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ActiveObjectCreationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NodeException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            /* If all jobs have finished */
            for (int i = 0; i < currentJobNumber; i++) {
                int jobListLength = ((ProActiveMPICoupling[]) this.proxyMap.get(i)).length;
                for (int j = 0; j < jobListLength; j++) {
                    if (((ProActiveMPICoupling[]) this.proxyMap.get(i))[j] == null) {
                        return;
                    }
                }
            }

            for (int i = 0; i < currentJobNumber; i++) {
                // send the table of User ProSpmd object to all the Proxy 
                try {
                    //TODO replace by a group call on the spmd proxy object
                    ((ProActiveMPICoupling) spmdProxyMap.get(i)).notifyProxy(this.proxyMap,
                            this.spmdProxyMap, this.userProxyMap);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            this.debugWaitForInit = true;
        } else {
            throw new IndexOutOfBoundsException(" No MPI job exists with num " + jobID);
        }
    }

    public boolean waitForInit() {
        return !this.debugWaitForInit;
    }

    public void deployUserClasses(int jobID, Node[] orderedNodes, Hashtable<String, Object> userProxyList)
            throws ActiveObjectCreationException, NodeException {
        final Map<String, LateDeploymentHelper> userClassMap = mpiSpmdList.get(jobID).getUserClassToDeploy();

        for (Iterator<Entry<String, LateDeploymentHelper>> iterator = userClassMap.entrySet().iterator(); iterator
                .hasNext();) {
            final Entry<String, LateDeploymentHelper> e = iterator.next();
            LateDeploymentHelper d = null;
            if ((d = e.getValue()).isUserClass()) {
                final String classname = e.getKey();
                final Object[] proxyList = new Object[orderedNodes.length];
                final List<LateDeploymentHelper> list = d.getUserClassesRank();

                /* Iterate over user class to instantiate */
                for (Iterator<LateDeploymentHelper> rankIt = list.iterator(); rankIt.hasNext();) {
                    final LateDeploymentHelper userClassInfo = rankIt.next();
                    proxyList[userClassInfo.getRank()] = PAActiveObject.newActive(classname,
                            (Object[]) userClassInfo.getParams(), orderedNodes[userClassInfo.getRank()]);
                }
                userProxyList.put(classname, proxyList);
            }
        }

        this.userProxyMap.put(jobID, userProxyList);
    }

    public void deployUserSpmdClasses(int jobID, Node[] orderedNodes, Hashtable<String, Object> userProxyList)
            throws ClassNotReifiableException, ActiveObjectCreationException, NodeException,
            ClassNotFoundException {
        //  get the list of SPMD class to instantiate for this MPISpmd object and send it as parameter.
        final Map<String, LateDeploymentHelper> userClassMap = mpiSpmdList.get(jobID).getUserClassToDeploy();

        for (Iterator<Entry<String, LateDeploymentHelper>> iterator = userClassMap.entrySet().iterator(); iterator
                .hasNext();) {
            final Entry<String, LateDeploymentHelper> e = iterator.next();
            LateDeploymentHelper d = null;
            if ((d = e.getValue()).isSpmd()) {
                userProxyList.put(e.getKey(), PASPMD.newSPMDGroup(e.getKey(), (Object[][]) d.getParams(),
                        orderedNodes));
            }
        }

        this.userProxyMap.put(jobID, userProxyList);
    }

    public void notifyNativeInterfaceIsReady(int jobID) {
        // ack of job is null means we can start MPI application
        if (logger.isInfoEnabled()) {
            logger.info("[MANAGER] JobID #" + jobID + " has notified its native interface is ready (" +
                (this.proxyMap.get(jobID).length - ackToStart[jobID]) + "/" +
                this.proxyMap.get(jobID).length + ")");
        }
        if (ackToStart[jobID] == 0) {
            MPISpmd mpiSpmd = (MPISpmd) mpiSpmdList.get(jobID);
            @SuppressWarnings("unused")
            MPIResult res = mpiSpmd.startMPI();
            if (logger.isInfoEnabled()) {
                logger.info("[MANAGER] Start MPI has been send for JobID #" + jobID);
            }
        } else {
            ackToStart[jobID]--;
        }
    }

    public void unregister(int jobID, int rank) {
        if (jobID < currentJobNumber) {
            ((ProActiveMPICoupling[]) this.proxyMap.get(jobID))[rank] = null;
            if (logger.isInfoEnabled()) {
                logger.info("[MANAGER] JobID #" + jobID + " unregister mpi process #" + rank);
            }
            for (int i = 0; i < currentJobNumber; i++) {
                int jobListLength = ((ProActiveMPICoupling[]) this.proxyMap.get(i)).length;
                for (int j = 0; j < jobListLength; j++) {
                    if (((ProActiveMPICoupling[]) this.proxyMap.get(i))[j] != null) {
                        return;
                    }
                }
            }

            for (int i = 0; i < this.mpiSpmdList.size(); i++) {
                (((MPISpmd) this.mpiSpmdList.get(i)).getVn()).killAll(false);
            }
            System.exit(0);
        } else {
            throw new IndexOutOfBoundsException(" No MPI job exists with num " + jobID);
        }
    }
}
