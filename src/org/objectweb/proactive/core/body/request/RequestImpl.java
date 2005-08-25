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
package org.objectweb.proactive.core.body.request;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.FutureResult;
import org.objectweb.proactive.core.body.message.MessageImpl;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.reply.ReplyImpl;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;
import org.objectweb.proactive.ext.security.ProActiveSecurity;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;

import sun.rmi.server.MarshalInputStream;


public class RequestImpl extends MessageImpl implements Request,
    java.io.Serializable {
    public static Logger logger = Logger.getLogger(RequestImpl.class.getName());
    public static Logger loggerNFE = Logger.getLogger("NFE");
    protected MethodCall methodCall;

    /**
     * Indicates if the method has been sent through a forwarder
     */
    protected int sendCounter;

    /** transient because we deal with the serialization of this variable
       in a custom manner. see writeObject method*/
    protected transient UniversalBody sender;
    private transient ProActiveSecurityManager psm;
    private byte[][] methodCallCiphered;
    private byte[] methodCallCipheredSignature;
    public long sessionID;
    protected String codebase;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public RequestImpl(MethodCall methodCall, UniversalBody sender,
        boolean isOneWay, long nextSequenceID) {
        super(sender.getID(), nextSequenceID, isOneWay, methodCall.getName());
        this.methodCall = methodCall;
        this.sender = sender;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- Implements Request -----------------------------------------------
    //
    public int send(UniversalBody destinationBody)
        throws java.io.IOException, RenegotiateSessionException {
        //System.out.println("RequestSender: sendRequest  " + methodName + " to destination");
        sendCounter++;
        return sendRequest(destinationBody);
    }

    public UniversalBody getSender() {
        return sender;
    }

    public Reply serve(Body targetBody) throws ServeException {
        if (logger.isDebugEnabled()) {
            logger.debug("Serving " + this.getMethodName());
        }
        FutureResult result = serveInternal(targetBody);
        if (logger.isDebugEnabled()) {
            logger.debug("result: " + result);
        }
        if (isOneWay) { // || (sender == null)) {
            return null;
        }
        return createReply(targetBody, result);
    }

    public Reply serveAlternate(Body targetBody, NonFunctionalException nfe) {
        if (loggerNFE.isDebugEnabled()) {
            loggerNFE.debug("*** Serving an alternate version of " +
                this.getMethodName());
            if (nfe != null) {
                loggerNFE.debug("*** Result  " + nfe.getClass().getName());
            } else {
                loggerNFE.debug("*** Result null");
            }
        }
        if (isOneWay) { // || (sender == null)) {
            return null;
        }
        return createReply(targetBody, new FutureResult(null, null, nfe));
    }

    public boolean hasBeenForwarded() {
        return sendCounter > 1;
    }

    public void resetSendCounter() {
        this.sendCounter = 0;
    }

    public Object getParameter(int index) {
        return methodCall.getParameter(index);
    }

    public MethodCall getMethodCall() {
        return methodCall;
    }

    public void notifyReception(UniversalBody bodyReceiver)
        throws java.io.IOException {
        if (!hasBeenForwarded()) {
            return;
        }

        //System.out.println("the request has been forwarded times");
        //we know c.res is a remoteBody since the call has been forwarded
        //if it is null, this is a one way call
        if (sender != null) {
            sender.updateLocation(bodyReceiver.getID(),
                bodyReceiver.getRemoteAdapter());
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected FutureResult serveInternal(Body targetBody)
        throws ServeException {
        Object result = null;
        Throwable exception = null;
        try {
            //loggerNFE.warn("CALL to " + targetBody);
            result = methodCall.execute(targetBody.getReifiedObject());
        } catch (MethodCallExecutionFailedException e) {
            // e.printStackTrace();
            throw new ServeException("serve method " +
                methodCall.getReifiedMethod().toString() + " failed", e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            exception = e.getTargetException();
            if (isOneWay) {

                /* ExceptionListException are handled by group users */
                if (!(exception instanceof ExceptionListException)) {
                    throw new ServeException("serve method " +
                        methodCall.getReifiedMethod().toString() + " failed",
                        exception);
                }
            }
        }

        return new FutureResult(result, exception, null);
    }

    protected Reply createReply(Body targetBody, FutureResult result) {
        ProActiveSecurityManager psm = null;
        try {
            psm = ProActive.getBodyOnThis().getProActiveSecurityManager();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (SecurityNotAvailableException e) {
            // do nothing
        }

        return new ReplyImpl(targetBody.getID(), sequenceNumber, methodName,
            result, psm);
    }

    protected int sendRequest(UniversalBody destinationBody)
        throws java.io.IOException, RenegotiateSessionException {
        if (logger.isDebugEnabled()) {
            logger.debug(" sending request " + methodCall.getName());
        }
        try {
            if (!ciphered && !hasBeenForwarded()) {
                sessionID = 0;

                if (sender == null) {
                    logger.warn("sender is null but why ?");
                }

                this.psm = sender.getProActiveSecurityManager();

                byte[] certE = destinationBody.getCertificateEncoded();
                X509Certificate cert = ProActiveSecurity.decodeCertificate(certE);
                sessionID = psm.getSessionIDTo(cert);
                System.out.println("session ID is : " + sessionID);
                if (sessionID != 0) {
                    methodCallCiphered = psm.encrypt(sessionID, methodCall);
                    ciphered = true;
                    methodCall = null;

                    System.out.println("methodcallciphered " +
                        methodCallCiphered + ", ciphered " + ciphered +
                        ", methodCall " + methodCall);
                }
            }
        } catch (SecurityNotAvailableException e) {
            // do nothing
            //e.printStackTrace();
            logger.debug("Request : security disabled");
        }

        int ftres = destinationBody.receiveRequest(this);

        if (logger.isDebugEnabled()) {
            logger.debug(" sending request finished");
        }

        return ftres;
    }

    // security issue
    public boolean isCiphered() {
        return ciphered;
    }

    public boolean decrypt(ProActiveSecurityManager psm)
        throws RenegotiateSessionException {
        //  String localCodeBase = null;
        //     if (ciphered) {
        if (ciphered) {
            try {
                //System.out.println("PSM " + psm);
                byte[] decryptedMethodCall = psm.decrypt(sessionID,
                        methodCallCiphered);

                // System.out.println("ReceiveRequest :method call apres decryption : " +  ProActiveSecurityManager.displayByte(decryptedMethodCall));
                ByteArrayInputStream bin = new ByteArrayInputStream(decryptedMethodCall);
                MarshalInputStream in = new MarshalInputStream(bin);

                // ObjectInputStream in = new ObjectInputStream(bin);
                methodCall = (MethodCall) in.readObject();
                in.close();
                ciphered = false;

                //  logger.info("After decoding method call  seq id " +sequenceNumber + ":" + ciphered + ":" + sessionID + "  "+ methodCall + ":" +methodCallCiphered);
                return true;
            } catch (ClassNotFoundException e) {
                int index = e.toString().indexOf(':');
                String className = e.toString().substring(index).trim();
                className = className.substring(2);

                //			   		//		try {
                //  MOPClassLoader currentClassLoader =	org.objectweb.proactive.core.mop.MOPClassLoader.createMOPClassLoader();
                // this.getClass().getClassLoader().loadClass(className);
                //    currentClassLoader.loadClass(className);  
                this.decrypt(psm);

                //		} catch (ClassNotFoundException ex) {
                //		e.printStackTrace();
                //	}
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //    System.setProperty("java.rmi.server.codebase",localCodeBase);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.request.Request#getSessionId()
     */
    public long getSessionId() {
        return sessionID;
    }

    //
    // -- PRIVATE METHODS FOR SERIALIZATION -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        out.defaultWriteObject();
        out.writeObject(sender.getRemoteAdapter());
    }

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        sender = (UniversalBody) in.readObject(); // it is actually a UniversalBody
    }
}
