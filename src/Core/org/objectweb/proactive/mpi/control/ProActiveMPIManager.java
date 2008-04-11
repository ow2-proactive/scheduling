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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFileTransfer;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.filetransfer.RemoteFile;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.mpi.MPIResult;
import org.objectweb.proactive.mpi.MPISpmd;


public class ProActiveMPIManager implements Serializable {
    private final static Logger MPI_IMPL_LOGGER = ProActiveLogger.getLogger(Loggers.MPI_CONTROL_MANAGER);
    public final static String DEFAULT_LIBRARY_NAME = "libProActiveMPIComm.so";

    /** number of jobs */
    private static int currentJobNumber = 0;

    /** list of MPISpmd object */
    private ArrayList mpiSpmdList;

    /*  Hashtable<jobID, ProActiveCoupling []> */
    private Hashtable<Integer, ProActiveMPICoupling[]> proxyMap;

    /*  Hashtable<jobID, PASPMD ProActiveMPICoupling> */
    private Hashtable<Integer, ProActiveMPICoupling> spmdProxyMap;

    /*  Hashtable<jobID, Hashtable<class, PASPMD user class || user proxy array>> */
    private Hashtable<Integer, Hashtable> userProxyMap;

    /*  ackToStart[jobID] = number of proxy registered */
    private int[] ackToStart;

    /*  ackToRecvlist[jobID] = number of proxy ready to begin activities */
    private int[] ackToRecv;

    public ProActiveMPIManager() {
    }

