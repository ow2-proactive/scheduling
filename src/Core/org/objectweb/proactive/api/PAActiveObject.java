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
package org.objectweb.proactive.api;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Active;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.benchmarks.timit.util.basic.TimItBasicManager;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.Context;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.exceptions.BodyTerminatedException;
import org.objectweb.proactive.core.body.ft.internalmsg.Heartbeat;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.event.NodeCreationEventProducerImpl;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.core.util.NodeCreationListenerForAoCreation;
import org.objectweb.proactive.core.util.NonFunctionalServices;
import org.objectweb.proactive.core.util.ProcessForAoCreation;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.profiling.Profiling;


/**
 * This class provides the main operations on active objects.
 *
 * @author The ProActive Team
 * @since ProActive 3.9 (November 2007)
 */
@PublicAPI
public class PAActiveObject {
    protected final static Logger logger = ProActiveLogger.getLogger(Loggers.CORE);
    private final static Heartbeat hb = new Heartbeat();

    static {
        ProActiveConfiguration.load();
        @SuppressWarnings("unused")
        // Execute RuntimeFactory's static blocks
        Class<?> c = org.objectweb.proactive.core.runtime.RuntimeFactory.class;
    }

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    private PAActiveObject() {
    }

    /**
     * Creates a new ActiveObject based on classname attached to a default node in the local JVM.
     * @param classname the name of the class to instanciate as active
     * @param constructorParameters the parameters of the constructor.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the DefaultNode cannot be created
     */
    public static Object newActive(String classname, Object[] constructorParameters)
            throws ActiveObjectCreationException, NodeException {
        return newActive(classname, null, constructorParameters, (Node) null, null, null);
    }

    /**
     * Creates a new ActiveObject based on classname attached to the node of the given URL.
     * @param classname the name of the class to instanciate as active
     * @param constructorParameters the parameters of the constructor.
     * @param nodeURL the URL of the node where to create the active object. If null, the active object
     *       is created localy on a default node
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node URL cannot be resolved as an existing Node
     */
    public static Object newActive(String classname, Object[] constructorParameters, String nodeURL)
            throws ActiveObjectCreationException, NodeException {
        if (nodeURL == null) {
            return newActive(classname, null, constructorParameters, (Node) null, null, null);
        } else {
            return newActive(classname, null, constructorParameters, NodeFactory.getNode(nodeURL), null, null);
        }
    }

    /**
     * Creates a new ActiveObject based on classname attached to the given node or on
     * a default node in the local JVM if the given node is null.
     * @param classname the name of the class to instanciate as active
     * @param constructorParameters the parameters of the constructor.
     * @param node the possibly null node where to create the active object.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object newActive(String classname, Object[] constructorParameters, Node node)
            throws ActiveObjectCreationException, NodeException {
        return newActive(classname, null, constructorParameters, node, null, null);
    }

    /**
     * Creates a new ActiveObject based on classname attached to a default node in the local JVM.
     * @param classname the name of the class to instanciate as active
     * @param genericParameters parameterizing types (of class @param classname)
     * @param constructorParameters the parameters of the constructor.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the DefaultNode cannot be created
     */
    public static Object newActive(String classname, Class<?>[] genericParameters,
            Object[] constructorParameters) throws ActiveObjectCreationException, NodeException {
        // avoid ambiguity for method parameters types
        Node nullNode = null;
        return newActive(classname, genericParameters, constructorParameters, nullNode, null, null);
    }

    /**
     * Creates a new ActiveObject based on classname attached to the node of the given URL.
     * @param classname the name of the class to instanciate as active
     * @param genericParameters parameterizing types (of class @param classname)
     * @param constructorParameters the parameters of the constructor.
     * @param nodeURL the URL of the node where to create the active object. If null, the active object
     *       is created localy on a default node
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node URL cannot be resolved as an existing Node
     */
    public static Object newActive(String classname, Class<?>[] genericParameters,
            Object[] constructorParameters, String nodeURL) throws ActiveObjectCreationException,
            NodeException {
        if (nodeURL == null) {
            // avoid ambiguity for method parameters types
            Node nullNode = null;
            return newActive(classname, genericParameters, constructorParameters, nullNode, null, null);
        } else {
            return newActive(classname, genericParameters, constructorParameters, NodeFactory
                    .getNode(nodeURL), null, null);
        }
    }

