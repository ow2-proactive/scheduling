/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.core.body.ibis;

import ibis.rmi.RemoteException;

/**
 *   An adapter for a LocalBody to be able to receive remote calls. This helps isolate RMI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe remote objects library.
 */
import org.apache.log4j.Logger;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.rmi.RandomPortSocketFactory;


public class IbisRemoteBodyImpl extends ibis.rmi.server.UnicastRemoteObject
    implements IbisRemoteBody, java.rmi.server.Unreferenced {
    protected static Logger logger = Logger.getLogger(IbisRemoteBodyImpl.class.getName());

    /**
     * A custom socket Factory
     */
    protected static RandomPortSocketFactory factory = new RandomPortSocketFactory(37002,
            5000);

    /**
     * The encapsulated local body
     * transient to deal with custom serialization of requests.
     */
    protected transient UniversalBody body;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public IbisRemoteBodyImpl() throws RemoteException {
    }

    public IbisRemoteBodyImpl(UniversalBody body) throws RemoteException {
        //   super(0, factory, factory);
        if (logger.isDebugEnabled()) {
            logger.debug(" IbisRemoteBodyImpl<init> ");
        }
        this.body = body;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements IbisRemoteBody -----------------------------------------------
    //
    public void receiveRequest(Request r) throws java.io.IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("body  = " + body);
            logger.debug("request =  " + r.getMethodName());
        }
        body.receiveRequest(r);
    }

    public void receiveReply(Reply r) throws java.io.IOException {
        body.receiveReply(r);
    }

    public String getNodeURL() {
        return body.getNodeURL();
    }

    public UniqueID getID() {
        return body.getID();
    }

    public void updateLocation(UniqueID id, UniversalBody remoteBody)
        throws java.io.IOException {
        body.updateLocation(id, remoteBody);
    }

    public void unreferenced() {
        if (logger.isDebugEnabled()) {
            // logger.debug("IbisRemoteBodyImpl: unreferenced()");      
        }
        System.gc();
    }

    public void enableAC() throws java.io.IOException {
        body.enableAC();
    }

    public void disableAC() throws java.io.IOException {
        body.disableAC();
    }

    public void setImmediateService(String methodName)
        throws java.io.IOException {
        body.setImmediateService(methodName);
    }

    /**
     * @see org.objectweb.proactive.core.body.jini.JiniBody#getProActiveComponent()
     */
    public ProActiveComponent getProActiveComponent()
        throws java.io.IOException {
        // COMPONENTS
        return body.getProActiveComponent();
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    //
    // -- SERIALIZATION -----------------------------------------------
    //

    /*
       private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
       long startTime=System.currentTimeMillis();
       out.defaultWriteObject();
       long endTime=System.currentTimeMillis();
       if (logger.isDebugEnabled()) {
       logger.debug(" SERIALIZATION OF REMOTEBODYIMPL lasted " + (endTime - startTime));
       }
       }
       private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
       in.defaultReadObject();
       }
     */
}
