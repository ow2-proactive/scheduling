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
package org.objectweb.proactive;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.util.Fractal;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.ActiveBody;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.ibis.IbisRemoteBodyAdapter;
import org.objectweb.proactive.core.body.migration.Migratable;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.body.proxy.AbstractProxy;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.body.request.BodyRequest;
import org.objectweb.proactive.core.body.rmi.RemoteBodyAdapter;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.descriptor.xml.ProActiveDescriptorHandler;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.handler.Handler;
import org.objectweb.proactive.core.exceptions.handler.HandlerNonFunctionalException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.ConstructionOfProxyObjectFailedException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.UrlBuilder;

import java.net.UnknownHostException;

import java.util.HashMap;


public class ProActive {
    protected static Logger logger = Logger.getLogger(ProActive.class.getName());
    public static Logger loggerSecurity = Logger.getLogger("SECURITY");
    public static Logger loggerGroup = Logger.getLogger("GROUP");

    //
    // -- STATIC MEMBERS -----------------------------------------------
    //

    /**
     * Default level is used to store default handlers
     */
    static public HashMap defaultLevel = null;

    /**
     * VM level provides handling strategies according to the virtual machine
     */
    static public HashMap VMLevel = null;

    /**
     * Code level is used for temporary handlers
     */
    static public HashMap codeLevel = null;