    /**
     * Creates a new ActiveObject based on classname attached to the given node or on
     * a default node in the local JVM if the given node is null.
     * @param classname the name of the class to instanciate as active
     * @param genericParameters parameterizing types (of class @param classname)
     * @param constructorParameters the parameters of the constructor.
     * @param node the possibly null node where to create the active object.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object newActive(String classname, Class<?>[] genericParameters,
            Object[] constructorParameters, Node node) throws ActiveObjectCreationException, NodeException {
        return newActive(classname, genericParameters, constructorParameters, node, null, null);
    }

    /**
     * Creates a new ActiveObject based on classname attached to the given node or on
     * a default node in the local JVM if the given node is null.
     * The object returned is a stub class that extends the target class and that is automatically
     * generated on the fly. The Stub class reference a the proxy object that reference the body
     * of the active object. The body referenced by the proxy can either be local of remote,
     * depending or the respective location of the object calling the newActive and the active object
     * itself.
     * @param classname the name of the class to instanciate as active
     * @param genericParameters parameterizing types (of class @param classname)
     * @param constructorParameters the parameters of the constructor of the object
     *    to instantiate as active. If some parameters are primitive types, the wrapper
     *    class types should be given here. null can be used to specify that no parameter
     *    are passed to the constructor.
     * @param node the possibly null node where to create the active object. If null, the active object
     *       is created localy on a default node
     * @param activity the possibly null activity object defining the different step in the activity of the object.
     *               see the definition of the activity in the javadoc of this classe for more information.
     * @param factory the possibly null meta factory giving all factories for creating the meta-objects part of the
     *                body associated to the reified object. If null the default ProActive MetaObject factory is used.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object newActive(String classname, Class<?>[] genericParameters,
            Object[] constructorParameters, Node node, Active activity, MetaObjectFactory factory)
            throws ActiveObjectCreationException, NodeException {
        if (factory == null) {
            factory = ProActiveMetaObjectFactory.newInstance();
            if (factory.getProActiveSecurityManager() == null) {
                factory.setProActiveSecurityManager(((AbstractBody) PAActiveObject.getBodyOnThis())
                        .getProActiveSecurityManager());
            }
        }

        MetaObjectFactory clonedFactory = factory;

        // TIMING
        // First we must create the timit manager then provide the timit
        // reductor to the MetaObjectFactory, this reductor will be used
        // in BodyImpl for the timing of a body.
        if (Profiling.TIMERS_COMPILED) {
            try {
                if (TimItBasicManager.checkNodeProperties(node)) {
                    // Because we don't want to time the TimItReductor
                    // active object and avoid StackOverflow
                    // we need to check the current activated object
                    // classname

                    //                    // The timit reductor will be passed to the factory
                    //                    // and used when a body is created
                    clonedFactory.setTimItReductor(TimItBasicManager.getInstance().createReductor());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ProActiveSecurityManager factorySM = factory.getProActiveSecurityManager();
        if (factorySM != null) {
            try {
                clonedFactory = (MetaObjectFactory) factory.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            ProActiveSecurityManager psm = clonedFactory.getProActiveSecurityManager();
            psm = psm.generateSiblingCertificate(EntityType.OBJECT, classname);
            clonedFactory.setProActiveSecurityManager(psm);
        }

        //using default proactive node
        if (node == null) {
            node = NodeFactory.getDefaultNode();
        }

        try {
            //          create stub object
            Object stub = MOP.createStubObject(classname, genericParameters, constructorParameters, node,
                    activity, clonedFactory);

            return stub;
        } catch (MOPException e) {
            Throwable t = e;

            if (e.getTargetException() != null) {
                t = e.getTargetException();
            }

            throw new ActiveObjectCreationException(t);
        }
    }

    /**
     * <p>Create a set of active objects  with given construtor parameters.
     * The object activation is optimized by a thread pool.</p>
     * <p>The total of active objects created is equal to the number of nodes
     * and to the total of constructor paramaters also.</p>
     * <p>The condition to use this method is that:
     * <b>constructorParameters.length == nodes.length</b></p>
     *
     * @param className the name of the class to instanciate as active.
     * @param constructorParameters the array that contains the parameters used
     * to build the active objects. All active objects have the same constructor
     * parameters.
     * @param nodes the array of nodes where the active objects are created.
     * @return an array of references (possibly remote) on Stubs of the newly
     * created active objects.
     * @throws ClassNotFoundException in the case of className is not a class.
     */
    public static Object[] newActiveInParallel(String className, Object[][] constructorParameters,
            Node[] nodes) throws ClassNotFoundException {
        return newActiveInParallel(className, null, constructorParameters, nodes);
    }

    /**
     * <p>Create a set of identical active objects on a given virtual node. The
     * object activation is optimized by a thread pool.</p>
     * <p>When the given virtual node is not previously activated, this method
     * employ the node creation event producer/listerner mechanism joined to the
     * thread pool. That aims to create an active object just after the node
     * deploying.</p>
     *
     * @param className the name of the class to instanciate as active.
     * @param constructorParameters the array that contains the parameters used
     * to build the active objects. All active objects have the same constructor
     * parameters.
     * @param virtualNode the virtual node where the active objects are created.
     * @return an array of references (possibly remote) on Stubs of the newly
     * created active objects.
     * @throws NodeException happens when the given virtualNode is already
     * activated and throws an exception.
     * @throws ClassNotFoundException in the case of className is not a class.
     */
    public static Object[] newActiveInParallel(String className, Object[] constructorParameters,
            VirtualNode virtualNode) throws NodeException, ClassNotFoundException {
        return newActiveInParallel(className, null, constructorParameters, virtualNode);
    }

