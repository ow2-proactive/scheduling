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
package org.objectweb.proactive.extensions.mixedlocation;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.gc.GCMessage;
import org.objectweb.proactive.core.gc.GCResponse;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;


public class UniversalBodyWrapper implements UniversalBody, Runnable {
    protected UniversalBody wrappedBody;
    protected long time;
    protected UniqueID id;
    protected boolean stop;
    protected long creationTime;
    protected String jobID;

    //protected  Thread t ;

    /**
     * Create a time-limited wrapper around a UniversalBody
     * @param body the wrapped UniversalBody
     * @param time the life expectancy of this wrapper in milliseconds
     */
    public UniversalBodyWrapper(UniversalBody body, long time) {
        this.wrappedBody = body;
        this.time = time;
        this.creationTime = System.currentTimeMillis();
        //     t =new Thread(this);
        this.id = this.wrappedBody.getID();

        //   t.start();
    }

    public int receiveRequest(Request request)
        throws IOException, RenegotiateSessionException {
        //       System.out.println("UniversalBodyWrapper.receiveRequest");
        if (this.wrappedBody == null) {
            throw new IOException();
        }

        //the forwarder should be dead
        if (System.currentTimeMillis() > (this.creationTime + this.time)) {
            //   this.updateServer();
            //   this.wrappedBody = null;
            //	t.start();
            //   System.gc();
            throw new IOException();
        } else {
            try {
                return this.wrappedBody.receiveRequest(request);
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }

        //      this.stop();
    }

    public int receiveReply(Reply r) throws IOException {
        return this.wrappedBody.receiveReply(r);
    }

    public String getNodeURL() {
        return this.wrappedBody.getNodeURL();
    }

    public UniqueID getID() {
        return this.id;
    }

    public String getJobID() {
        if (this.jobID == null) {
            this.jobID = this.wrappedBody.getJobID();
        }

        return this.jobID;
    }

    public void updateLocation(UniqueID id, UniversalBody body)
        throws IOException {
        this.wrappedBody.updateLocation(id, body);
    }

    public UniversalBody getRemoteAdapter() {
        return this.wrappedBody.getRemoteAdapter();
    }

    public String getReifiedClassName() {
        return this.wrappedBody.getReifiedClassName();
    }

    public void enableAC() throws java.io.IOException {
        this.wrappedBody.enableAC();
    }

    public void disableAC() throws java.io.IOException {
        this.wrappedBody.disableAC();
    }

    protected void updateServer() {
        //        System.out.println("UniversalBodyWrapper.updateServer");
        //  LocationServer server = LocationServerFactory.getLocationServer();
        //        try {
        //            server.updateLocation(id, this.wrappedBody);
        //        } catch (Exception e) {
        //            System.out.println("XXXX Error XXXX");
        //           // e.printStackTrace();
        //        }
    }

    //protected synchronized void stop() {
    // this.stop=true;
    // this.notifyAll();
    //}
    //
    //protected synchronized void waitForStop(long time) {
    //	if (!this.stop) {
    //	 try {
    //		 wait(time);
    //	} catch (InterruptedException e) {
    //		e.printStackTrace();
    //	}
    //	}
    //
    //}
    public void run() {
        //        System.out.println("UniversalBodyWrapper.run life expectancy " + time);
        try {
            // Thread.currentThread().sleep(time);
            //  this.waitForStop(time);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //        System.out.println("UniversalBodyWrapper.run end of life...");
        this.updateServer();
        this.wrappedBody = null;
        //        System.gc();
    }

    // SECURITY
    public void terminateSession(long sessionID)
        throws java.io.IOException, SecurityNotAvailableException {
        this.wrappedBody.terminateSession(sessionID);
    }

    public X509Certificate getCertificate()
        throws java.io.IOException, SecurityNotAvailableException {
        return this.wrappedBody.getCertificate();
    }

    public long startNewSession(Communication policy)
        throws java.io.IOException, RenegotiateSessionException,
            SecurityNotAvailableException {
        return this.wrappedBody.startNewSession(policy);
    }

    public PublicKey getPublicKey()
        throws java.io.IOException, SecurityNotAvailableException {
        return this.wrappedBody.getPublicKey();
    }

    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws IOException, SecurityNotAvailableException,
            RenegotiateSessionException {
        return this.wrappedBody.randomValue(sessionID, cl_rand);
    }

    public byte[][] publicKeyExchange(long sessionID, byte[] my_pub,
        byte[] my_cert, byte[] sig_code)
        throws IOException, SecurityNotAvailableException,
            RenegotiateSessionException, KeyExchangeException {
        return this.wrappedBody.publicKeyExchange(sessionID, my_pub, my_cert,
            sig_code);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
        byte[] tmp2, byte[] tmp3, byte[] tmp4)
        throws IOException, SecurityNotAvailableException,
            RenegotiateSessionException {
        return this.wrappedBody.secretKeyExchange(sessionID, tmp, tmp1, tmp2,
            tmp3, tmp4);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificateEncoded()
     */
    public byte[] getCertificateEncoded()
        throws IOException, SecurityNotAvailableException {
        return this.wrappedBody.getCertificateEncoded();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicy(org.objectweb.proactive.ext.security.SecurityContext)
     */
    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        return this.wrappedBody.getPolicy(securityContext);
    }

    public ArrayList<Entity> getEntities()
        throws SecurityNotAvailableException, IOException {
        return this.wrappedBody.getEntities();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveFTMessage(org.objectweb.proactive.core.body.ft.internalmsg.FTMessage)
     */
    public Object receiveFTMessage(FTMessage ev) throws IOException {
        return this.wrappedBody.receiveFTMessage(ev);
    }

    public GCResponse receiveGCMessage(GCMessage msg) throws IOException {
        return this.wrappedBody.receiveGCMessage(msg);
    }

    public void setRegistered(boolean registered) throws IOException {
        this.wrappedBody.setRegistered(registered);
    }

    public void createShortcut(Shortcut shortcut) throws IOException {
        // TODO implement
        throw new ProActiveRuntimeException(
            "create shortcut method not implemented yet");
    }

    public void register(String url)
        throws IOException, UnknownProtocolException {
        this.wrappedBody.register(url);
    }
}
