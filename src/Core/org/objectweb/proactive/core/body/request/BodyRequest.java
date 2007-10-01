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
package org.objectweb.proactive.core.body.request;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.message.MessageImpl;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityEntity;


public class BodyRequest extends MessageImpl implements Request,
    java.io.Serializable {
    protected MethodCall methodCall;
    protected boolean isPriority;
    protected boolean isNFRequest;
    protected int nfRequestPriority;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public BodyRequest(Body targetBody, String methodName,
        Class[] paramClasses, Object[] params, boolean isPriority)
        throws NoSuchMethodException {
        super(null, 0, true, methodName);
        if (paramClasses == null) {
            paramClasses = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                paramClasses[i] = params[i].getClass();
            }
        }
        methodCall = MethodCall.getMethodCall(targetBody.getClass()
                                                        .getMethod(methodName,
                    paramClasses), params, (Map<TypeVariable, Class>) null);
        this.isPriority = isPriority;
    }

    //Non functional BodyRequests constructor
    public BodyRequest(Body targetBody, String methodName,
        Class[] paramClasses, Object[] params, boolean isNFRequest,
        int nfRequestPriority) throws NoSuchMethodException {
        super(null, 0, true, methodName);
        if (paramClasses == null) {
            paramClasses = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                paramClasses[i] = params[i].getClass();
            }
        }
        methodCall = MethodCall.getMethodCall(targetBody.getClass()
                                                        .getMethod(methodName,
                    paramClasses), params, (Map<TypeVariable, Class>) null);

        this.isNFRequest = isNFRequest;
        this.nfRequestPriority = nfRequestPriority;
    }

    // SECURITY
    public boolean isCiphered() {
        return false;
    }

    public boolean decrypt(ProActiveSecurityManager psm) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.request.Request#getSessionId()
     */
    public long getSessionId() {
        return 0;
    }

    public boolean crypt(ProActiveSecurityManager psm,
        SecurityEntity destinationBody) {
        return true;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- Implements Request -----------------------------------------------
    //
    public int send(UniversalBody destinationBody) throws java.io.IOException {
        int ftres;
        if (!(destinationBody instanceof Body)) {
            throw new java.io.IOException(
                "The destination body is not a local body");
        }
        if (!isPriority) {
            ftres = ((Body) destinationBody).getRequestQueue().add(this);
        } else {
            ftres = ((Body) destinationBody).getRequestQueue().addToFront(this);
        }
        return ftres;
    }

    public Reply serve(Body targetBody) {
        serveInternal(targetBody);
        return null;
    }

    @Override
    public boolean isOneWay() {
        return true;
    }

    public boolean hasBeenForwarded() {
        return false;
    }

    public void resetSendCounter() {
    }

    public UniversalBody getSender() {
        return null;
    }

    public Object getParameter(int index) {
        return methodCall.getParameter(index);
    }

    public MethodCall getMethodCall() {
        return methodCall;
    }

    public void notifyReception(UniversalBody bodyReceiver)
        throws java.io.IOException {
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected void serveInternal(Body targetBody) {
        try {
            methodCall.execute(targetBody);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (MethodCallExecutionFailedException e) {
            e.printStackTrace();
        }
    }

    //
    // -- METHODS DEALING WITH NON FUNCTIONAL REQUESTS
    //
    public boolean isFunctionalRequest() {
        return this.isNFRequest;
    }

    public void setFunctionalRequest(boolean isFunctionalRequest) {
        this.isNFRequest = isFunctionalRequest;
    }

    public void setNFRequestPriority(int nfReqPriority) {
        this.nfRequestPriority = nfReqPriority;
    }

    public int getNFRequestPriority() {
        return this.nfRequestPriority;
    }
}
