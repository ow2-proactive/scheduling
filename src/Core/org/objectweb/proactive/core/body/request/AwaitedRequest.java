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
package org.objectweb.proactive.core.body.request;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.exception.ProtocolErrorException;
import org.objectweb.proactive.core.body.ft.message.MessageInfo;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityEntity;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class defines a particular request that is subject to <i>wait-by-necessity</i>
 * mecanism. It only contains the Id of the awaited sender. When this awaited sender
 * eventually send a request, the corresponding awaited request is updated, and then
 * behaves as a normal ProActive request.
 * If an active object tries to serve an awaited request that is not yet updated, it's
 * activity is suspended until the awaited request is updated.
 * This request can be considered as a "future of request".
 * (@see org.objectweb.proactive.core.body.future.Future)
 * @author cdelbe
 * @since ProActive 2.2
 */
public class AwaitedRequest implements Request, java.io.Serializable {

    /** Logger */
    public static Logger logger = ProActiveLogger.getLogger(Loggers.REQUESTS);

    // awaited sender
    private UniqueID awaitedSender;

    // wrapped Request
    private Request wrappedRequest;
    private boolean isArrived;

    //Non Functional Requests
    protected boolean isNFRequest = false;
    protected int nfRequestPriority;

    /**
     * Create a new awaited request.
     * @param awaitedSender the id of the awaited sender.
     */
    public AwaitedRequest(UniqueID awaitedSender) {
        this.awaitedSender = awaitedSender;
        this.isArrived = false;
    }

    /**
     * Update this awaited request with a received request from the awaited sender.
     * @param r the received request.
     */
    public synchronized void setAwaitedRequest(Request r) {
        //System.err.println("[AWAITED] Request is updated by " + this.awaitedSender + " with " + r);
        //if(!(r.getSourceBodyID().equals(this.awaitedSender))){
        //    logger.error(" **ERROR** : update request is not from the awaited sender !");
        //}
        this.wrappedRequest = r;
        this.isArrived = true;
        notifyAll();
    }

    /**
     * Return the id of the awaited sender
     * @return the id of the awaited sender
     */
    public UniqueID getAwaitedSender() {
        return this.awaitedSender;
    }

    /**
     * Serve the request. This method is subject to wait-by-necessity mechanism.
     * @return the reply resulting from the service of this request.
     */
    public Reply serve(Body targetBody) {
        waitForRequest();
        return wrappedRequest.serve(targetBody);
    }

    /*
     * Wait for a request from the awaited sender.
     */
    private synchronized void waitForRequest() {
        while (!isArrived) {
            try {
                //                //if (logger.isDebugEnabled()) {
                //                UniqueID waiter = ProActive.getBodyOnThis().getID();
                //                    logger.info("[WAIT] " + waiter + " is waiting for a request from " +
                //                        this.awaitedSender);
                //                //}
                this.wait(3000);

                if (!isArrived) {
                    UniqueID waiter = PAActiveObject.getBodyOnThis().getID();
                    logger.info("[WAIT] " + waiter + " is waiting for a request from " + this.awaitedSender);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //// WRAPPED METHODS
    public MethodCall getMethodCall() {
        if (this.isArrived) {
            return this.wrappedRequest.getMethodCall();
        } else {
            return null;
        }
    }

    public Object getParameter(int index) {
        return wrappedRequest.getParameter(index);
    }

    public UniversalBody getSender() {
        return wrappedRequest.getSender();
    }

    public boolean hasBeenForwarded() {
        return wrappedRequest.hasBeenForwarded();
    }

    public void resetSendCounter() {
        throw new ProtocolErrorException("An active object is trying to send an awaited request");
    }

    public void notifyReception(UniversalBody bodyReceiver) throws IOException {
        wrappedRequest.notifyReception(bodyReceiver);
    }

    public int send(UniversalBody destinationBody) throws IOException, RenegotiateSessionException {
        throw new ProtocolErrorException("An active object is trying to send an awaited request");
    }

    public String getMethodName() {
        if (!this.isArrived) {
            return "Awaited Request from " + this.awaitedSender;
        } else {
            return wrappedRequest.getMethodName();
        }
    }

    public UniqueID getSourceBodyID() {
        return this.awaitedSender;
    }

    public long getTimeStamp() {
        return wrappedRequest.getTimeStamp();
    }

    public boolean isOneWay() {
        return wrappedRequest.isOneWay();
    }

    public long getSequenceNumber() {
        if (!this.isArrived) {
            return 0;
        } else {
            return wrappedRequest.getSequenceNumber();
        }
    }

    public void setMessageInfo(MessageInfo mi) {
        wrappedRequest.setMessageInfo(mi);
    }

    public MessageInfo getMessageInfo() {
        if (!this.isArrived) {
            return null;
        } else {
            return wrappedRequest.getMessageInfo();
        }
    }

    public void setIgnoreIt(boolean ignore) {
        wrappedRequest.setIgnoreIt(ignore);
    }

    public boolean ignoreIt() {
        return wrappedRequest.ignoreIt();
    }

    public void setFTManager(FTManager ft) {
    }

    public FTManager getFTManager() {
        return null;
    }

    public boolean isCiphered() {
        return this.wrappedRequest.isCiphered();
    }

    public long getSessionId() {
        return this.wrappedRequest.getSessionId();
    }

    public boolean decrypt(ProActiveSecurityManager psm) throws RenegotiateSessionException {
        return this.wrappedRequest.decrypt(psm);
    }

    public boolean crypt(ProActiveSecurityManager psm, SecurityEntity destinationBody)
            throws RenegotiateSessionException {
        return this.wrappedRequest.crypt(psm, destinationBody);
    }

    //
    // -- Methods dealing with Non Functional Requests
    //
    public boolean isFunctionalRequest() {
        return isNFRequest;
    }

    public void setFunctionalRequest(boolean isFunctionalRequest) {
        this.isNFRequest = isFunctionalRequest;
    }

    public void setNFRequestPriority(int nfReqPriority) {
        this.nfRequestPriority = nfReqPriority;
    }

    public int getNFRequestPriority() {
        return nfRequestPriority;
    }
}