    public void deploy(ArrayList spmdList) {
        this.mpiSpmdList = spmdList;
        this.proxyMap = new Hashtable<Integer, ProActiveMPICoupling[]>();
        this.spmdProxyMap = new Hashtable<Integer, ProActiveMPICoupling>();
        this.userProxyMap = new Hashtable<Integer, Hashtable>();
        this.ackToStart = new int[spmdList.size()];
        this.ackToRecv = new int[spmdList.size()];

        // loop on the MPISpmd object list
        try {
            for (int i = 0; i < spmdList.size(); i++) {
                VirtualNode vn = ((MPISpmd) spmdList.get(currentJobNumber)).getVn();
                Node[] allNodes;
                allNodes = vn.getNodes();
                String remoteLibraryPath = ((MPISpmd) spmdList.get(currentJobNumber)).getRemoteLibraryPath();

                ClassLoader cl = this.getClass().getClassLoader();
                java.net.URL u = cl
                        .getResource("org/objectweb/proactive/mpi/control/" + DEFAULT_LIBRARY_NAME);

                File remoteDest = new File(remoteLibraryPath + "/libProActiveMPIComm.so");
                File localSource = new File(u.getFile());

                RemoteFile filePushed = PAFileTransfer.push(localSource, allNodes[0], remoteDest);
                filePushed.waitFor();

                ackToStart[i] = allNodes.length - 1;
                ackToRecv[i] = allNodes.length - 1;
                Object[][] params = new Object[allNodes.length][];

                // create parameters
                // "Comm" is the name of the JNI Library
                for (int j = 0; j < params.length; j++) {
                    params[j] = new Object[] { "ProActiveMPIComm",
                            (ProActiveMPIManager) PAActiveObject.getStubOnThis(),
                            new Integer(currentJobNumber) };
                }
                MPI_IMPL_LOGGER.info("[MANAGER] Create SPMD Proxy for jobID: " + currentJobNumber);
                ProActiveMPICoupling spmdCouplingProxy = (ProActiveMPICoupling) PASPMD.newSPMDGroup(
                        ProActiveMPICoupling.class.getName(), params, vn.getNodes());

                // create PASPMD proxy
                this.spmdProxyMap.put(new Integer(currentJobNumber), spmdCouplingProxy);
                MPI_IMPL_LOGGER.info("[MANAGER] Initialize remote environments");
                // initialize queues & semaphores and start thread
                Ack ack = spmdCouplingProxy.initEnvironment();
                PAFuture.waitFor(ack);
                MPI_IMPL_LOGGER.info("[MANAGER] Activate remote thread for communication");
                // once environment is ready, start thread to get mpi process rank  
                spmdCouplingProxy.createRecvThread();
                // initialize joblist & and userProxyList table
                this.proxyMap.put(new Integer(currentJobNumber), new ProActiveMPICoupling[allNodes.length]);
                this.userProxyMap.put(new Integer(currentJobNumber), new Hashtable());

                currentJobNumber++;
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
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    public void register(int jobID, int rank) {
        // ack of corresponding job is null means that the 
        // job is ready to recv message from another job
        if (ackToRecv[jobID] == 0) {
            for (int i = 0; i < currentJobNumber; i++) {
                if (ackToRecv[i] != 0) {
                    return;
                }
            }
            for (int i = 0; i < currentJobNumber; i++) {
                (spmdProxyMap.get(new Integer(i))).wakeUpThread();
            }
        } else {
            // we decrease the number of daemon rest
            ackToRecv[jobID]--;
        }
    }

    // insert Comm Active Object at the correct location
    public void register(int jobID, int rank, ProActiveMPICoupling activeProxyComm) {
        if (jobID < currentJobNumber) {
            MPI_IMPL_LOGGER.info("[MANAGER] JobID #" + jobID + " register mpi process #" + rank);

            (this.proxyMap.get(new Integer(jobID)))[rank] = activeProxyComm;

            // test if this job is totally registered
            boolean deployUserSpmdObject = true;
            for (int i = 0; i < (this.proxyMap.get(new Integer(jobID))).length; i++) {
                if ((this.proxyMap.get(new Integer(jobID)))[i] == null) {
                    // not totally registered
                    deployUserSpmdObject = false;
                }
            }

            //  all proxy are registered
            if (deployUserSpmdObject) {
                // create a new array of nodes well ordered
                Node[] orderedNodes = new Node[(this.proxyMap.get(new Integer(jobID))).length];
                for (int i = 0; i < orderedNodes.length; i++) {
                    try {
                        orderedNodes[i] = (this.proxyMap.get(new Integer(jobID)))[i].getNode();
                    } catch (NodeException e) {
                        e.printStackTrace();
                    }
                }
                deployUserSpmdClasses(jobID, orderedNodes);
                deployUserClasses(jobID, orderedNodes);
            }

            for (int i = 0; i < currentJobNumber; i++) {
                int jobListLength = (this.proxyMap.get(new Integer(i))).length;
                for (int j = 0; j < jobListLength; j++) {
                    if ((this.proxyMap.get(new Integer(i)))[j] == null) {
                        return;
                    }
                }
            }

            for (int i = 0; i < currentJobNumber; i++) {
                // send the table of User ProSpmd object to all the Proxy 
                //     ((ProActiveMPICoupling) proxySpmdTabByJob.get(new Integer(i))).setUserProSPMDList(this.userSpmdTabByJob);
                (spmdProxyMap.get(new Integer(i))).notifyProxy(this.proxyMap, this.spmdProxyMap,
                        this.userProxyMap);
            }
        } else {
            throw new IndexOutOfBoundsException(" No MPI job exists with num " + jobID);
        }
    }

    public void deployUserClasses(int jobID, Node[] orderedNodes) {
        //    get the list of classes to instanciate for this MPISpmd object
        // 	  and send it as parameter.
        ArrayList classes = ((MPISpmd) mpiSpmdList.get(jobID)).getClasses();
        if (!classes.isEmpty()) {
            MPI_IMPL_LOGGER.info("[MANAGER] JobID #" + jobID + " deploy user classes");
            // get the table of parameters
            Hashtable paramsTable = ((MPISpmd) mpiSpmdList.get(jobID)).getClassesParams();
            Hashtable<String, Object[]> userProxyList = new Hashtable<String, Object[]>();
            Iterator iterator = classes.iterator();
            while (iterator.hasNext()) {
                String cl = (String) iterator.next();
                try {
                    Object[] parameters = (Object[]) paramsTable.get(cl);
                    Object[] proxyList = new Object[parameters.length];
                    for (int i = 0; i < parameters.length; i++) {
                        Object[] params = (Object[]) parameters[i];
                        if (params != null) {
                            proxyList[i] = PAActiveObject.newActive(cl, params, orderedNodes[i]);
                        }
                    }
                    userProxyList.put(cl, proxyList);
                    this.userProxyMap.put(new Integer(jobID), userProxyList);
                } catch (ActiveObjectCreationException e) {
                    e.printStackTrace();
                } catch (NodeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void deployUserSpmdClasses(int jobID, Node[] orderedNodes) {
        //  get the list of SPMD class to instanciate for this MPISpmd object
        // 	  and send it as parameter.
        ArrayList classes = ((MPISpmd) mpiSpmdList.get(jobID)).getSpmdClasses();
        if (!classes.isEmpty()) {
            MPI_IMPL_LOGGER.info("[MANAGER] JobID #" + jobID + " deploy user SPMD classes");
            // get the table of parameters
            Hashtable paramsTable = ((MPISpmd) mpiSpmdList.get(jobID)).getSpmdClassesParams();
            Hashtable<String, Object> userProxyList = new Hashtable<String, Object>();
            Iterator iterator = classes.iterator();
            while (iterator.hasNext()) {
                String cl = (String) iterator.next();
                try {
                    ArrayList parameters = (ArrayList) paramsTable.remove(cl);

                    // simple array parameter
                    if (parameters.get(0) != null) {
                        Object[] params = (Object[]) parameters.get(0);
                        Object[][] p = new Object[orderedNodes.length][];
                        for (int i = 0; i < orderedNodes.length; i++) {
                            p[i] = params;
                        }
                        userProxyList.put(cl, PASPMD.newSPMDGroup(cl, p, orderedNodes));
                    } // matrix parameter 
                    else if (parameters.get(1) != null) {
                        Object[][] params = (Object[][]) parameters.get(1);
                        userProxyList.put(cl, PASPMD.newSPMDGroup(cl, params, orderedNodes));
                    } // no parameters 
                    else {
                        Object[][] params = new Object[orderedNodes.length][];
                        userProxyList.put(cl, PASPMD.newSPMDGroup(cl, params, orderedNodes));
                    }
                    this.userProxyMap.put(new Integer(jobID), userProxyList);
                } catch (ClassNotReifiableException e) {
                    e.printStackTrace();
                } catch (ActiveObjectCreationException e) {
                    e.printStackTrace();
                } catch (NodeException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } // end_try
            } // end_while
        } // end_if_classes
    }

    public void register(int jobID) {
        // ack of job is null means we can start MPI application
        if (ackToStart[jobID] == 0) {
            MPISpmd mpiSpmd = (MPISpmd) mpiSpmdList.get(jobID);
            MPIResult res = mpiSpmd.startMPI();

            // the prinln generate a deadlock
            //System.out.println(mpiSpmd);
        } else {
            ackToStart[jobID]--;
        }
    }

    public void unregister(int jobID, int rank) {
        if (jobID < currentJobNumber) {
            (this.proxyMap.get(new Integer(jobID)))[rank] = null;
            MPI_IMPL_LOGGER.info("[MANAGER] JobID #" + jobID + " unregister mpi process #" + rank);
            for (int i = 0; i < currentJobNumber; i++) {
                int jobListLength = (this.proxyMap.get(new Integer(i))).length;
                for (int j = 0; j < jobListLength; j++) {
                    if ((this.proxyMap.get(new Integer(i)))[j] != null) {
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

    ////////////////////////////////////////////////////////////
    ////////// IF MANAGER IS USED AS A PROXY ///////////////////
    ////////////////////////////////////////////////////////////
    //    public void sendMessageToComm(int jobID, MessageRecv m_r) {
    //        int dest = m_r.getDest();
    //        if (jobID < proxyTabByJob.size()) {
    //            ProActiveMPICoupling[] tabOfComm = (ProActiveMPICoupling[]) proxyTabByJob.get(new Integer(
    //                        jobID));
    //            if ((dest < tabOfComm.length) && (tabOfComm[dest] != null)) {
    //                tabOfComm[dest].receiveFromMpi(m_r);
    //
    //                //                System.out.println(
    //                //                    "[JOBMANAGER]sendMessageToComm> One message received from : " +
    //                //                    m_r.getSrc() + " Destinator is :" + dest + " Job: " +
    //                //                    jobID);
    //                // System.out.println(" Message is :" + m_r);
    //            } else {
    //                throw new IndexOutOfBoundsException(
    //                    " ActiveProxyComm destinator " + dest + " is unreachable!");
    //            }
    //        } else {
    //            throw new IndexOutOfBoundsException(" No MPI job exists with num " +
    //                jobID);
    //        }
    //    }
    //
    //    public void allSendMessageToComm(int jobID, MessageRecv m_r) {
    //        if (jobID < proxyTabByJob.size()) {
    //            ProActiveMPICoupling[] allDest = (ProActiveMPICoupling[]) proxyTabByJob.get(new Integer(
    //                        jobID));
    //            for (int i = 0; i < allDest.length; i++) {
    //                if (allDest[i] != null) {
    //                    allDest[i].receiveFromMpi(m_r);
    //                } else {
    //                    System.out.println(
    //                        "[JOBMANAGER]allSendMessageToComm> on destinator is null  : " +
    //                        i + " Job: " + jobID);
    //                }
    //            }
    //            System.out.println("[JOBMANAGER]allSendMessageToComm>  to Job: " +
    //                jobID);
    //        } else {
    //            throw new IndexOutOfBoundsException(" No MPI job exists with num " +
    //                jobID);
    //        }
    //    }
    //    
}
