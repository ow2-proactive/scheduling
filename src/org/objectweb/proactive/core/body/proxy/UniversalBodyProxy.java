/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.body.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Active;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.exceptions.manager.ExceptionHandler;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.mop.ConstructorCallImpl;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;


public class UniversalBodyProxy extends AbstractBodyProxy implements java.io.Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.BODY);

    // note that we do not want to serialize this member but rather handle
    // the serialization by ourselve
    protected transient UniversalBody universalBody;
    protected transient boolean isLocal;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Empty, no args constructor
     */
    public UniversalBodyProxy() {
    }

    /**
     * Instantiates an object of class BodyProxy, creates a body object
     * (referenced either via the instance variable <code>localBody</code>
     * or <code>remoteBody</code>) and passes the ConstructorCall
     * object <code>c</code> to the body, which will then handle
     * the creation of the reified object (That's it !).
     * parameter contains either :
     *    &lt;Node, Active, MetaObjectFactory>
     * or
     *    &lt;UniversalBody>
     */
    public UniversalBodyProxy(ConstructorCall constructorCall,
        Object[] parameters) throws ProActiveException {
        Object p0 = parameters[0];

        // Determines whether the body is local or remote
        if (p0 instanceof UniversalBody) {
            // This is simple connection to an existant local body
            this.universalBody = (UniversalBody) p0;
            this.bodyID = universalBody.getID();
            isLocal = LocalBodyStore.getInstance().getLocalBody(bodyID) != null;
            if (logger.isDebugEnabled()) {
                //logger.debug("UniversalBodyProxy created from UniversalBody bodyID="+bodyID+" isLocal="+isLocal);
            }
        } else {
            // instantiate the body locally or remotely
            Class bodyClass = Constants.DEFAULT_BODY_CLASS;
            Node node = (Node) p0;

            //added lines--------------------------
            //ProActiveRuntime part = node.getProActiveRuntime();
            //added lines----------------------------
            Active activity = (Active) parameters[1];
            MetaObjectFactory factory = (MetaObjectFactory) parameters[2];
            String jobID = (String) parameters[3];
            Class[] argsClass = new Class[] {
                    ConstructorCall.class, String.class, Active.class,
                    MetaObjectFactory.class, String.class
                };
            Object[] args = new Object[] {
                    constructorCall, node.getNodeInformation().getURL(),
                    activity, factory, jobID
                };

            //added lines--------------------------
            //Object[] args = new Object[] { constructorCall, node.getNodeInformation().getURL(), activity, factory };
            //added lines--------------------------
            ConstructorCall bodyConstructorCall = buildBodyConstructorCall(bodyClass,
                    argsClass, args);
            if (NodeFactory.isNodeLocal(node)) {
                // the node is local
                //added line -------------------------
                //if (RuntimeFactory.isRuntimeLocal(part)){
                //added line -------------------------
                this.universalBody = createLocalBody(bodyConstructorCall,
                        constructorCall, node);
                isLocal = true;
            } else {
                this.universalBody = createRemoteBody(bodyConstructorCall, node);
                //added line -------------------------
                //this.universalBody = createRemoteBody(bodyConstructorCall, part , node);
                //added line -------------------------
                isLocal = false;
            }
            this.bodyID = universalBody.getID();
            if (logger.isDebugEnabled()) {
                //logger.debug("UniversalBodyProxy created from constructorCall bodyID="+bodyID+" isLocal="+isLocal);
            }
        }
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public boolean equals(Object o) {
        if (!(o instanceof UniversalBodyProxy)) {
            return false;
        }

        UniversalBodyProxy proxy = (UniversalBodyProxy) o;
        return universalBody.equals(proxy.universalBody);
    }

    public int hashCode() {
        return universalBody.hashCode();
    }

    //
    // -- implements BodyProxy interface -----------------------------------------------
    //
    public UniversalBody getBody() {
        return universalBody;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected UniversalBody createLocalBody(
        ConstructorCall bodyConstructorCall,
        ConstructorCall reifiedObjectConstructorCall, Node node)
        throws ProActiveException {
        try {
            reifiedObjectConstructorCall.makeDeepCopyOfArguments();

            //The node is local, so is the proActiveRuntime
            // acessing it direclty avoids to get a copy of the body
            ProActiveRuntime part = ProActiveRuntimeImpl.getProActiveRuntime();

            //return (UniversalBody) bodyConstructorCall.execute();
            //---------------------added lines--------------------------
            //			if (logger.isDebugEnabled()) {
            //						logger.debug("LocalBodyProxy created using " + body + " from ConstructorCall");
            //			}
            return part.createBody(node.getNodeInformation().getName(),
                bodyConstructorCall, true);
            //---------------------added lines------------------------------
        } catch (ConstructorCallExecutionFailedException e) {
            throw new ProActiveException(e);
        } catch (InvocationTargetException e) {
            throw new ProActiveException(e.getTargetException());
        } catch (java.io.IOException e) {
            throw new ProActiveException("Error in the copy of the arguments of the constructor",
                e);
        }
    }

    protected UniversalBody createRemoteBody(
        ConstructorCall bodyConstructorCall, Node node)
        throws ProActiveException {
        try {
            ProActiveRuntime part = node.getProActiveRuntime();

            //if (logger.isDebugEnabled()) {
            //            //logger.debug("UniversalBodyProxy.createRemoteBody bodyClass="+bodyClass+"  node="+node);
            //}
            //return node.createBody(bodyConstructorCall);
            //--------------added lines
            if (logger.isDebugEnabled()) {
                logger.debug("RemoteBodyProxy created bodyID=" + bodyID +
                    " from ConstructorCall");
            }
            return part.createBody(node.getNodeInformation().getName(),
                bodyConstructorCall, false);
            //--------------added lines
        } catch (ConstructorCallExecutionFailedException e) {
            throw new ProActiveException(e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new ProActiveException(e);
        } catch (NodeException e) {
            throw new ProActiveException(e);
        }
    }

    protected void sendRequest(MethodCall methodCall, Future future)
        throws java.io.IOException, RenegotiateSessionException {
        // Determines the body that is at the root of the subsystem from which the
        // call was sent.
        // It is always true that the body that issued the request (and not the body
        // that is the target of the call) and this BodyProxy are in the same
        // address space because being a local representative for something remote
        // is what the proxy is all about. This is why we know that the table that
        // can be accessed by using a static methode has this information.
        ExceptionHandler.addRequest(methodCall, (FutureProxy) future);
        try {
            sendRequest(methodCall, future,
                LocalBodyStore.getInstance().getCurrentThreadBody());
        } catch (java.io.IOException ioe) {
            if (future != null) {
                /* (future == null) happens on one-way calls */
                ExceptionHandler.addResult((FutureProxy) future);
            }
            throw ioe;
        }
    }

    protected void sendRequest(MethodCall methodCall, Future future,
        Body sourceBody)
        throws java.io.IOException, RenegotiateSessionException {
        // TODO if component request and shortcut : update body ref
        // Now we check whether the reference to the remoteBody has changed i.e the body has migrated
        // Maybe we could use some optimisation here
        UniversalBody newBody = sourceBody.checkNewLocation(universalBody.getID());
        if (newBody != null) {
            universalBody = newBody;
            isLocal = LocalBodyStore.getInstance().getLocalBody(bodyID) != null;
        }
        sourceBody.getFuturePool().registerDestination(universalBody);
        if (isLocal) {
            // Replaces the effective arguments with a deep copy
            // Only do this if the body is local
            // For remote bodies, this is automatically handled by the RMI stub
            methodCall.makeDeepCopyOfArguments();
        }
        sendRequestInternal(methodCall, future, sourceBody);
        sourceBody.getFuturePool().removeDestination();
    }

    protected void sendRequestInternal(MethodCall methodCall, Future future,
        Body sourceBody)
        throws java.io.IOException, RenegotiateSessionException {
        sourceBody.sendRequest(methodCall, future, universalBody);
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private ConstructorCall buildBodyConstructorCall(Class bodyClass,
        Class[] argsClass, Object[] args) throws ProActiveException {
        // Determines the constructor of the body object: it is the constructor that
        // has only one argument, this argument being of type ConstructorCall
        try {
            Constructor cstr = bodyClass.getConstructor(argsClass);

            // A word of explanation: here we have two nested ConstructorCall objects:
            // 'bodyConstructorCall' is the reification of the construction of the body,
            // which contains another ConstructorCall object that represents the reification
            // of the construction of the reified object itself.
            return new ConstructorCallImpl(cstr, args);
        } catch (NoSuchMethodException e) {
            throw new ProActiveException("Class " + bodyClass.getName() +
                " has no constructor matching ", e);
        }
    }

    public boolean isLocal() {
        return this.isLocal;
    }

    //
    // -- SERIALIZATION -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        if (this.universalBody == null) {
            out.writeObject(null);
        } else {
            out.writeObject(universalBody.getRemoteAdapter());
        }
    }

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        Body localBody = LocalBodyStore.getInstance().getLocalBody(bodyID);

        // Thread.dumpStack();
        if (logger.isDebugEnabled()) {
            logger.debug("Local body is " + localBody);
        }
        if (localBody != null) {
            // the body is local
            universalBody = localBody;
            in.readObject();
            isLocal = true;
        } else {
            // the body is not local
            universalBody = (UniversalBody) in.readObject();
            isLocal = false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("universalBody is " + universalBody);
        }
    }
}
