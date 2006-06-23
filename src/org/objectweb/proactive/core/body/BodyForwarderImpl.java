/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.body;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeForwarderImpl;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


/**
 * A BodyForwarderImpl is not a Body but a forwarder. It is a singleton, one and
 * only one body forwarder on each forwarder.
 *
 * Each method in UniversalBody as an equivalent in UniversalBody, same name, same return
 * type, same arguments but as first argument an ID is passed. This ID allows to a body
 * forwarder to identify uniquely the body to which forward a call.
 *
 * @author ProActive Team
 */
public class BodyForwarderImpl implements UniversalBodyForwarder {

    /** All bodies, read BodyAdapter, known by the forwarder */
    private HashMap bodies; // <UniqueID, BodyAdapter>

    /** Bodies which have been created through this forwarder
     *
     * This table is used to perform intra-cluster optimization. Generally all nodes
     * behind the same forwarder can talk between them directly.
     */
    private HashMap createdBodies; // <UniqueID, null>

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public BodyForwarderImpl() {
        bodies = new HashMap();
        createdBodies = new HashMap();
    }

    /**
     * Add rBody to the collection of known bodies
     * @param rBody The body to be added
     */
    public synchronized void add(UniversalBody rBody) {
        if (!bodies.containsKey(rBody.getID())) {
            // We do not want to overwrite an already existing entry. 
            // Not checking that this entry do not already exist can lead to
            // a deadlock
            bodies.put(rBody.getID(), rBody);
        }
    }

    /**
     * Add id to the collection of body created through this forwarder
     * @param id ID of created body
     */
    public synchronized void addcreatedBody(UniqueID id) {
        createdBodies.put(id, null);
    }

    //
    // --- Method call forwarding
    //