    /**
     * <p>Create a set of active objects  with given construtor parameters.
     * The object activation is optimized by a thread pool.</p>
     * <p>The total of active objects created is equal to the number of nodes
     * and to the total of constructor paramaters also.</p>
     * <p>The condition to use this method is that:
     * <b>constructorParameters.length == nodes.length</b></p>
     *
     * @param className the name of the class to instanciate as active.
     * @param genericParameters genericParameters parameterizing types
     * @param constructorParameters the array that contains the parameters used
     * to build the active objects. All active objects have the same constructor
     * parameters.
     * @param nodes the array of nodes where the active objects are created.
     * @return an array of references (possibly remote) on Stubs of the newly
     * created active objects.
     * @throws ClassNotFoundException in the case of className is not a class.
     */
    public static Object[] newActiveInParallel(String className, Class<?>[] genericParameters,
            Object[][] constructorParameters, Node[] nodes) throws ClassNotFoundException {
        if (constructorParameters.length != nodes.length) {
            throw new ProActiveRuntimeException("The total of constructors must"
                + " be equal to the total of nodes");
        }

        ExecutorService threadPool = Executors.newCachedThreadPool();

        Vector result = new Vector();

        // TODO execute tasks
        // The Virtual Node is already activate
        for (int i = 0; i < constructorParameters.length; i++) {
            threadPool.execute(new ProcessForAoCreation(result, className, genericParameters,
                constructorParameters[i], nodes[i % nodes.length]));
        }

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(PAProperties.PA_COMPONENT_CREATION_TIMEOUT.getValueAsInt(),
                    TimeUnit.SECONDS);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        Class<?> classForResult = Class.forName(className);
        return result.toArray((Object[]) Array.newInstance(classForResult, result.size()));
    }

    /**
     * <p>Create a set of identical active objects on a given virtual node. The
     * object activation is optimized by a thread pool.</p>
     *
     * @param className the name of the class to instanciate as active.
     * @param genericParameters genericParameters parameterizing types
     * @param constructorParameters the array that contains the parameters used
     * to build the active objects. All active objects have the same constructor
     * parameters.
     * @param virtualNode the virtual node where the active objects are created.
     * @return an array of references (possibly remote) on Stubs of the newly
     * created active objects.
     * @throws NodeException happens when the given virtualNode is already
     * activated and throws an exception.
     * @throws ClassNotFoundException in the case of className is not a class.
     */
    public static Object[] newActiveInParallel(String className, Class<?>[] genericParameters,
            Object[] constructorParameters, VirtualNode virtualNode) throws NodeException,
            ClassNotFoundException {
        // Creation of the thread pool
        ExecutorService threadPool = Executors.newCachedThreadPool();

        Vector result = new Vector();
        if (virtualNode.isActivated()) {
            // The Virtual Node is already activate
            Node[] nodes = virtualNode.getNodes();
            for (int i = 0; i < nodes.length; i++) {
                threadPool.execute(new ProcessForAoCreation(result, className, genericParameters,
                    constructorParameters, nodes[i]));
            }
        } else {
            // Use the node creation event mechanism
            ((NodeCreationEventProducerImpl) virtualNode)
                    .addNodeCreationEventListener(new NodeCreationListenerForAoCreation(result, className,
                        genericParameters, constructorParameters, threadPool));
            virtualNode.activate();
            ((VirtualNodeImpl) virtualNode).waitForAllNodesCreation();
        }
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(PAProperties.PA_COMPONENT_CREATION_TIMEOUT.getValueAsInt(),
                    TimeUnit.SECONDS);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        Class<?> classForResult = Class.forName(className);
        return result.toArray((Object[]) Array.newInstance(classForResult, result.size()));
    }

    /**
     * Turns the target object into an ActiveObject attached to a default node in the local JVM.
     * The type of the stub is is the type of the existing object.
     * @param target The object to turn active
     * @return a reference (possibly remote) on a Stub of the existing object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the DefaultNode cannot be created
     */
    public static Object turnActive(Object target) throws ActiveObjectCreationException, NodeException {
        return turnActive(target, (Class<?>[]) null, (Node) null);
    }

