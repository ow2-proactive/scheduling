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

import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class ProActiveMPIComm {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.MPI_CONTROL_COUPLING);
    private String hostname = "NULL";
    private volatile boolean shouldRun = true;

    /* my proxy */
    private ProActiveMPICoupling myProxy;

    // rank of mpi process in the job
    private int myRank = -1;
    private boolean notify = true;
    private int jobID;
    private int count;

    /* job manager */
    private ProActiveMPIManager manager;

    ////////////////////////
    //// NATIVE METHODS ////
    ////////////////////////
    private native int initRecvQueue();

    private native int initSendQueue();

    private native int sendJobNb(int jobNumber);

    private native int init(String userPath, int r);

    private native int closeQueue();

    private native int closeAllQueues();

    private native int sendRequest(ProActiveMPIData m_r, byte[] bs);

    private native byte[] recvRequest(ProActiveMPIData m_r);

    public native int proActiveSendRequest(ProActiveMPIData m_r, byte[] bs);

    ////////////////////////////////
    //// CONSTRUCTOR METHODS    ////
    ////////////////////////////////
    public ProActiveMPIComm() {
    }

    public ProActiveMPIComm(String libName, int uniqueID) {
        try {
            hostname = URIBuilder.getLocalAddress().getHostName();
            logger.info("[REMOTE PROXY] [" + this.hostname +
                "] Constructor> : Loading library.");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.loadLibrary(libName);
        logger.info("[REMOTE PROXY] [" + this.hostname +
            "] Constructor> : Library loaded.");
        // initialize semaphores & log files
        this.init(uniqueID);
    }

    ////////////////////////////////
    //// INTERNAL METHODS ////
    ////////////////////////////////
    public void initQueues() {
        logger.info("[REMOTE PROXY] [" + this.hostname +
            "] initQueues> : init receiving queue: " + initRecvQueue());

        logger.info("[REMOTE PROXY] [" + this.hostname +
            "] initQueues> : init sending queue: " + initSendQueue());
    }

    public void closeQueues() {
        logger.info("[REMOTE PROXY] [" + this.hostname +
            "] closeQueues> : closeQueue: " + closeQueue());
    }

    public void closeAllSRQueues() {
        logger.info("[REMOTE PROXY] [" + this.hostname +
            "] closeAllSRQueues> : closeAllQueues: " + closeAllQueues());
    }

    public void createRecvThread() {
        this.createThread();
    }

    public void createThread() {
        Runnable r = new MessageRecvHandler();
        Thread t = new Thread(r, "Thread Message Recv");
        t.start();
    }

    public void sendJobNumberAndRegister() {
        sendJobNumber(jobID);
        this.manager.register(this.jobID, myRank);
    }

    public void wakeUpThread() {
        logger.info("[REMOTE PROXY] [" + this.hostname +
            "] activeThread> : activate thread");
        this.notify = true;
    }

    public void asleepThread() {
        this.notify = false;
    }

    ////////////////////////////////
    //// INITIALIZATION METHODS ////
    ////////////////////////////////
    public void setMyProxy(ProActiveMPICoupling myProxy,
        ProActiveMPIManager jobManager, int idJob) {
        this.myProxy = myProxy;
        this.jobID = idJob;
        this.manager = jobManager;
    }

    public void init(int uniqueID) {
        logger.info("[REMOTE PROXY] [" + this.hostname + "] init> : init: " +
            init(System.getProperty("user.home"), uniqueID));
        this.closeAllSRQueues();
    }

    ////////////////////////////////
    //// COMMUNICATION METHODS  ////
    ////////////////////////////////
    public void receiveFromProActive(ProActiveMPIData m_r) {
        proActiveSendRequest(m_r, m_r.getData());
    }

    public void sendJobNumber(int jobNumber) {
        logger.info("[REMOTE PROXY] [" + this.hostname +
            "] sendJobNumber> send job number " + sendJobNb(jobNumber));
    }

    public void receiveFromMpi(ProActiveMPIData m_r) {
        if (m_r.getData() == null) {
            throw new RuntimeException("[REMOTE PROXY] !!! DATA are null ");
        }

        // byte[]
        sendRequest(m_r, m_r.getData());
    }

    public int getMyRank() {
        return myRank;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n Class: ");
        sb.append(this.getClass().getName());
        sb.append("\n Hostname: " + this.hostname);
        sb.append("\n rank: " + this.myRank);
        return sb.toString();
    }

    /**
     * This class reads all messages from the message queue C2S
     */
    protected class MessageRecvHandler implements Runnable {
        public MessageRecvHandler() {
        }

        public void run() {
            //      signal the job manager that this daemon is ok to recv message
            myProxy.register();
            ProActiveMPIData m_r = new ProActiveMPIData();
            byte[] data;
            Ack ack = new Ack();
            count = 0;
            while (shouldRun) {
                if (notify) {
                    try {
                        if ((data = recvRequest(m_r)) == null) {
                            throw new RuntimeException(
                                "[REMOTE PROXY] !!! ERROR data received are NULL from native method");
                        }

                        //check TAG1
                        if (m_r.getTag1() == ProActiveMPIConstants.COMM_MSG_INIT) {
                            myRank = m_r.getSrc();
                            myProxy.registerProcess(myRank);
                            asleepThread();
                        } else if (m_r.getTag1() == ProActiveMPIConstants.COMM_MSG_SEND) {
                            m_r.setData(data);
                            int jobRecver = m_r.getjobID();
                            m_r.setJobID(jobID);
                            count++;
                            if ((count % 1000) == 0) {
                                // wait for old acknowledge
                                PAFuture.waitFor(ack);
                                // create new Acknowledge
                                ack = myProxy.sendToMpi(jobRecver, m_r, false);
                            } else {
                                myProxy.sendToMpi(jobRecver, m_r);
                            }
                        } else if (m_r.getTag1() == ProActiveMPIConstants.COMM_MSG_SEND_PROACTIVE) {
                            //                      	        System.out.println("[" + hostname +
                            //                                  "] TREAD] RECVING MESSAGE-> SENDING");
                            m_r.setData(data);
                            int jobRecver = m_r.getjobID();
                            m_r.setJobID(jobID);
                            m_r.parseParameters();
                            myProxy.sendToProActive(jobRecver, m_r);
                        } else if (m_r.getTag1() == ProActiveMPIConstants.COMM_MSG_ALLSEND) {
                            m_r.setData(data);
                            int jobRecver = m_r.getjobID();
                            m_r.setJobID(jobID);
                            myProxy.allSendToMpi(jobRecver, m_r);
                        } else if (m_r.getTag1() == ProActiveMPIConstants.COMM_MSG_FINALIZE) {
                            closeQueues();
                            myProxy.unregisterProcess(myRank);
                            shouldRun = false;
                        } else {
                            logger.info("[REMOTE PROXY]  TAG UNKNOWN ");
                        }
                    } catch (Exception e) {
                        System.out.println("In Java:\n\t" + e);
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