    static {
        ProActiveConfiguration.load();
        Class c = org.objectweb.proactive.core.runtime.RuntimeFactory.class;

        // Creation of the default level which contains standard exception handlers
        defaultLevel = new HashMap();

        // We add handler to default level
        if (logger.isDebugEnabled()) {
            logger.debug("[NFE_INIT] Initialization of default level handlers");
        }
        setExceptionHandler(HandlerNonFunctionalException.class,
            NonFunctionalException.class, Handler.ID_Default, null);

        //ProActiveConfiguration.load();
    }

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    private ProActive() {
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * Creates a new ActiveObject based on classname attached to a default node in the local JVM.
     * @param classname the name of the class to instanciate as active
     * @param constructorParameters the parameters of the constructor.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the DefaultNode cannot be created
     */
    public static Object newActive(String classname,
        Object[] constructorParameters)
        throws ActiveObjectCreationException, NodeException {
        // avoid ambiguity for method parameters types
        Node nullNode = null;
        return newActive(classname, constructorParameters, nullNode, null, null);
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
    public static Object newActive(String classname,
        Object[] constructorParameters, String nodeURL)
        throws ActiveObjectCreationException, NodeException {
        if (nodeURL == null) {
            // avoid ambiguity for method parameters types
            Node nullNode = null;
            return newActive(classname, constructorParameters, nullNode, null,
                null);
        } else {
            return newActive(classname, constructorParameters,
                NodeFactory.getNode(nodeURL), null, null);
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
    public static Object newActive(String classname,
        Object[] constructorParameters, Node node)
        throws ActiveObjectCreationException, NodeException {
        return newActive(classname, constructorParameters, node, null, null);
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
     * @param constructorParameters the parameters of the constructor of the object
     *    to instantiate as active. If some parameters are primitive types, the wrapper
     *    class types should be given here. null can be used to specify that no parameter
     *    are passed to the constructor.
     * @param node the possibly null node where to create the active object. If null, the active object
     *       is created localy on a default node
     * @param activity the possibly null activity object defining the different step in the activity of the object.
     *               see the definition of the activity in the javadoc of this classe for more information.
     * @param factory the possibly null meta factory giving all factories for creating the meta-objects part of the
     *                body associated to the reified object. If null the default ProActive MataObject factory is used.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object newActive(String classname,
        Object[] constructorParameters, Node node, Active activity,
        MetaObjectFactory factory)
        throws ActiveObjectCreationException, NodeException {
        //using default proactive node
        if (node == null) {
            node = NodeFactory.getDefaultNode();
        }

        if (factory == null) {
            factory = ProActiveMetaObjectFactory.newInstance();
        }

        try {
            return createStubObject(classname, constructorParameters, node,
                activity, factory);
        } catch (MOPException e) {
            Throwable t = e;

            if (e.getTargetException() != null) {
                t = e.getTargetException();
            }

            throw new ActiveObjectCreationException(t);
        }
    }

    /**
            * Creates a new set of active objects based on classname attached to the given virtualnode.
            * @param classname classname the name of the class to instanciate as active
            * @param constructorParameters constructorParameters the parameters of the constructor.
            * @param virtualnode The virtualnode where to create active objects. Active objects will be created
            * on each node mapped to the given virtualnode in XML deployment descriptor.
            * @return Object a Group of references (possibly remote) on  Stub of newly created active objects
            * @throws ActiveObjectCreationException if a problem occur while creating the stub or the body
            * @throws NodeException if the virtualnode was null
            */
    public static Object newActive(String classname,
        Object[] constructorParameters, VirtualNode virtualnode)
        throws ActiveObjectCreationException, NodeException {
        return ProActive.newActive(classname, constructorParameters,
            virtualnode, null, null);
    }

    /**
     * Creates a new ActiveObject based on classname attached to the given virtualnode.
     * @param classname classname the name of the class to instanciate as active
     * @param constructorParameters constructorParameters the parameters of the constructor.
     * @param virtualnode The virtualnode where to create active objects. Active objects will be created
     * on each node mapped to the given virtualnode in XML deployment descriptor.
     * @param activity the possibly null activity object defining the different step in the activity of the object.
     *               see the definition of the activity in the javadoc of this classe for more information.
     * @param factory the possibly null meta factory giving all factories for creating the meta-objects part of the
     *                body associated to the reified object. If null the default ProActive MataObject factory is used.
     * @return Object a Group of references (possibly remote) on  Stub of newly created active objects
     * @throws ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @throws NodeException if the virtualnode was null
     *
     */
    public static Object newActive(String classname,
        Object[] constructorParameters, VirtualNode virtualnode,
        Active activity, MetaObjectFactory factory)
        throws ActiveObjectCreationException, NodeException {
        if (virtualnode != null) {
            if (!virtualnode.isActivated()) {
                virtualnode.activate();
            }
            Node[] nodeTab = virtualnode.getNodes();
            Group aoGroup = null;
            try {
                aoGroup = ProActiveGroup.getGroup(ProActiveGroup.newGroup(
                            classname));
            } catch (ClassNotFoundException e) {
                throw new ActiveObjectCreationException(
                    "Cannot create group of active objects" + e);
            } catch (ClassNotReifiableException e) {
                throw new ActiveObjectCreationException(
                    "Cannot create group of active objects" + e);
            }
            for (int i = 0; i < nodeTab.length; i++) {
                Object tmp = newActive(classname, constructorParameters,
                        (Node) nodeTab[i], activity, factory);
                aoGroup.add(tmp);
            }

            return aoGroup.getGroupByType();
        } else {
            throw new NodeException(
                "VirtualNode is null, unable to activate the object");
        }
    }

    /**
      * Creates a new ProActive component over the specified base class, according to the
      * given component parameters, and returns a reference on the component of type Component.
      * A reference on the active object base class can be retreived through the component parameters controller's
      * method "getStubOnReifiedObject".
      *
      * @param classname the name of the base class. "Composite" if the component is a composite,
      * "ParallelComposite" if the component is a parallel composite component
      * @param constructorParameters the parameters of the constructor of the object
      *    to instantiate as active. If some parameters are primitive types, the wrapper
      *    class types should be given here. null can be used to specify that no parameter
      *    are passed to the constructor.
      * @param node the possibly null node where to create the active object. If null, the active object
      *       is created localy on a default node
      * @param activity the possibly null activity object defining the different step in the activity of the object.
      *               see the definition of the activity in the javadoc of this classe for more information.
      * @param factory should be null for components (automatically created)
      * @param componentParameters the parameters of the component
      * @return a component representative of type Component
      * @exception ActiveObjectCreationException if a problem occurs while creating the stub or the body
      * @exception NodeException if the node was null and that the DefaultNode cannot be created
      */
    public static Component newActiveComponent(String classname,
        Object[] constructorParameters, Node node, Active activity,
        MetaObjectFactory factory, ComponentParameters componentParameters)
        throws ActiveObjectCreationException, NodeException {
        // COMPONENTS
        try {
            Component boot = Fractal.getBootstrapComponent();
            GenericFactory cf = Fractal.getGenericFactory(boot);
            return cf.newFcInstance(componentParameters.getComponentType(),
                new ControllerDescription(componentParameters.getName(),
                    componentParameters.getHierarchicalType()),
                new ContentDescription(classname, constructorParameters, node,
                    activity, factory));
        } catch (NoSuchInterfaceException e) {
            throw new ActiveObjectCreationException(e);
        } catch (InstantiationException e) {
            if (e.getCause() instanceof NodeException) {
                throw new NodeException(e);
            } else {
                throw new ActiveObjectCreationException(e);
            }
        }
    }

    /**
     * Creates a new ProActive component over the specified base class, according to the
     * given component parameters, and returns a reference on the component of type Component.
     *
     * This method allows automatic of primitive components on Virtual Nodes. In that case, the appendix
     * -cyclicInstanceNumber-<b><i>number</i></b> is added to the name of each of these components.
     * If the component is not a primitive, only one instance of the component is created, on the first node
     * retreived from the specified virtual node.
     *
     * A reference on the active object base class can be retreived through the component parameters controller's
     * method "getStubOnReifiedObject".
     *
     * @param className the name of the base class. "Composite" if the component is a composite,
     * "ParallelComposite" if the component is a parallel composite component
     * @param constructorParameters the parameters of the constructor of the object
     *    to instantiate as active. If some parameters are primitive types, the wrapper
     *    class types should be given here. null can be used to specify that no parameter
     *    are passed to the constructor.
     * @param vn the possibly null node where to create the active object. If null, the active object
     *       is created localy on a default node
     * @param componentParameters the parameters of the component
     * @return a typed group of component representative elements, of type Component
     * @exception ActiveObjectCreationException if a problem occurs while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Component newActiveComponent(String className,
        Object[] constructorParameters, VirtualNode vn,
        ComponentParameters componentParameters)
        throws ActiveObjectCreationException, NodeException {
        // COMPONENTS
        try {
            Component boot = Fractal.getBootstrapComponent();
            GenericFactory cf = Fractal.getGenericFactory(boot);
            return cf.newFcInstance(componentParameters.getComponentType(),
                new ControllerDescription(componentParameters.getName(),
                    componentParameters.getHierarchicalType()),
                new ContentDescription(className, constructorParameters, vn));
        } catch (NoSuchInterfaceException e) {
            throw new ActiveObjectCreationException(e);
        } catch (InstantiationException e) {
            if (e.getCause() instanceof NodeException) {
                throw new NodeException(e);
            } else {
                throw new ActiveObjectCreationException(e);
            }
        }
    }

    /**
     * Turns the target object into an ActiveObject attached to a default node in the local JVM.
     * The type of the stub is is the type of the existing object.
     * @param target The object to turn active
     * @return a reference (possibly remote) on a Stub of the existing object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the DefaultNode cannot be created
     */
    public static Object turnActive(Object target)
        throws ActiveObjectCreationException, NodeException {
        return turnActive(target, (Node) null);
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
    public static Object turnActive(Object target, String nodeURL)
        throws ActiveObjectCreationException, NodeException {
        if (nodeURL == null) {
            return turnActive(target, target.getClass().getName(), null, null,
                null);
        } else {
            return turnActive(target, target.getClass().getName(),
                NodeFactory.getNode(nodeURL), null, null);
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
    public static Object turnActive(Object target, Node node)
        throws ActiveObjectCreationException, NodeException {
        return turnActive(target, target.getClass().getName(), node, null, null);
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
    public static Object turnActive(Object target, Node node, Active activity,
        MetaObjectFactory factory)
        throws ActiveObjectCreationException, NodeException {
        return turnActive(target, target.getClass().getName(), node, activity,
            factory);
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
    public static Object turnActive(Object target, String nameOfTargetType,
        Node node) throws ActiveObjectCreationException, NodeException {
        return turnActive(target, nameOfTargetType, node, null, null);
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
    public static Object turnActive(Object target, String nameOfTargetType,
        Node node, Active activity, MetaObjectFactory factory)
        throws ActiveObjectCreationException, NodeException {
        if (node == null) {
            //using default proactive node
            node = NodeFactory.getDefaultNode();
        }

        if (factory == null) {
            factory = ProActiveMetaObjectFactory.newInstance();
        }

        try {
            return createStubObject(target, nameOfTargetType, node, activity,
                factory);
        } catch (MOPException e) {
            Throwable t = e;

            if (e.getTargetException() != null) {
                t = e.getTargetException();
            }

            throw new ActiveObjectCreationException(t);
        }
    }

    /**
     * Turns a Java object into an Active Object and send it to remote Nodes mapped to the given virtualnode in
     * the XML deployment descriptor.
     * The type of the stub is given by the parameter <code>nameOfTargetType</code>.
     * @param target The object to turn active
     * @param nameOfTargetType the fully qualified name of the type the stub class should
     * inherit from. That type can be less specific than the type of the target object.
     * @param virtualnode The VirtualNode where the target object will be turn into an Active Object
     * Target object will be turned into an Active Object on each node mapped to the given virtualnode in XML deployment descriptor.
     * @return an array of references (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object turnActive(Object target, String nameOfTargetType,
        VirtualNode virtualnode)
        throws ActiveObjectCreationException, NodeException {
        if (virtualnode != null) {
            Node[] nodeTab = virtualnode.getNodes();
            Group aoGroup = null;
            try {
                aoGroup = ProActiveGroup.getGroup(ProActiveGroup.newGroup(
                            target.getClass().getName()));
            } catch (ClassNotFoundException e) {
                throw new ActiveObjectCreationException(
                    "Cannot create group of active objects" + e);
            } catch (ClassNotReifiableException e) {
                throw new ActiveObjectCreationException(
                    "Cannot create group of active objects" + e);
            }

            for (int i = 0; i < nodeTab.length; i++) {
                Object tmp = turnActive(target, nameOfTargetType,
                        (Node) nodeTab[i], null, null);
                aoGroup.add(tmp);
            }

            return aoGroup;
        } else {
            throw new NodeException(
                "VirtualNode is null, unable to active the object");
        }
    }

    /**
     * Registers an active object into a RMI registry. In fact it is the
     * remote version of the body of the active object that is registered into the
     * RMI Registry under the given URL.
     * @param obj the active object to register.
     * @param url the url under which the remote body is registered.
     * @exception java.io.IOException if the remote body cannot be registered
     */
    public static void register(Object obj, String url)
        throws java.io.IOException {
        // Check if obj is really a reified object
        if (!(MOP.isReifiedObject(obj))) {
            throw new java.io.IOException("The given object " + obj +
                " is not a reified object");
        }

        // Find the appropriate remoteBody
        org.objectweb.proactive.core.mop.Proxy myProxy = ((StubObject) obj).getProxy();

        if (myProxy == null) {
            throw new java.io.IOException(
                "Cannot find a Proxy on the stub object: " + obj);
        }

        BodyProxy myBodyProxy = (BodyProxy) myProxy;
        UniversalBody body = myBodyProxy.getBody().getRemoteAdapter();

        if (body instanceof RemoteBodyAdapter) {
            RemoteBodyAdapter.register((RemoteBodyAdapter) body, url);
            if (logger.isInfoEnabled()) {
                logger.info("Success at binding url " + url);
            }
        } else {
            if (body instanceof IbisRemoteBodyAdapter) {
                IbisRemoteBodyAdapter.register((IbisRemoteBodyAdapter) body, url);
                if (logger.isInfoEnabled()) {
                    logger.info("Success at binding url " + url);
                }
            } else {
                throw new java.io.IOException(
                    "Cannot reconize the type of this UniversalBody: " +
                    body.getClass().getName());
            }
        }
    }

    /**
     * Unregisters an active object previously registered into a RMI registry.
     * @param url the url under which the active object is registered.
     * @exception java.io.IOException if the remote object cannot be removed from the registry
     */
    public static void unregister(String url) throws java.io.IOException {
        RemoteBodyAdapter.unregister(url);
        if (logger.isDebugEnabled()) {
            logger.debug("Success at unbinding url " + url);
        }
    }

    /**
     * Looks-up an active object previously registered in a RMI registry. In fact it is the
     * remote version of the body of an active object that can be registered into the
     * RMI Registry under a given URL. If the lookup is successful, the method reconstructs
     * a Stub-Proxy couple and point it to the RemoteBody found.
     * @param classname the fully qualified name of the class the stub should inherit from.
     * @param url the url under which the remote body is registered.
     * @return a remote reference on a Stub of type <code>classname</code> pointing to the
     *     remote body found
     * @exception java.io.IOException if the remote body cannot be found under the given url
     *      or if the object found is not of type RemoteBody
     * @exception ActiveObjectCreationException if the stub-proxy couple cannot be created
     */
    public static Object lookupActive(String classname, String url)
        throws ActiveObjectCreationException, java.io.IOException {
        UniversalBody b = null;
        if ("ibis".equals(System.getProperty("proactive.communication.protocol"))) {
            b = IbisRemoteBodyAdapter.lookup(url);
        } else {
            b = RemoteBodyAdapter.lookup(url);
        }

        try {
            return createStubObject(classname, b);
        } catch (MOPException e) {
            Throwable t = e;

            if (e.getTargetException() != null) {
                t = e.getTargetException();
            }

            throw new ActiveObjectCreationException("Exception occured when trying to create stub-proxy",
                t);
        }
    }

    /**
     * Blocks the calling thread until the object <code>future</code>
     * is available. <code>future</code> must be the result object of an
     * asynchronous call. Usually the the wait by necessity model take care
     * of blocking the caller thread asking for a result not yet available.
     * This method allows to block before the result is first used.
     */
    public static void waitFor(Object future) {
        // If the object is not reified, it cannot be a future
        if ((MOP.isReifiedObject(future)) == false) {
            return;
        } else {
            org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();

            // If it is reified but its proxy is not of type future, we cannot wait
            if (!(theProxy instanceof Future)) {
                return;
            } else {
                ((Future) theProxy).waitFor();
            }
        }
    }

    /**
     * Returns a <code>ProActiveDescriptor</code> that gives an object representation
     * of the XML document located at the given url.
     * @param xmlDescriptorUrl The url of the XML document
     * @return ProActiveDescriptor. The object representation of the XML document
     * @throws ProActiveException if a problem occurs during the creation of the object
     * @see org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor
     * @see org.objectweb.proactive.core.descriptor.data.VirtualNode
     * @see org.objectweb.proactive.core.descriptor.data.VirtualMachine
     */
    public static ProActiveDescriptor getProactiveDescriptor(
        String xmlDescriptorUrl) throws ProActiveException {
        RuntimeFactory.getDefaultRuntime();
        if (!xmlDescriptorUrl.startsWith("file:")) {
            xmlDescriptorUrl = "file:" + xmlDescriptorUrl;
        }
        ProActiveRuntimeImpl part = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();
        if (part.getDescriptor(xmlDescriptorUrl) != null) {
            return part.getDescriptor(xmlDescriptorUrl);
        }

        try {
            if (logger.isInfoEnabled()) {
                logger.info("************* Reading deployment descriptor: " +
                    xmlDescriptorUrl + " ********************");
            }
            ProActiveDescriptorHandler proActiveDescriptorHandler = ProActiveDescriptorHandler.createProActiveDescriptor(xmlDescriptorUrl);
            ProActiveDescriptor pad = (ProActiveDescriptor) proActiveDescriptorHandler.getResultObject();
            part.registerDescriptor(xmlDescriptorUrl, pad);
            return pad;
        } catch (org.xml.sax.SAXException e) {
            e.printStackTrace();
            logger.fatal(
                "a problem occurs when getting the proActiveDescriptor");
            throw new ProActiveException(e);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            logger.fatal(
                "a problem occurs during the ProActiveDescriptor object creation");
            throw new ProActiveException(e);
        }
    }

    /**
     * Registers locally the given VirtualNode in a registry such RMIRegistry or JINI Lookup Service.
     * The VirtualNode to register must exist on the local runtime. This is done when using XML Deployment Descriptors
     * @param virtualNode the VirtualNode to register.
     * @param registrationProtocol The protocol used for registration. At this time RMI and JINI are supported
     * @param replacePreviousBinding
     * @throws ProActiveException If the VirtualNode with the given name does not exist on the local runtime
     */
    public static void registerVirtualNode(VirtualNode virtualNode,
        String registrationProtocol, boolean replacePreviousBinding)
        throws ProActiveException {
        if (!(virtualNode instanceof VirtualNodeImpl)) {
            throw new ProActiveException(
                "Cannot register such virtualNode since it results from a lookup!");
        }
        String virtualnodeName = virtualNode.getName();
        ProActiveRuntime part = RuntimeFactory.getProtocolSpecificRuntime(registrationProtocol);
        VirtualNode vn = part.getVirtualNode(virtualnodeName);
        if (vn == null) {
            throw new ProActiveException("VirtualNode " + virtualnodeName +
                " does not exist !");
        }
        part.registerVirtualNode(UrlBuilder.appendVnSuffix(virtualnodeName),
            replacePreviousBinding);
    }

    /**
     * Looks-up a VirtualNode previously registered in a registry(RMI or JINI)
     * @param url The url where to perform the lookup
     * @param protocol The protocol used to perform the lookup(RMI and JINI are supported)
     * @return VirtualNode The virtualNode returned by the lookup
     * @throws ProActiveException If no objects are bound with the given url
     */
    public static VirtualNode lookupVirtualNode(String url, String protocol)
        throws ProActiveException {
        ProActiveRuntime remoteProActiveRuntime = null;
        try {
            remoteProActiveRuntime = RuntimeFactory.getRuntime(UrlBuilder.buildVirtualNodeUrl(
                        url), protocol);
        } catch (UnknownHostException ex) {
            throw new ProActiveException(ex);
        }
        return remoteProActiveRuntime.getVirtualNode(UrlBuilder.getNameFromUrl(
                url));
    }

    /**
     * Unregisters the virtualNode previoulsy registered in a registry such as JINI or RMI.
     * Calling this method removes the VirtualNode from the local runtime.
     * @param virtualNode The VirtualNode to unregister
     * @throws ProActiveException if a problem occurs whle unregistering the VirtualNode
     */
    public static void unregisterVirtualNode(VirtualNode virtualNode)
        throws ProActiveException {
        //VirtualNode vn = ((VirtualNodeStrategy)virtualNode).getVirtualNode();
        if (!(virtualNode instanceof VirtualNodeImpl)) {
            throw new ProActiveException(
                "Cannot unregister such virtualNode since it results from a lookup!");
        }
        String virtualNodeName = virtualNode.getName();
        ProActiveRuntime part = RuntimeFactory.getProtocolSpecificRuntime(((VirtualNodeImpl) virtualNode).getRegistrationProtocol());
        part.unregisterVirtualNode(UrlBuilder.appendVnSuffix(
                virtualNode.getName()));
        if (logger.isInfoEnabled()) {
            logger.info("Success at unbinding " + virtualNodeName);
        }
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
     */
    public static Body getBodyOnThis() {
        return LocalBodyStore.getInstance().getCurrentThreadBody();
    }

    /**
     * Returns a Stub-Proxy couple pointing to the local body associated to the active
     * object whose active thread is calling this method.
     * @return a Stub-Proxy couple pointing to the local body.
     * @see #getBodyOnThis
     */
    public static StubObject getStubOnThis() {
        Body body = getBodyOnThis();

        if (logger.isDebugEnabled()) {
            //logger.debug("ProActive: getStubOnThis() returns " + body);
        }
        if (body == null) {
            return null;
        }

        return getStubForBody(body);
    }

    /**
     * Migrates the active object whose active thread is calling this method to the
     * same location as the active object given in parameter.
     * This method must be called from an active object using the active thread as the
     * current thread will be used to find which active object is calling the method.
     * The object given as destination must be an active object.
     * @param activeObject the active object indicating the destination of the migration.
     * @exception MigrationException if the migration fails
     * @see #getBodyOnThis
     */
    public static void migrateTo(Object activeObject) throws MigrationException {
        migrateTo(getNodeFromURL(getNodeURLFromActiveObject(activeObject)));
    }

    /**
     * Migrates the active object whose active thread is calling this method to the
     * node caracterized by the given url.
     * This method must be called from an active object using the active thread as the
     * current thread will be used to find which active object is calling the method.
     * The url must be the url of an existing node.
     * @param nodeURL the url of an existing where to migrate to.
     * @exception MigrationException if the migration fails
     * @see #getBodyOnThis
     */
    public static void migrateTo(String nodeURL) throws MigrationException {
        if (logger.isDebugEnabled()) {
            logger.debug("migrateTo " + nodeURL);
        }
        ProActive.migrateTo(getNodeFromURL(nodeURL));
    }

    /**
     * Migrates the active object whose active thread is calling this method to the
     * given node.
     * This method must be called from an active object using the active thread as the
     * current thread will be used to find which active object is calling the method.
     * @param node an existing node where to migrate to.
     * @exception MigrationException if the migration fails
     * @see #getBodyOnThis
     */
    public static void migrateTo(Node node) throws MigrationException {
        if (logger.isDebugEnabled()) {
            logger.debug("migrateTo " + node);
        }
        Body bodyToMigrate = getBodyOnThis();
        if (!(bodyToMigrate instanceof Migratable)) {
            throw new MigrationException(
                "This body cannot migrate. It doesn't implement Migratable interface");
        }

        ((Migratable) bodyToMigrate).migrateTo(node);
    }

    /**
     * Migrates the given body to the same location as the active object given in parameter.
     * This method can be called from any object and does not perform the migration.
     * Instead it generates a migration request that is sent to the targeted body.
     * Two strategies are possible :
     *   - the request is high priority and is processed before all existing requests
     *   the body may have received (priority = true)
     *   - the request is normal priority and is processed after all existing requests
     *   the body may have received (priority = false)
     * The object given as destination must be an active object.
     * @param bodyToMigrate the body to migrate.
     * @param activeObject the active object indicating the destination of the migration.
     * @param priority a boolean indicating the priority of the migration request sent to the body.
     * @exception MigrationException if the migration fails
     */
    public static void migrateTo(Body bodyToMigrate, Object activeObject,
        boolean priority) throws MigrationException {
        migrateTo(bodyToMigrate,
            getNodeFromURL(getNodeURLFromActiveObject(activeObject)), priority);
    }

    /**
     * Migrates the given body to the node caracterized by the given url.
     * This method can be called from any object and does not perform the migration.
     * Instead it generates a migration request that is sent to the targeted body.
     * Two strategies are possible :
     *   - the request is high priority and is processed before all existing requests
     *   the body may have received (priority = true)
     *   - the request is normal priority and is processed after all existing requests
     *   the body may have received (priority = false)
     * The object given as destination must be an active object.
     * @param bodyToMigrate the body to migrate.
     * @param nodeURL the url of an existing where to migrate to.
     * @param priority a boolean indicating the priority of the migration request sent to the body.
     * @exception MigrationException if the migration fails
     */
    public static void migrateTo(Body bodyToMigrate, String nodeURL,
        boolean priority) throws MigrationException {
        ProActive.migrateTo(bodyToMigrate, getNodeFromURL(nodeURL), priority);
    }

    /**
     * Migrates the body <code>bodyToMigrate</code> to the given node.
     * This method can be called from any object and does not perform the migration.
     * Instead it generates a migration request that is sent to the targeted body.
     * Two strategies are possible :
     *   - the request is high priority and is processed before all existing requests
     *   the body may have received (priority = true)
     *   - the request is normal priority and is processed after all existing requests
     *   the body may have received (priority = false)
     * The object given as destination must be an active object.
     * @param bodyToMigrate the body to migrate.
     * @param node an existing node where to migrate to.
     * @param priority a boolean indicating the priority of the migration request sent to the body.
     * @exception MigrationException if the migration fails
     */
    public static void migrateTo(Body bodyToMigrate, Node node, boolean priority)
        throws MigrationException {
        if (!(bodyToMigrate instanceof Migratable)) {
            throw new MigrationException(
                "This body cannot migrate. It doesn't implement Migratable interface");
        }

        Object[] arguments = { node };

        try {
            BodyRequest request = new BodyRequest(bodyToMigrate, "migrateTo",
                    new Class[] { Node.class }, arguments, priority);
            request.send(bodyToMigrate);
        } catch (NoSuchMethodException e) {
            throw new MigrationException("Cannot find method migrateTo this body. Non sense since the body is instance of Migratable",
                e);
        } catch (java.io.IOException e) {
            throw new MigrationException("Cannot send the request to migrate", e);
        }
    }

    /**
     * Blocks the calling thread until one of the futures in the vector is available.
     * THIS METHOD MUST BE CALLED FROM AN ACTIVE OBJECT.
     * @param futures vector of futures
     * @return index of the available future in the vector
     */
    public static int waitForAny(java.util.Vector futures) {
        FuturePool fp = getBodyOnThis().getFuturePool();

        synchronized (fp) {
            while (true) {
                java.util.Iterator it = futures.iterator();
                int index = 0;

                while (it.hasNext()) {
                    Object current = it.next();

                    if (!(isAwaited(current))) {
                        return index;
                    }

                    index++;
                }
                fp.waitForReply();
            }
        }
    }

    /**
     * Blocks the calling thread until all futures in the vector are available.
     * THIS METHOD MUST BE CALLED FROM AN ACTIVE OBJECT.
     * @param futures vector of futures
     */
    public static void waitForAll(java.util.Vector futures) {
        FuturePool fp = getBodyOnThis().getFuturePool();

        synchronized (fp) {
            boolean oneIsMissing = true;

            while (oneIsMissing) {
                oneIsMissing = false;

                java.util.Iterator it = futures.iterator();

                while (it.hasNext()) {
                    Object current = it.next();

                    if (isAwaited(current)) {
                        oneIsMissing = true;
                    }
                }

                if (oneIsMissing) {
                    fp.waitForReply();
                }
            }
        }
    }

    /**
     * Blocks the calling thread until the N-th of the futures in the vector is available.
     * THIS METHOD MUST BE CALLED FROM AN ACTIVE OBJECT.
     * @param futures vector of futures
     */
    public static void waitForTheNth(java.util.Vector futures, int n) {
        FuturePool fp = getBodyOnThis().getFuturePool();

        synchronized (fp) {
            Object current = futures.get(n);

            if (isAwaited(current)) {
                waitFor(current);
            }
        }
    }

    /**
     * Return <code>false</code> if one object of <code>futures</code> is
     * available.
     * @param futures a table with futures.
     * @return <code>true</code> if all futures are awaited, else <code>false
     * </code>.
     */
    public static boolean allAwaited(java.util.Vector futures) {
        FuturePool fp = getBodyOnThis().getFuturePool();

        synchronized (fp) {
            java.util.Iterator it = futures.iterator();

            while (it.hasNext()) {
                Object current = it.next();

                if (!isAwaited(current)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Return false if the object <code>future</code> is available.
     * This method is recursive, i.e. if result of future is a future too,
     * <CODE>isAwaited</CODE> is called again on this result, and so on.
     */
    public static boolean isAwaited(Object future) {
        // If the object is not reified, it cannot be a future
        if ((MOP.isReifiedObject(future)) == false) {
            return false;
        } else {
            org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();

            // If it is reified but its proxy is not of type future, we cannot wait
            if (!(theProxy instanceof Future)) {
                return false;
            } else {
                if (((Future) theProxy).isAwaited()) {
                    return true;
                } else {
                    return isAwaited(((Future) theProxy).getResult());
                }
            }
        }
    }

    /**
     * Return the object contains by the future (ie its target).
     * If parameter is not a future, it is returned.
     * A wait-by-necessity occurs if future is not available.
     * This method is recursive, i.e. if result of future is a future too,
     * <CODE>getFutureValue</CODE> is called again on this result, and so on.
     */
    public static Object getFutureValue(Object future) {
        // If the object is not reified, it cannot be a future
        if ((MOP.isReifiedObject(future)) == false) {
            return future;
        } else {
            org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();

            // If it is reified but its proxy is not of type future, we cannot wait
            if (!(theProxy instanceof Future)) {
                return future;
            } else {
                Object o = ((Future) theProxy).getResult();

                return getFutureValue(o);
            }
        }
    }

    /**
     * Enable the automatic continuation mechanism for this active object.
     */
    public static void enableAC(Object obj) throws java.io.IOException {
        // Check if obj is really a reified object
        if (!(MOP.isReifiedObject(obj))) {
            throw new ProActiveRuntimeException("The given object " + obj +
                " is not a reified object");
        }

        // Find the appropriate remoteBody
        org.objectweb.proactive.core.mop.Proxy myProxy = ((StubObject) obj).getProxy();

        if (myProxy == null) {
            throw new ProActiveRuntimeException(
                "Cannot find a Proxy on the stub object: " + obj);
        }

        BodyProxy myBodyProxy = (BodyProxy) myProxy;
        UniversalBody body = myBodyProxy.getBody().getRemoteAdapter();
        body.enableAC();
    }

    /**
     * Disable the automatic continuation mechanism for this active object.
     */
    public static void disableAC(Object obj) throws java.io.IOException {
        // Check if obj is really a reified object
        if (!(MOP.isReifiedObject(obj))) {
            throw new ProActiveRuntimeException("The given object " + obj +
                " is not a reified object");
        }

        // Find the appropriate remoteBody
        org.objectweb.proactive.core.mop.Proxy myProxy = ((StubObject) obj).getProxy();

        if (myProxy == null) {
            throw new ProActiveRuntimeException(
                "Cannot find a Proxy on the stub object: " + obj);
        }

        BodyProxy myBodyProxy = (BodyProxy) myProxy;
        UniversalBody body = myBodyProxy.getBody().getRemoteAdapter();
        body.disableAC();
    }

    /**
     * Set an immmediate execution for the active object obj, ie request of name methodName
     * will be executed by the calling thread, and not add in the request queue.
     * BE CAREFULL : for the first release of this method, do not make use of getCurrentThreadBody nor
     * getStubOnThis in the method defined by methodName !!
     */
    public static void setImmediateService(Object obj, String methodName)
        throws java.io.IOException {
        // Check if obj is really a reified object
        if (!(MOP.isReifiedObject(obj))) {
            throw new ProActiveRuntimeException("The given object " + obj +
                " is not a reified object");
        }

        // Find the appropriate remoteBody
        org.objectweb.proactive.core.mop.Proxy myProxy = ((StubObject) obj).getProxy();

        if (myProxy == null) {
            throw new ProActiveRuntimeException(
                "Cannot find a Proxy on the stub object: " + obj);
        }

        BodyProxy myBodyProxy = (BodyProxy) myProxy;
        UniversalBody body = myBodyProxy.getBody().getRemoteAdapter();
        body.setImmediateService(methodName);
    }

    /**
     * @return the jobId associated with the object calling this method
     */
    public static String getJobId() {
        return ProActive.getBodyOnThis().getJobID();
    }

    /**
       * Search an appropriate handler for a given non functional exception.
       * The search starts in the highest level and continue in lower levels. When no
       * handler is available in one level, the search steps down into the hierarchy.
       * @param ex Exception for which we search a handler.
       * @param target An object which contains its own level.
       * @return A reliable handler or null if no handler is available
       */
    public static Handler searchExceptionHandler(NonFunctionalException ex,
        Object target) {
        // Temporary handler
        Handler handler = null;

        // Logging info about research
        if (logger.isDebugEnabled()) {
            logger.debug("[NFE_INFO] Retrieving handler for " +
                ex.getDescription());
        }

        // Try to get a handler from object level (active object = body or proxy)
        if (target != null) {
            // Try to get an handler from code level
            if (codeLevel != null) {
                if ((handler = searchExceptionHandler(ex.getClass(),
                                (HashMap) codeLevel.get(
                                    new Integer(target.hashCode())),
                                Handler.ID_Code)) != null) {
                    return handler;
                }
            }

            // target is local body (i.e. active object level) ?			
            if (target instanceof ActiveBody) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "[NFE_INFO] Retrieving handler in local body level");
                }
                try {
                    UniversalBody body = ((BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) target).getProxy()).getBody();
                    HashMap map = ((UniversalBody) body).getHandlersLevel();
                    if ((handler = searchExceptionHandler(ex.getClass(), map,
                                    Handler.ID_Body)) != null) {
                        return handler;
                    }
                } catch (ProActiveException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[NFE_ERROR] " + e.getMessage());
                    }
                }

                // target is remote body (i.e. active object level) ?
            } else if (target instanceof RemoteBodyAdapter) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "[NFE_INFO] Retrieving handler in remote body level");
                }
                try {
                    UniversalBody body = ((BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) target).getProxy()).getBody();
                    HashMap map = ((UniversalBody) body).getRemoteAdapter()
                                   .getHandlersLevel();
                    if ((handler = searchExceptionHandler(ex.getClass(), map,
                                    Handler.ID_Body)) != null) {
                        return handler;
                    }
                } catch (ProActiveException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[NFE_ERROR] " + e.getMessage());
                    }
                }

                // target is a proxy (i.e. a ref. to a body) ?
            } else if (target instanceof AbstractProxy) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[NFE_INFO] Retrieving handler in proxy level");
                }
                try {
                    HashMap map = ((AbstractProxy) target).getHandlersLevel();
                    if ((handler = searchExceptionHandler(ex.getClass(), map,
                                    Handler.ID_Proxy)) != null) {
                        return handler;
                    }
                } catch (ProActiveException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[NFE_ERROR] " + e.getMessage());
                    }
                }
            }
        }

        // Try to get an handler from VM level
        if (logger.isDebugEnabled()) {
            logger.debug("[NFE_INFO] Retrieving handler in VM level");
        }
        if ((handler = searchExceptionHandler(ex.getClass(), VMLevel,
                        Handler.ID_VM)) != null) {
            return handler;
        }

        // At the end, get an handler from default level or return null
        if (logger.isDebugEnabled()) {
            logger.debug("[NFE_INFO] Retrieving handler in Default level");
        }
        return searchExceptionHandler(ex.getClass(), defaultLevel,
            Handler.ID_Default);
    }

    /**
     * Search an appropriate handler for a given non functional exception.
     * We first search in the highest level a handler for the real class of the exception. If the search fails, we try
     * with mother classes. When no handler is available in this level, we go down into the hierarchy of levels.
     * @param NFEClass Class of the non-functional exception for which a handler is searched.
     * @param level The level where handlers are searched
     * @param levelID Identificator of the level
     * @return A reliable handler or null if no handler is available
     */
    public static Handler searchExceptionHandler(Class NFEClass, HashMap level,
        int levelID) {
        // Test level capacity
        if ((level == null) || level.isEmpty()) {
            return null;
        }

        // Retrieving the handler in the level
        Handler handler = null;
        while ((handler == null) &&
                (NFEClass.getName().compareTo(ProActiveException.class.getName()) != 0)) {
            // Information
            if (logger.isDebugEnabled()) {
                logger.debug("[NFE_INFO] Retrieving handler " +
                    NFEClass.getName() + " in level " + levelID);
            }

            // Research algorithm
            if (level.containsKey(NFEClass)) {
                handler = (Handler) level.get(NFEClass);
            } else {
                NFEClass = NFEClass.getSuperclass();
            }
        }

        // We return the handler
        return handler;
    }

    /**
     * Add one handler of Non Functional Exception (nfe) to a specific level.
     * Similar handlers are overwritten except those at default level.
     * @param handler A class of handler associated with a class of non functional exception.
     * @param exception A class of non functional exception. It is a subclass of <code>NonFunctionalException</code>.
     * @param levelID An identificator for the level where the handler is added.
     * @param target An object which contains its own level. It is null if  <code>levelID</code> is default or VM level.
     */
    public static void setExceptionHandler(Class handler, Class exception,
        int levelID, Object target) {
        // Logging info
        if (logger.isDebugEnabled()) {
            logger.debug("[NFE_INFO] Setting " + handler.getName() + " for " +
                exception.getName() + " in level " + levelID);
        }

        // Handler instantiation
        Handler h = null;
        try {
            h = (Handler) handler.newInstance();
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "[NFE_SET_ERROR] Problem during instantiation of class " +
                    handler.getName());
            }
        }

        // To minimize overall cost, level are created during the association of the first handler
        switch (levelID) {
        case (Handler.ID_Default):
            if (defaultLevel.get(exception) == null) {
                defaultLevel.put(exception, h);
            }
            break;
        case (Handler.ID_VM):
            if (VMLevel == null) {
                VMLevel = new HashMap();
            }
            VMLevel.put(exception, h);
            break;
        case (Handler.ID_Body):
            // The target object must be a body
            if (target != null) {
                //	Get the body of the target object
                UniversalBody body = ((BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) target).getProxy()).getBody();
                try {
                    if (body instanceof ActiveBody) {
                        // Local body
                        if (logger.isDebugEnabled()) {
                            logger.debug("[NFE_INFO] Setting handler " +
                                handler.getName() +
                                " in local body of object " +
                                target.getClass().getName());
                        }
                        body.setExceptionHandler(exception, handler);
                    } else if (body instanceof RemoteBodyAdapter) {
                        // Remote body
                        if (logger.isDebugEnabled()) {
                            logger.debug("[NFE_INFO] Setting handler " +
                                handler.getName() +
                                " in remote body of object " +
                                target.getClass().getName());
                        }
                        body.getRemoteAdapter().setExceptionHandler(exception,
                            handler);
                    }
                } catch (ProActiveException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[NFE_ERROR] Setting handler " +
                            handler.getName() + " in body level failed");
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "[NFE_ERROR] Handler has no body object to be attached to");
                }
            }
            break;
        case (Handler.ID_Proxy):
            // The target object must be a proxy
            if (((target != null) && target instanceof AbstractProxy)) {
                try {
                    ((AbstractProxy) target).setExceptionHandler(exception,
                        handler);
                } catch (ProActiveException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[NFE_ERROR] Setting handler " +
                            handler.getName() + " in proxy level failed");
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "[NFE_ERROR] Handler has no proxy object to be attached to");
                }
            }
            break;
        case (Handler.ID_Future):
            break;
        case (Handler.ID_Code):
            if (target != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[NFE_INFO] Setting handler " +
                        handler.getName() +
                        " in code level specific to object " +
                        target.getClass().getName());
                }
                if (codeLevel == null) {
                    codeLevel = new HashMap();
                }

                // Try to get the hashmap associated to this object
                if (codeLevel.containsKey(new Integer(target.hashCode()))) {
                    ((HashMap) codeLevel.get(new Integer(target.hashCode()))).put(exception,
                        handler);
                } else {
                    codeLevel.put(new Integer(target.hashCode()), new HashMap());
                    ((HashMap) codeLevel.get(new Integer(target.hashCode()))).put(exception,
                        h);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "[NFE_ERROR] Cannot set handler to NULL code level");
                }
            }
            break;
        }
    }

    /**
     * Remove a handler associated to a class of non functional exceptions.
     * @param exception A class of non functional exception which does not require the given handler anymore.
     * @param levelID An identificator for the level where the handler is removed.
     * @param target An object which contains its own level. It is null if  <code>levelID</code> is default or VM level.
     */
    public static Handler unsetExceptionHandler(Class exception, int levelID,
        Object target) {
        	
        // Logging info
        if (logger.isDebugEnabled()) {
            logger.debug("[NFE_INFO] Removing handler for " +
                exception.getName() + " in level " + levelID);
        }

        // We keep a trace of the removed handler
        Handler handler= null;

        // The correct level is identified
        switch (levelID) {
        // Default level must not be modified !
        case (Handler.ID_Default):
            if (logger.isDebugEnabled()) {
                logger.debug("[NFE_WARNING] Removing handler from default level is forbidden");
            }
            return null;
        case (Handler.ID_VM):
            if (VMLevel != null) {
                handler = (Handler) VMLevel.remove(exception);
            }
            break;
        case (Handler.ID_Body):
            // The target must be a body
            if (((target != null) && target instanceof UniversalBody)) {
                //	Get the body of target (remote) object
                UniversalBody body = ((BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) target).getProxy()).getBody();
                try {
                    if (body instanceof ActiveBody) {
                        handler = body.unsetExceptionHandler(exception);
                    } else if (body instanceof RemoteBodyAdapter) {
						handler = body.getRemoteAdapter().unsetExceptionHandler(exception);
                    }
                } catch (ProActiveException e) {
                    if (logger.isDebugEnabled()) {
						logger.debug("[NFE_ERROR] Removing handler " +
							handler.getClass().getName() + " from body level failed");
                    }
                }
            }
            break;
        case (Handler.ID_Proxy):
            // The target must be a proxy
            if (((target != null) && target instanceof AbstractProxy)) {
                // Create a request to associate handler to the distant body
                try {
                    handler = ((AbstractProxy) target).unsetExceptionHandler(exception);
                    return handler;
                } catch (ProActiveException e) {
                    if (logger.isDebugEnabled()) {
						logger.debug("[NFE_ERROR] Removing handler " +
							handler.getClass().getName() + " from proxy level failed");
                    }
                }
            }
            break;
        case (Handler.ID_Future):
            break;
        case (Handler.ID_Code):
            if ((target != null) && (codeLevel != null)) {
                if (codeLevel.containsKey(new Integer(target.hashCode()))) {
                    handler = (Handler) ((HashMap) codeLevel.get(new Integer(
                                target.hashCode()))).remove(exception);
                }
            }
            break;
        }
        
        return handler;
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private static String getNodeURLFromActiveObject(Object o)
        throws MigrationException {
        //first we check if the parameter is an active object,
        if (!org.objectweb.proactive.core.mop.MOP.isReifiedObject(o)) {
            throw new MigrationException(
                "The parameter is not an active object");
        }

        //now we get a reference on the remoteBody of this guy
        BodyProxy destProxy = (BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) o).getProxy();

        return destProxy.getBody().getNodeURL();
    }

    private static Node getNodeFromURL(String url) throws MigrationException {
        try {
            return NodeFactory.getNode(url);
        } catch (NodeException e) {
            throw new MigrationException("The node of given URL " + url +
                " cannot be localized", e);
        }
    }

    // -------------------------------------------------------------------------------------------
    // 
    // STUB CREATION
    // 
    // -------------------------------------------------------------------------------------------
    private static StubObject getStubForBody(Body body) {
        try {
            return createStubObject(body.getReifiedObject(),
                new Object[] { body },
                body.getReifiedObject().getClass().getName());
        } catch (MOPException e) {
            throw new ProActiveRuntimeException(
                "Cannot create Stub for this Body e=" + e);
        }
    }

    private static Object createStubObject(String className, UniversalBody body)
        throws MOPException {
        return createStubObject(className, null, new Object[] { body });
    }

    private static Object createStubObject(String className,
        Object[] constructorParameters, Node node, Active activity,
        MetaObjectFactory factory) throws MOPException {
        return createStubObject(className, constructorParameters,
            new Object[] { node, activity, factory, ProActive.getJobId() });
    }

    private static Object createStubObject(String className,
        Object[] constructorParameters, Object[] proxyParameters)
        throws MOPException {
        try {
            return MOP.newInstance(className, constructorParameters,
                Constants.DEFAULT_BODY_PROXY_CLASS_NAME, proxyParameters);
        } catch (ClassNotFoundException e) {
            throw new ConstructionOfProxyObjectFailedException(
                "Class can't be found e=" + e);
        }
    }

    private static Object createStubObject(Object target,
        String nameOfTargetType, Node node, Active activity,
        MetaObjectFactory factory) throws MOPException {
        return createStubObject(target,
            new Object[] { node, activity, factory, ProActive.getJobId() },
            nameOfTargetType);
    }

    private static StubObject createStubObject(Object object,
        Object[] proxyParameters, String nameOfTargetType)
        throws MOPException {
        try {
            return (StubObject) MOP.turnReified(nameOfTargetType,
                Constants.DEFAULT_BODY_PROXY_CLASS_NAME, proxyParameters, object);
        } catch (ClassNotFoundException e) {
            throw new ConstructionOfProxyObjectFailedException(
                "Class can't be found e=" + e);
        }
    }
}