    /** @see UniversalBody#createShortcut(Shortcut) */
    public void createShortcut(UniqueID id, Shortcut shortcut)
        throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            rbody.createShortcut(shortcut);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see UniversalBody#disableAC() */
    public void disableAC(UniqueID id) throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            rbody.disableAC();
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see UniversalBody#enableAC() */
    public void enableAC(UniqueID id) throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            rbody.enableAC();
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see org.objectweb.proactive.ext.security.SecurityEntity#getCertificate() */
    public X509Certificate getCertificate(UniqueID id)
        throws SecurityNotAvailableException, IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            return rbody.getCertificate();
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see org.objectweb.proactive.ext.security.SecurityEntity#getCertificate() */
    public byte[] getCertificateEncoded(UniqueID id)
        throws SecurityNotAvailableException, IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            return rbody.getCertificateEncoded();
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see org.objectweb.proactive.ext.security.SecurityEntity#getEntities() */
    public ArrayList getEntities(UniqueID id)
        throws SecurityNotAvailableException, IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            return rbody.getEntities();
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see UniversalBody#getID() */
    public UniqueID getID(UniqueID id) {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            return rbody.getID();
        } else {
            bodyLogger.info(
                "Cannot retieve associated BodyAdapter: Invalid ID " + id);
            return null;
        }
    }

    /** @see UniversalBody#getNodeURL() */
    public String getNodeURL(UniqueID id) {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            return rbody.getNodeURL();
        } else {
            bodyLogger.info(
                "Cannot retieve associated BodyAdapter: Invalid ID " + id);
            return null;
        }
    }

    /** @see org.objectweb.proactive.ext.security.SecurityEntity#getPolicy(SecurityContext) */
    public SecurityContext getPolicy(UniqueID id,
        SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            return rbody.getPolicy(securityContext);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see org.objectweb.proactive.ext.security.SecurityEntity#getPublicKey() */
    public PublicKey getPublicKey(UniqueID id)
        throws SecurityNotAvailableException, IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            return rbody.getPublicKey();
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see UniversalBody#getRemoteAdapter() */
    public BodyAdapter getRemoteAdapter(UniqueID id) {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        ProActiveRuntimeForwarderImpl partf = (ProActiveRuntimeForwarderImpl) ProActiveRuntimeImpl.getProActiveRuntime();
        return new BodyAdapterForwarder(partf.getBodyAdapterForwarder(), rbody);
    }

    /** @see org.objectweb.proactive.ext.security.SecurityEntity#publicKeyExchange(long, byte[], byte[], byte[]) */
    public byte[][] publicKeyExchange(UniqueID id, long sessionID,
        byte[] my_pub, byte[] my_cert, byte[] sig_code)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            KeyExchangeException, IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            return rbody.publicKeyExchange(sessionID, my_pub, my_cert, sig_code);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see UniversalBody#receiveFTMessage(FTMessage) */
    public Object receiveFTMessage(UniqueID id, FTMessage ev)
        throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            return rbody.receiveFTMessage(ev);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see UniversalBody#receiveReply(Reply) */
    public int receiveReply(UniqueID id, Reply r) throws IOException {
        // a FuturProxy can be present inside r and must know if it is running
        // on a body forwarder or not. See FuturProxy.writeObject
        FuturePool.addMeAsBodyForwarder();
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.receiveReply(r);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id +
                    " request=" + r);
            }
        } finally {
            FuturePool.removeMeFromBodyForwarders();
        }
    }

    /** @see UniversalBody#receiveRequest(Request) */
    public int receiveRequest(UniqueID id, Request request)
        throws IOException, RenegotiateSessionException {
        // a FuturProxy can be present inside r and must know if it is running
        // on a body forwarder or not. See FuturProxy.writeObject
        FuturePool.addMeAsBodyForwarder();
        try {
            Object o = bodies.get(id);
            BodyAdapter rbody = (BodyAdapter) o;
            if (rbody != null) {
                return rbody.receiveRequest(request);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } finally {
            FuturePool.removeMeFromBodyForwarders();
        }
    }

    /** @see UniversalBody#removeImmediateService(String, Class[]) */
    public void removeImmediateService(UniqueID id, String methodName,
        Class[] parametersTypes) throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            rbody.removeImmediateService(methodName, parametersTypes);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see org.objectweb.proactive.ext.security.SecurityEntity#secretKeyExchange(long, byte[], byte[], byte[], byte[], byte[]) */
    public byte[][] secretKeyExchange(UniqueID id, long sessionID, byte[] tmp,
        byte[] tmp1, byte[] tmp2, byte[] tmp3, byte[] tmp4)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            return rbody.secretKeyExchange(sessionID, tmp, tmp1, tmp2, tmp3,
                tmp4);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see UniversalBody#setImmediateService(String, Class[]) */
    public void setImmediateService(UniqueID id, String methodName,
        Class[] parametersTypes) throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            rbody.setImmediateService(methodName, parametersTypes);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see UniversalBody#setImmediateService(String) */
    public void setImmediateService(UniqueID id, String methodName)
        throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            rbody.setImmediateService(methodName);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see org.objectweb.proactive.ext.security.SecurityEntity#startNewSession(Communication) */
    public long startNewSession(UniqueID id, Communication policy)
        throws SecurityNotAvailableException, IOException,
            RenegotiateSessionException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            return rbody.startNewSession(policy);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see UniversalBody#terminate() */
    public void terminate(UniqueID id) throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            // TODO probably remove this AO from our cache
            rbody.terminate();
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see org.objectweb.proactive.ext.security.SecurityEntity#terminateSession(long) */
    public void terminateSession(UniqueID id, long sessionID)
        throws IOException, SecurityNotAvailableException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            rbody.terminateSession(sessionID);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see org.objectweb.proactive.ext.security.SecurityEntity#randomValue(long, byte[]) */
    public byte[] randomValue(UniqueID id, long sessionID, byte[] cl_rand)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            return rbody.randomValue(sessionID, cl_rand);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see UniversalBody#updateLocation(UniqueID, UniversalBody) */
    public void updateLocation(UniqueID id, UniqueID uid, UniversalBody body)
        throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            rbody.updateLocation(uid, body);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see org.objectweb.proactive.Job#getJobID() */
    public String getJobID(UniqueID id) throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            return rbody.getJobID();
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see org.objectweb.proactive.core.exceptions.manager.NFEProducer#addNFEListener(NFEListener) */
    public void addNFEListener(UniqueID id, NFEListener listener)
        throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            rbody.addNFEListener(listener);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see org.objectweb.proactive.core.exceptions.manager.NFEProducer#removeNFEListener(NFEListener) */
    public void removeNFEListener(UniqueID id, NFEListener listener)
        throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            rbody.removeNFEListener(listener);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see BodyAdapter#changeProxiedBody(Body) */
    public void changeProxiedBody(UniqueID id, Body newBody)
        throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            rbody.changeProxiedBody(newBody);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see org.objectweb.proactive.core.exceptions.manager.NFEProducer#fireNFE(NonFunctionalException) */
    public int fireNFE(UniqueID id, NonFunctionalException e)
        throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            return rbody.fireNFE(e);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see BodyAdapter#lookup(String) */
    public UniversalBody lookup(UniqueID id, String url)
        throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            return rbody.lookup(url);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see BodyAdapter#register(String) */
    public void register(UniqueID id, String url) throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            rbody.register(url);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    /** @see BodyAdapter#unregister(String) */
    public void unregister(UniqueID id, String url) throws IOException {
        BodyAdapter rbody = (BodyAdapter) bodies.get(id);
        if (rbody != null) {
            rbody.unregister(url);
        } else {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }
}
