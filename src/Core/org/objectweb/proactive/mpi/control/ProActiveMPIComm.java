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

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.ProActiveInet;
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

    /**
     * Send information regarding current job to the native code. 
     * @param jobNumber The current jobId
     * @param nbJob The number of processes involved in this job.
     * @return
     */
    private native int sendJobNb(int jobNumber, int nbJob);

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
        hostname = ProActiveInet.getInstance().getInetAddress().getHostName();
        if (logger.isInfoEnabled()) {
            logger.info("[REMOTE PROXY] [" + this.hostname + "] Constructor> : Loading library.");
        }

        if (logger.isInfoEnabled()) {
            logger.info(System.getProperty("java.library.path"));
        }
        System.loadLibrary(libName);
        if (logger.isInfoEnabled()) {
            logger.info("[REMOTE PROXY] [" + this.hostname + "] Constructor> : Library loaded.");
        }
        //  initialize semaphores & log files
        this.init(uniqueID);
    }

    ////////////////////////////////
    //// INTERNAL METHODS ////
    ////////////////////////////////
    public void initQueues() {
        int res = initRecvQueue();
        if (logger.isInfoEnabled()) {
            logger.info("[REMOTE PROXY] [" + this.hostname + "] initQueues> : init receiving queue: " + res);
        }

        res = initSendQueue();

        if (logger.isInfoEnabled()) {
            logger.info("[REMOTE PROXY] [" + this.hostname + "] initQueues> : init sending queue: " + res);
        }
    }

    public void closeQueues() {
        int res = closeQueue();

        if (logger.isInfoEnabled()) {
            logger.info("[REMOTE PROXY] [" + this.hostname + "] closeQueues> : closeQueue: " + res);
        }
    }

    public void closeAllSRQueues() {
        int res = closeAllQueues();

        if (logger.isInfoEnabled()) {
            logger.info("[REMOTE PROXY] [" + this.hostname + "] closeAllSRQueues> : closeAllQueues: " + res);
        }
    }

    public void createRecvThread() {
        this.createThread();
    }

    public void createThread() {
        Runnable r = new MessageRecvHandler();
        Thread t = new Thread(r, "Thread Message Recv");
        t.start();
    }

    public void sendJobNumberAndRegister(int nbJob) {
        sendJobNumber(jobID, nbJob);
        this.manager.register(this.jobID, myRank);
    }

    public void wakeUpThread() {
        if (logger.isInfoEnabled()) {
            logger.info("[REMOTE PROXY] [" + this.hostname + "] activeThread> : activate thread");
        }
        this.notify = true;
    }

    public void asleepThread() {
        this.notify = false;
    }

    ////////////////////////////////
    //// INITIALIZATION METHODS ////
    ////////////////////////////////
    public void setMyProxy(ProActiveMPICoupling myProxy, ProActiveMPIManager jobManager, int idJob) {
        this.myProxy = myProxy;
        this.jobID = idJob;
        this.manager = jobManager;
    }

    public void init(int uniqueID) {
        int res = init(System.getProperty("user.home"), uniqueID);
        if (logger.isInfoEnabled()) {
            logger.info("[REMOTE PROXY] [" + this.hostname + "] init> : init: " + res);
        }
        this.closeAllSRQueues();
    }

    ////////////////////////////////
    //// COMMUNICATION METHODS  ////
    ////////////////////////////////
    public int receiveFromProActive(ProActiveMPIData m_r) {
        if (logger.isDebugEnabled()) {
            logger.debug("[REMOTE PROXY] [" + this.hostname + "," + m_r.getDest() +
                "] is receiving a request from " + m_r.getSrc());
        }
        int res = proActiveSendRequest(m_r, m_r.getData());

        return res;
    }

    public void sendJobNumber(int jobNumber, int nbJob) {
        sendJobNb(jobNumber, nbJob);
        if (logger.isInfoEnabled()) {
            logger.info("[REMOTE PROXY] [" + this.hostname + "] sendJobNumber> setting jobId as(" +
                jobNumber + ")");
        }
    }

    public void receiveFromMpi(ProActiveMPIData m_r) {
        if (m_r.getData() == null) {
            throw new RuntimeException("[REMOTE PROXY] !!! DATA are null ");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[REMOTE PROXY] [" + this.hostname + "]  receiveFromMpi> received message" + m_r);
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
            myProxy.nativeInterfaceReady();
            ProActiveMPIData m_r = new ProActiveMPIData();
            byte[] data;
            String prefix = "[PA/MPI] [" + ProActiveMPIComm.this.hostname + "] > ";
            count = 0;
            while (shouldRun) {
                if (notify) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(prefix + " waiting for message ");
                    }

                    try {
                        if ((data = recvRequest(m_r)) == null) {
                            throw new RuntimeException(prefix +
                                " !!! ERROR data received are NULL from native method");
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug(prefix + " received a message  " + m_r.toString(prefix));
                            }
                        }
                        //check msg_type
                        if (m_r.getMsgType() == ProActiveMPIConstants.COMM_MSG_INIT) {
                            myRank = m_r.getSrc();
                            myProxy.register(myRank);

                            if (logger.isDebugEnabled()) {
                                logger.debug(prefix + " received init message  (" + jobID + "," + myRank +
                                    ")");
                            }

                            asleepThread();
                        } else if (m_r.getMsgType() == ProActiveMPIConstants.COMM_MSG_NF) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(prefix + " received Non-Functional message " +
                                    m_r.toString(prefix));
                            }
                        } else if (m_r.getMsgType() == ProActiveMPIConstants.COMM_MSG_SEND) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(prefix + " [START] sending message to remote mpi\n" +
                                    m_r.toString(prefix));
                            }

                            m_r.setData(data);
                            int jobRecver = m_r.getjobID();
                            m_r.setJobID(jobID);
                            count++;
                            if ((count % 1000) == 0) {
                                // wait for old acknowledge
                                if (logger.isDebugEnabled()) {
                                    logger.debug(prefix +
                                        " [MIDDLE] sending message to remote mpi with ack\n" +
                                        m_r.toString(prefix));
                                }

                                myProxy.sendToMpi(jobRecver, m_r);
                                if (logger.isDebugEnabled()) {
                                    logger.debug(prefix + " [END] sending message to remote mpi with ack\n" +
                                        m_r.toString(prefix));
                                }

                            } else {

                                myProxy.sendToMpi(jobRecver, m_r);

                                if (logger.isDebugEnabled()) {
                                    logger.debug(prefix + " [END] sending message to remote mpi\n" +
                                        m_r.toString(prefix));
                                }
                            }
                        } else if (m_r.getMsgType() == ProActiveMPIConstants.COMM_MSG_SEND_PROACTIVE) {
                            m_r.setData(data);
                            int jobRecver = m_r.getjobID();

                            if (logger.isDebugEnabled()) {
                                logger.debug(prefix + " [START] sending message to proactive\n" +
                                    m_r.toString(prefix));
                            }

                            m_r.setJobID(jobID);
                            m_r.parseParameters();

                            myProxy.sendToProActive(jobRecver, m_r);

                            if (logger.isDebugEnabled()) {
                                logger.debug(prefix + " [END] sending message to proactive\n" +
                                    m_r.toString(prefix));
                            }
                        } else if (m_r.getMsgType() == ProActiveMPIConstants.COMM_MSG_ALLSEND) {
                            m_r.setData(data);

                            if (logger.isDebugEnabled()) {
                                logger
                                        .debug(prefix + " [START] all sending message\n" +
                                            m_r.toString(prefix));
                            }

                            int jobRecver = m_r.getjobID();
                            m_r.setJobID(jobID);

                            myProxy.allSendToMpi(jobRecver, m_r);

                            if (logger.isDebugEnabled()) {
                                logger.debug(prefix + " [END] all sending message\n" + m_r.toString(prefix));
                            }
                        } else if (m_r.getMsgType() == ProActiveMPIConstants.COMM_MSG_FINALIZE) {
                            closeQueues();
                            myProxy.unregisterProcess(myRank);
                            shouldRun = false;
                        } else {
                            if (logger.isInfoEnabled()) {
                                logger.info(prefix + " msg_type UNKNOWN  " + m_r.toString(prefix));
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("In Java:\n\t" + e);
                        if (logger.isDebugEnabled()) {
                            logger.debug(prefix + " exception in Message recv handler " + e);
                        }
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
