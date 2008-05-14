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

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;


public class ProActiveMPICoupling implements Serializable, InitActive {

    /** global Manager*/
    private ProActiveMPIManager manager;

    /** Comm object it refers */
    private ProActiveMPIComm target;

    /*  Hashtable<jobID, Hashtable<class, PASPMD user class || user proxy array>> */
    private Hashtable userProxyMap;

    // job # managed by the Job Manager
    private int jobID;

    /*  Hashtable<jobID, ProActiveCoupling []> */
    private static Hashtable proxyMap;

    /*  Hashtable<jobID, PASPMD ProActiveMPICoupling> */
    private Hashtable spmdProxyMap;

    ////////////////////////////////
    //// CONSTRUCTOR METHODS    ////
    ////////////////////////////////
    public ProActiveMPICoupling() {
    }

    public ProActiveMPICoupling(String libName, ProActiveMPIManager manager, Integer jobNum)
            throws ActiveObjectCreationException, NodeException, ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        this.manager = manager;
        this.jobID = jobNum.intValue();
        target = new ProActiveMPIComm(libName, PAActiveObject.getBodyOnThis().getID().hashCode());
    }

    public void initActivity(Body body) {
        // update proxy ref 
        this.target.setMyProxy((ProActiveMPICoupling) PAActiveObject.getStubOnThis(), this.manager,
                this.jobID);
    }

    ///////////////////////////////
    ////  PROXY OUTING METHODS ////
    ///////////////////////////////
    public void registerProcess(int rank) {
        this.manager.register(this.jobID, rank, (ProActiveMPICoupling) PAActiveObject.getStubOnThis());
    }

    public void register() {
        this.manager.register(this.jobID);
    }

    public void register(int rank) {
        this.manager.register(this.jobID, rank);
    }

    public void unregisterProcess(int rank) {
        this.manager.unregister(this.jobID, rank);
    }

    public void receiveFromMpi(ProActiveMPIData m_r) {
        this.target.receiveFromMpi(m_r);
    }

    public void receiveFromProActive(ProActiveMPIData m_r) {
        this.target.receiveFromProActive(m_r);
    }

    public void sendToMpi(int jobID, ProActiveMPIData m_r) throws IOException {
        int dest = m_r.getDest();
        if (jobID < proxyMap.size()) {
            ProActiveMPICoupling[] arrayComm = (ProActiveMPICoupling[]) proxyMap.get(new Integer(jobID));
            if ((dest < arrayComm.length) && (arrayComm[dest] != null)) {
                arrayComm[dest].receiveFromMpi(m_r);
            } else {
                throw new IndexOutOfBoundsException(" ActiveProxyComm destinator " + dest +
                    " is unreachable!");
            }
        } else {
            throw new IndexOutOfBoundsException(" No MPI job exists with num " + jobID);
        }
    }

    public Ack sendToMpi(int jobID, ProActiveMPIData m_r, boolean b) throws IOException {
        this.sendToMpi(jobID, m_r);
        return new Ack();
    }

    public static void MPISend(byte[] buf, int count, int datatype, int dest, int tag, int jobID) {
        //create Message to send and use the native method
        ProActiveMPIData m_r = new ProActiveMPIData();

        m_r.setData(buf);
        m_r.setCount(count);
        m_r.setDatatype(datatype);
        m_r.setDest(dest);
        m_r.setTag(tag);
        m_r.setJobID(jobID);
        if (jobID < proxyMap.size()) {
            ProActiveMPICoupling[] arrayComm = (ProActiveMPICoupling[]) proxyMap.get(new Integer(jobID));
            if ((dest < arrayComm.length) && (arrayComm[dest] != null)) {
                arrayComm[dest].receiveFromProActive(m_r);
            } else {
                throw new IndexOutOfBoundsException(" ActiveProxyComm destinator " + dest +
                    " is unreachable!");
            }
        } else {
            throw new IndexOutOfBoundsException(" No MPI job exists with num " + jobID);
        }
    }

    /////////////////////////////////
    ////  PROXY ENTERING METHODS ////
    /////////////////////////////////
    public Ack initEnvironment() {
        //    	initialize the send & recv queues
        this.target.initQueues();
        return new Ack();
    }

    public void createRecvThread() {
        this.target.createRecvThread();
    }

    @SuppressWarnings("unchecked")
    public void notifyProxy(Hashtable jobList, Hashtable groupList, Hashtable userProxyMap) {
        proxyMap = jobList;
        spmdProxyMap = groupList;
        this.userProxyMap = userProxyMap;
        this.target.sendJobNumberAndRegister();
    }

    public void wakeUpThread() {
        this.target.wakeUpThread();
    }

    ///////////////////////////
    ////  GETTER METHODS   ////
    ///////////////////////////
    public Node getNode() throws NodeException {
        return NodeFactory.getNode(PAActiveObject.getBodyOnThis().getNodeURL());
    }

    public void allSendToMpi(int jobID, ProActiveMPIData m_r) {
        if (jobID < spmdProxyMap.size()) {
            ProActiveMPICoupling groupDest = (ProActiveMPICoupling) spmdProxyMap.get(new Integer(jobID));
            groupDest.receiveFromMpi(m_r);
        } else {
            throw new IndexOutOfBoundsException(" MPI job with such ID: " + jobID + " doesn't exist");
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(target.toString());
        sb.append("\n MPIJobNum: " + this.jobID);
        return sb.toString();
    }

    public void sendToProActive(int jobID, ProActiveMPIData m_r) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException,
            ClassNotFoundException {
        int dest = m_r.getDest();
        if (jobID < proxyMap.size()) {
            Hashtable proSpmdByClasses = (Hashtable) this.userProxyMap.get(new Integer(jobID));

            Object proSpmdGroup = proSpmdByClasses.get(m_r.getClazz());

            // if the corresponding object exists, its a -ProSpmd object- or a -proxy-
            if (proSpmdGroup != null) {
                Group<?> g = PAGroup.getGroup(proSpmdByClasses.get(m_r.getClazz()));

                // its a ProSpmd Object
                if (g != null) {
                    // extract the specified object from the group and call method on it
                    (g.get(dest).getClass().getDeclaredMethod(m_r.getMethod(),
                            new Class[] { ProActiveMPIData.class }))
                            .invoke(g.get(dest), new Object[] { m_r });
                } else {
                    if (((Object[]) proSpmdByClasses.get(m_r.getClazz()))[dest] != null) {
                        (((Object[]) proSpmdByClasses.get(m_r.getClazz()))[dest].getClass()
                                .getDeclaredMethod(m_r.getMethod(), new Class[] { ProActiveMPIData.class }))
                                .invoke(((Object[]) proSpmdByClasses.get(m_r.getClazz()))[dest],
                                        new Object[] { m_r });
                    } else {
                        throw new ClassNotFoundException("The Specified User Class *** " + m_r.getClazz() +
                            "*** doesn't exist !!!");
                    }
                }
            }
            // the specified class doesn't exist  
            else {
                throw new ClassNotFoundException("The Specified User Class *** " + m_r.getClazz() +
                    "*** doesn't exist !!!");
            }
        } else {
            throw new IndexOutOfBoundsException(" No MPI job exists with num " + jobID);
        }
    }
}