    /**
     * Turns the target object into an Active Object and send it to the Node
     * identified by the given url.
     * The type of the stub is is the type of the existing object.
     * @param target The object to turn active
     * @param nodeURL the URL of the node where to create the active object on. If null, the active object
     *       is created localy on a default node
     * @return a reference (possibly remote) on a Stub of the existing object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object turnActive(Object target, String nodeURL) throws ActiveObjectCreationException,
            NodeException {
        if (nodeURL == null) {
            return turnActive(target, null, target.getClass().getName(), null, null, null);
        } else {
            return turnActive(target, null, target.getClass().getName(), NodeFactory.getNode(nodeURL), null,
                    null);
        }
    }

    /**
     * Turns the target object into an Active Object and send it to the given Node
     * or to a default node in the local JVM if the given node is null.
     * The type of the stub is is the type of the target object.
     * @param target The object to turn active
     * @param node The Node the object should be sent to or null to create the active
     *       object in the local JVM
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object turnActive(Object target, Node node) throws ActiveObjectCreationException,
            NodeException {
        return turnActive(target, null, target.getClass().getName(), node, null, null);
    }

    /**
     * Turns the target object into an Active Object and send it to the given Node
     * or to a default node in the local JVM if the given node is null.
     * The type of the stub is is the type of the target object.
     * @param target The object to turn active
     * @param node The Node the object should be sent to or null to create the active
     *       object in the local JVM
     * @param activity the possibly null activity object defining the different step in the activity of the object.
     *               see the definition of the activity in the javadoc of this classe for more information.
     * @param factory the possibly null meta factory giving all factories for creating the meta-objects part of the
     *                body associated to the reified object. If null the default ProActive MataObject factory is used.
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object turnActive(Object target, Node node, Active activity, MetaObjectFactory factory)
            throws ActiveObjectCreationException, NodeException {
        return turnActive(target, null, target.getClass().getName(), node, activity, factory);
    }

    /**
     * Turns a Java object into an Active Object and send it to a remote Node or to a
     * local node if the given node is null.
     * The type of the stub is given by the parameter <code>nameOfTargetType</code>.
     * @param target The object to turn active
     * @param nameOfTargetType the fully qualified name of the type the stub class should
     * inherit from. That type can be less specific than the type of the target object.
     * @param node The Node the object should be sent to or null to create the active
     *       object in the local JVM
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object turnActive(Object target, String nameOfTargetType, Node node)
            throws ActiveObjectCreationException, NodeException {
        return turnActive(target, null, nameOfTargetType, node, null, null);
    }

    /**
     * Turns a Java object into an Active Object and send it to a remote Node or to a
     * local node if the given node is null.
     * The type of the stub is given by the parameter <code>nameOfTargetType</code>.
     * A Stub is dynamically generated for the existing object. The result of the call
     * will be an instance of the Stub class pointing to the proxy object pointing
     * to the body object pointing to the existing object. The body can be remote
     * or local depending if the existing is sent remotely or not.
     * @param target The object to turn active
     * @param nameOfTargetType the fully qualified name of the type the stub class should
     * inherit from. That type can be less specific than the type of the target object.
     * @param node The Node the object should be sent to or null to create the active
     *       object in the local JVM
     * @param activity the possibly null activity object defining the different step in the activity of the object.
     *               see the definition of the activity in the javadoc of this classe for more information.
     * @param factory the possibly null meta factory giving all factories for creating the meta-objects part of the
     *                body associated to the reified object. If null the default ProActive MataObject factory is used.
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object turnActive(Object target, String nameOfTargetType, Node node, Active activity,
            MetaObjectFactory factory) throws ActiveObjectCreationException, NodeException {
        return turnActive(target, null, nameOfTargetType, node, activity, factory);
    }

    /**
     * Turns a Java object into an Active Object and send it to a remote Node or to a
     * local node if the given node is null.
     * The type of the stub is given by the parameter <code>nameOfTargetType</code>.
     * A Stub is dynamically generated for the existing object. The result of the call
     * will be an instance of the Stub class pointing to the proxy object pointing
     * to the body object pointing to the existing object. The body can be remote
     * or local depending if the existing is sent remotely or not.
     * @param target The object to turn active
     * @param genericParameters parameterizing types (of class @param classname)
     * @param nameOfTargetType the fully qualified name of the type the stub class should
     * inherit from. That type can be less specific than the type of the target object.
     * @param node The Node the object should be sent to or null to create the active
     *       object in the local JVM
     * @param activity the possibly null activity object defining the different step in the activity of the object.
     *               see the definition of the activity in the javadoc of this classe for more information.
     * @param factory the possibly null meta factory giving all factories for creating the meta-objects part of the
     *                body associated to the reified object. If null the default ProActive MataObject factory is used.
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object turnActive(Object target, String nameOfTargetType, Class<?>[] genericParameters,
            Node node, Active activity, MetaObjectFactory factory) throws ActiveObjectCreationException,
            NodeException {
        if (factory == null) {
            factory = ProActiveMetaObjectFactory.newInstance();
            if (factory.getProActiveSecurityManager() == null) {
                factory.setProActiveSecurityManager(((AbstractBody) PAActiveObject.getBodyOnThis())
                        .getProActiveSecurityManager());
            }
        }

        ProActiveSecurityManager factorySM = factory.getProActiveSecurityManager();

        MetaObjectFactory clonedFactory = factory;

        if (factorySM != null) {
            try {
                clonedFactory = (MetaObjectFactory) factory.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            clonedFactory.setProActiveSecurityManager(factory.getProActiveSecurityManager()
                    .generateSiblingCertificate(EntityType.OBJECT, nameOfTargetType));

            ProActiveLogger.getLogger(Loggers.SECURITY).debug("new active object with security manager");
        }

        if (node == null) {
            //using default proactive node
            node = NodeFactory.getDefaultNode();
        }

        try {
            return MOP.createStubObject(target, nameOfTargetType, genericParameters, node, activity,
                    clonedFactory);
        } catch (MOPException e) {
            Throwable t = e;

            if (e.getTargetException() != null) {
                t = e.getTargetException();
            }

            throw new ActiveObjectCreationException(t);
        }
    }

    /**
     * Turns the target object into an ActiveObject attached to a default node in the local JVM.
     * The type of the stub is is the type of the existing object.
     * @param target The object to turn active
     * @param genericParameters genericParameters parameterizing types
     * @return a reference (possibly remote) on a Stub of the existing object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the DefaultNode cannot be created
     */
    public static Object turnActive(Object target, Class<?>[] genericParameters)
            throws ActiveObjectCreationException, NodeException {
        return turnActive(target, genericParameters, (Node) null, (Active) null, (MetaObjectFactory) null);
    }

    /**
     * Turns the target object into an Active Object and send it to the Node
     * identified by the given url.
     * The type of the stub is is the type of the existing object.
     * @param target The object to turn active
     * @param genericParameters genericParameters parameterizing types
     * @param nodeURL the URL of the node where to create the active object on. If null, the active object
     *       is created localy on a default node
     * @return a reference (possibly remote) on a Stub of the existing object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object turnActive(Object target, Class<?>[] genericParameters, String nodeURL)
            throws ActiveObjectCreationException, NodeException {
        if (nodeURL == null) {
            return turnActive(target, genericParameters, target.getClass().getName(), null, null, null);
        } else {
            return turnActive(target, genericParameters, target.getClass().getName(), NodeFactory
                    .getNode(nodeURL), null, null);
        }
    }

    /**
     * Turns the target object into an Active Object and send it to the given Node
     * or to a default node in the local JVM if the given node is null.
     * The type of the stub is is the type of the target object.
     * @param target The object to turn active
     * @param genericParameters genericParameters parameterizing types
     * @param node The Node the object should be sent to or null to create the active
     *       object in the local JVM
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object turnActive(Object target, Class<?>[] genericParameters, Node node)
            throws ActiveObjectCreationException, NodeException {
        return turnActive(target, genericParameters, target.getClass().getName(), node, null, null);
    }

    /**
     * Turns the target object into an Active Object and send it to the given Node
     * or to a default node in the local JVM if the given node is null.
     * The type of the stub is is the type of the target object.
     * @param target The object to turn active
     * @param genericParameters genericParameters parameterizing types
     * @param node The Node the object should be sent to or null to create the active
     *       object in the local JVM
     * @param activity the possibly null activity object defining the different step in the activity of the object.
     *               see the definition of the activity in the javadoc of this classe for more information.
     * @param factory the possibly null meta factory giving all factories for creating the meta-objects part of the
     *                body associated to the reified object. If null the default ProActive MataObject factory is used.
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object turnActive(Object target, Class<?>[] genericParameters, Node node, Active activity,
            MetaObjectFactory factory) throws ActiveObjectCreationException, NodeException {
        return turnActive(target, genericParameters, target.getClass().getName(), node, activity, factory);
    }

    /**
     * Turns a Java object into an Active Object and send it to a remote Node or to a
     * local node if the given node is null.
     * The type of the stub is given by the parameter <code>nameOfTargetType</code>.
     * @param target The object to turn active
     * @param genericParameters genericParameters parameterizing types
     * @param nameOfTargetType the fully qualified name of the type the stub class should
     * inherit from. That type can be less specific than the type of the target object.
     * @param node The Node the object should be sent to or null to create the active
     *       object in the local JVM
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object turnActive(Object target, Class<?>[] genericParameters, String nameOfTargetType,
            Node node) throws ActiveObjectCreationException, NodeException {
        return turnActive(target, genericParameters, nameOfTargetType, node, null, null);
    }

    /**
     * Turns a Java object into an Active Object and send it to a remote Node or to a
     * local node if the given node is null.
     * The type of the stub is given by the parameter <code>nameOfTargetType</code>.
     * A Stub is dynamically generated for the existing object. The result of the call
     * will be an instance of the Stub class pointing to the proxy object pointing
     * to the body object pointing to the existing object. The body can be remote
     * or local depending if the existing is sent remotely or not.
     * @param target The object to turn active
     * @param genericParameters genericParameters parameterizing types
     * @param nameOfTargetType the fully qualified name of the type the stub class should
     * inherit from. That type can be less specific than the type of the target object.
     * @param node The Node the object should be sent to or null to create the active
     *       object in the local JVM
     * @param activity the possibly null activity object defining the different step in the activity of the object.
     *               see the definition of the activity in the javadoc of this classe for more information.
     * @param factory the possibly null meta factory giving all factories for creating the meta-objects part of the
     *                body associated to the reified object. If null the default ProActive MataObject factory is used.
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object turnActive(Object target, Class<?>[] genericParameters, String nameOfTargetType,
            Node node, Active activity, MetaObjectFactory factory) throws ActiveObjectCreationException,
            NodeException {
        if (factory == null) {
            factory = ProActiveMetaObjectFactory.newInstance();
        }

        ProActiveSecurityManager factorySM = factory.getProActiveSecurityManager();

        MetaObjectFactory clonedFactory = factory;

        if (factorySM != null) {
            try {
                clonedFactory = (MetaObjectFactory) factory.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            clonedFactory.setProActiveSecurityManager(factory.getProActiveSecurityManager()
                    .generateSiblingCertificate(EntityType.OBJECT, nameOfTargetType));
            ProActiveLogger.getLogger(Loggers.SECURITY).debug("new active object with security manager");
        }

        if (node == null) {
            //using default proactive node
            node = NodeFactory.getDefaultNode();
        }

        try {
            return MOP.createStubObject(target, nameOfTargetType, genericParameters, node, activity,
                    clonedFactory);
        } catch (MOPException e) {
            Throwable t = e;

            if (e.getTargetException() != null) {
                t = e.getTargetException();
            }

            throw new ActiveObjectCreationException(t);
        }
    }

    /**
     * Registers an active object into a registry(RMI or IBIS or HTTP, default is RMI).
     * In fact it is the remote version of the body of the active object that is registered
     * into the registry under the given URL. According to the type of the associated body(default is Rmi),
     * the registry in which to register is automatically found.
     * @param obj the active object to register.
     * @param url the url under which the remote body is registered. The url must point to the localhost
     * since registering is always a local action. The url can take the form:protocol://localhost:port/nam
     * or //localhost:port/name if protocol is RMI or //localhost/name if port is 1099 or only the name.
     * The registered object will be reachable with the following url: protocol://machine_name:port/name
     * using lookupActive method. Protocol and port can be removed if default
     * @exception java.io.IOException if the remote body cannot be registered
     */
    public static void register(Object obj, String url) throws java.io.IOException {
        UniversalBody body = AbstractBody.getRemoteBody(obj);

        try {
            body.register(url);
            body.setRegistered(true);
            if (PAActiveObject.logger.isInfoEnabled()) {
                PAActiveObject.logger.info("Success at binding url " + url);
            }
        } catch (UnknownProtocolException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Looks-up all Active Objects registered on a host, using a registry(RMI or HTTP or IBIS)
     * The registry where to look for is fully determined with the protocol included in the url.
     * @param url The url where to perform the lookup. The url takes the following form:
     * protocol://machine_name:port. Protocol and port can be ommited if respectively RMI and 1099:
     * //machine_name
     * @return String [] the list of names registered on the host; if no Registry found, returns {}
     * @throws IOException If the given url does not map to a physical host, or if the connection is refused.
     */
    public static String[] listActive(String url) throws java.io.IOException {
        String[] activeNames = null;
        try {
            URI[] uris = RemoteObjectHelper.list(URI.create(url));
            activeNames = new String[uris.length];
            for (int i = 0; i < uris.length; i++) {
                activeNames[i] = uris[i].toString();
            }
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return activeNames;
    }

    /**
     * Kill an Active Object by calling terminate() method on its body.
     * @param ao the active object to kill
     * @param immediate if this boolean is true, this method is served as an immediate service.
     * The termination is then synchronous.
     * The active object dies immediatly. Else, the kill request is served as a normal request, it
     * is put on the request queue. The termination is asynchronous.
     */
    public static void terminateActiveObject(Object ao, boolean immediate) {
        if (MOP.isReifiedObject(ao)) {
            //if ao is a future we need to obtain the real stub
            ao = PAFuture.getFutureValue(ao);

            Proxy proxy = ((StubObject) ao).getProxy();
            try {
                if (immediate) {
                    NonFunctionalServices.terminateAOImmediately(proxy);
                } else {
                    NonFunctionalServices.terminateAO(proxy);
                }
            } catch (BodyTerminatedException e) {
                // the terminated body is already terminated
                if (PAActiveObject.logger.isDebugEnabled()) {
                    PAActiveObject.logger.debug("Terminating already terminated body : " + e);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            throw new ProActiveRuntimeException("The given object " + ao + " is not a reified object");
        }
    }

    /**
     * Kill the calling active object by calling terminate() method on its body.
     * @param immediate if this boolean is true, this method is served as an immediate service.
     * The termination is then synchronous.
     * The active object dies immediatly. Else, the kill request is served as a normal request, it
     * is put on the request queue. The termination is asynchronous.
     */
    public static void terminateActiveObject(boolean immediate) {
        terminateActiveObject(PAActiveObject.getStubOnThis(), immediate);
    }

    /**
     * Ping the target active object. Note that this method does not take into account the
     * state of the target object : pinging an inactive but reachable active object actually
     * returns true.
     * @param target the pinged active object.
     * @return true if the active object is reachable, false otherwise.
     */
    public static boolean pingActiveObject(Object target) {
        //if target is a future we need to obtain the real stub
        target = PAFuture.getFutureValue(target);

        UniversalBody targetedBody = null;
        try {
            // reified object is checked in getRemoteBody
            targetedBody = AbstractBody.getRemoteBody(target);
            targetedBody.receiveFTMessage(PAActiveObject.hb);
            return true;
        } catch (IOException e) {
            if (PAActiveObject.logger.isDebugEnabled()) {
                // id should be cached locally
                PAActiveObject.logger.debug("Active object " + targetedBody.getID() + " is unreachable.", e);
            }
            return false;
        }
    }

    /**
     * Set an immediate execution for the caller active object of the method methodName,
     * ie request of name methodName will be executed right away upon arrival at the caller
     * AO context.
     * Warning: the execution of an Immediate Service method is achieved in parallel of  the
     * current services, so it is the programmer responsibility to ensure that Immediate Services
     * do not interfere with any other methods.
     * @param methodName the name of the method
     */
    public static void setImmediateService(String methodName) {
        PAActiveObject.getBodyOnThis().setImmediateService(methodName);
    }

    /**
     * Set an immediate execution for the caller active object obj of the method methodName with
     * parameters parametersType, ie request of name methodName will be executed right away upon
     * arrival at the caller AO context.
     * Warning: the execution of an Immediate Service method is achieved in parallel of  the
     * current services, so it is the programmer responsibility to ensure that Immediate Services
     * do not interfere with any other methods.
     * @param methodName the name of the method
     * @param parametersTypes the types of the parameters of the method
     */
    public static void setImmediateService(String methodName, Class<?>[] parametersTypes) {
        PAActiveObject.getBodyOnThis().setImmediateService(methodName, parametersTypes);
    }

    /**
     * Removes an immmediate execution for the active object obj, i.e. requests corresponding to the name
     * will be executed by the calling thread, and not added in the request queue.
     * @param methodName the name of the method
     */
    public static void removeImmediateService(String methodName) {
        PAActiveObject.getBodyOnThis().removeImmediateService(methodName);
    }

    /**
     * Removes an immmediate execution for the active object obj, i.e. requests corresponding to the name and types of parameters
     * will be executed by the calling thread, and not added in the request queue.
     * @param methodName the name of the method
     * @param parametersTypes the types of the parameters of the method
     */
    public static void removeImmediateService(String methodName, Class<?>[] parametersTypes) {
        PAActiveObject.getBodyOnThis().removeImmediateService(methodName, parametersTypes);
    }

    /**
     * Return the URL of the remote <code>activeObject</code>.
     * @param activeObject the remote active object.
     * @return the URL of <code>activeObject</code>.
     */
    public static String getActiveObjectNodeUrl(Object activeObject) {
        UniversalBody body = AbstractBody.getRemoteBody(activeObject);
        return body.getNodeURL();
    }

    public static Node getActiveObjectNode(Object activeObject) throws NodeException {
        Node node = NodeFactory.getNode(getActiveObjectNodeUrl(activeObject));
        return node;
    }

    /**
     * Unregisters an active object previously registered into a registry.
     * @param url the url under which the active object is registered.
     * @exception java.io.IOException if the remote object cannot be removed from the registry
     */
    public static void unregister(String url) throws java.io.IOException {
        String protocol = URIBuilder.getProtocol(url);

        RemoteObject rmo;
        try {
            rmo = RemoteObjectHelper.lookup(URI.create(url));
            Object o = RemoteObjectHelper.generatedObjectStub(rmo);

            if (o instanceof UniversalBody) {
                UniversalBody ub = (UniversalBody) o;
                ub.setRegistered(false);
            }

            if (PAActiveObject.logger.isDebugEnabled()) {
                PAActiveObject.logger.debug("Success at unbinding url " + url);
            }
        } catch (ProActiveException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Return the current execution context for the calling thread. The execution context
     * contains a reference to the body associated to this thread, and some informations about
     * the currently served request if any.
     * @return the current execution context associated to the calling thread.
     * @see org.objectweb.proactive.core.body.Context
     */
    public static Context getContext() {
        return LocalBodyStore.getInstance().getContext();
    }

    /**
     * Returns a Stub-Proxy couple pointing to the local body associated to the active
     * object whose active thread is calling this method.
     * @return a Stub-Proxy couple pointing to the local body.
     * @see PAActiveObject#getBodyOnThis
     */
    public static StubObject getStubOnThis() {
        Body body = PAActiveObject.getBodyOnThis();

        if (PAActiveObject.logger.isDebugEnabled()) {
            //logger.debug("ProActive: getStubOnThis() returns " + body);
        }
        if (body == null) {
            return null;
        }

        return PAActiveObject.getStubForBody(body);
    }

    /**
     * @return the jobId associated with the object calling this method
     */
    public static String getJobId() {
        return PAActiveObject.getBodyOnThis().getJobID();
    }

    /**
     * @return The node of the current active object.
     * @throws NodeException problem with the node.
     */
    public static Node getNode() throws NodeException {
        BodyProxy destProxy = (BodyProxy) (getStubOnThis()).getProxy();
        return NodeFactory.getNode(destProxy.getBody().getNodeURL());
    }

    /**
     * Looks-up an active object previously registered in a registry(RMI, IBIS, HTTP). In fact it is the
     * remote version of the body of an active object that can be registered into the Registry
     * under a given URL. If the lookup is successful, the method reconstructs a Stub-Proxy couple and
     * point it to the RmiRemoteBody found.
     * The registry where to look for is fully determined with the protocol included in the url
     * @param classname the fully qualified name of the class the stub should inherit from.
     * @param url the url under which the remote body is registered. The url takes the following form:
     * protocol://machine_name:port/name. Protocol and port can be ommited if respectively RMI and 1099:
     * //machine_name/name
     * @return a remote reference on a Stub of type <code>classname</code> pointing to the
     *     remote body found
     * @exception java.io.IOException if the remote body cannot be found under the given url
     *      or if the object found is not of type RmiRemoteBody
     * @exception ActiveObjectCreationException if the stub-proxy couple cannot be created
     */
    public static Object lookupActive(String classname, String url) throws ActiveObjectCreationException,
            java.io.IOException {
        RemoteObject rmo;
        URI uri = RemoteObjectHelper.expandURI(URI.create(url));

        try {
            rmo = RemoteObjectHelper.lookup(uri);

            Object o = RemoteObjectHelper.generatedObjectStub(rmo);

            if (o instanceof UniversalBody) {
                return MOP.createStubObject(classname, (UniversalBody) o);
            }
        } catch (ProActiveException e) {
            throw new IOException(e.getMessage());
        } catch (MOPException e) {
            Throwable t = e;

            if (e.getTargetException() != null) {
                t = e.getTargetException();
            }

            throw new ActiveObjectCreationException("Exception occured when trying to create stub-proxy", t);
        }

        return null;
    }

    /**
     * Enable the automatic continuation mechanism for this active object.
     */
    public static void enableAC(Object obj) throws java.io.IOException {
        UniversalBody body = AbstractBody.getRemoteBody(obj);
        body.enableAC();
    }

    /**
     * Disable the automatic continuation mechanism for this active object.
     */
    public static void disableAC(Object obj) throws java.io.IOException {
        UniversalBody body = AbstractBody.getRemoteBody(obj);
        body.disableAC();
    }

    /**
     * When an active object is created, it is associated with a Body that takes care
     * of all non fonctionnal properties. Assuming that the active object is only
     * accessed by the different Stub objects, all method calls end-up as Requests sent
     * to this Body. Therefore the only thread calling the method of the active object
     * is the active thread managed by the body. There is an unique mapping between the
     * active thread and the body responsible for it. From any method in the active object
     * the current thread caller of the method is the active thread. When a reified method wants
     * to get a reference to the Body associated to the active object, it can invoke this
     * method. Assuming that the current thread is the active thread, the associated body
     * is returned.
     * @return the body associated to the active object whose active thread is calling
     *     this method.
     * @throws ProActiveException
     */
    public static Body getBodyOnThis() {
        return LocalBodyStore.getInstance().getContext().getBody();
    }

    // -------------------------------------------------------------------------------------------
    //
    // STUB CREATION
    //
    // -------------------------------------------------------------------------------------------
    private static StubObject getStubForBody(Body body) {
        try {
            return MOP.createStubObject(body.getReifiedObject(), new Object[] { body }, body
                    .getReifiedObject().getClass().getName(), null);
        } catch (MOPException e) {
            throw new ProActiveRuntimeException("Cannot create Stub for this Body e=" + e);
        }
    }
}
