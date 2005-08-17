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
package org.objectweb.proactive.osgi;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.Active;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


/**
 * This interface represents a ProActive Service.
 * Using this service, a bundle can use this service ( only one instance per OSGi platform )
 * When this service is first called , it  creates a ProActive runtime.
 * @author vlegrand
 *
 */
public interface ProActiveService {
    public Object newActive(String classname, Object[] constructorParameters)
        throws ActiveObjectCreationException, NodeException;

    public Object newActive(String classname, Object[] constructorParameters,
        String nodeURL) throws ActiveObjectCreationException, NodeException;

    public Object newActive(String classname, Object[] constructorParameters,
        Node node) throws ActiveObjectCreationException, NodeException;

    public Object newActive(String classname, Object[] constructorParameters,
        Node node, Active activity, MetaObjectFactory factory)
        throws ActiveObjectCreationException, NodeException;

    public Object newActiveAsGroup(String classname,
        Object[] constructorParameters, VirtualNode virtualnode)
        throws ActiveObjectCreationException, NodeException;

    public Object newActiveAsGroup(String classname,
        Object[] constructorParameters, VirtualNode virtualnode,
        Active activity, MetaObjectFactory factory)
        throws ActiveObjectCreationException, NodeException;

    public Component newActiveComponent(String classname,
        Object[] constructorParameters, Node node, Active activity,
        MetaObjectFactory factory, ComponentParameters componentParameters)
        throws ActiveObjectCreationException, NodeException;

    public Component newActiveComponent(String className,
        Object[] constructorParameters, VirtualNode vn,
        ComponentParameters componentParameters)
        throws ActiveObjectCreationException, NodeException;

    public Object turnActive(Object target)
        throws ActiveObjectCreationException, NodeException;

    public Object turnActive(Object target, String nodeURL)
        throws ActiveObjectCreationException, NodeException;

    public Object turnActive(Object target, Node node)
        throws ActiveObjectCreationException, NodeException;

    public Object turnActive(Object target, Node node, Active activity,
        MetaObjectFactory factory)
        throws ActiveObjectCreationException, NodeException;

    public Object turnActive(Object target, String nameOfTargetType, Node node)
        throws ActiveObjectCreationException, NodeException;

    public Object turnActive(Object target, String nameOfTargetType, Node node,
        Active activity, MetaObjectFactory factory)
        throws ActiveObjectCreationException, NodeException;

    public Object turnActiveAsGroup(Object target, String nameOfTargetType,
        VirtualNode virtualnode)
        throws ActiveObjectCreationException, NodeException;

    public void register(Object obj, String url) throws java.io.IOException;

    public void unregister(String url) throws java.io.IOException;

    public Object lookupActive(String classname, String url)
        throws ActiveObjectCreationException, java.io.IOException;

    public String getActiveObjectNodeUrl(Object activeObject);

    public boolean isException(Object future);

    public void waitFor(Object future);

    public void waitFor(Object future, long timeout) throws ProActiveException;

    public ProActiveDescriptor getProactiveDescriptor(String xmlDescriptorUrl)
        throws ProActiveException;

    public void registerVirtualNode(VirtualNode virtualNode,
        String registrationProtocol, boolean replacePreviousBinding)
        throws ProActiveException;

    public VirtualNode lookupVirtualNode(String url) throws ProActiveException;

    public void unregisterVirtualNode(VirtualNode virtualNode)
        throws ProActiveException;

    public Body getBodyOnThis();

    public StubObject getStubOnThis();

    public void migrateTo(Object activeObject) throws MigrationException;

    public void migrateTo(String nodeURL) throws MigrationException;

    public void migrateTo(Node node) throws MigrationException;

    public void migrateTo(Body bodyToMigrate, Object activeObject,
        boolean priority) throws MigrationException;

    public void migrateTo(Body bodyToMigrate, String nodeURL, boolean priority)
        throws MigrationException;

    public void migrateTo(Body bodyToMigrate, Node node, boolean priority)
        throws MigrationException;

    public int waitForAny(java.util.Vector futures);

    public int waitForAny(java.util.Vector futures, long timeout)
        throws ProActiveException;

    public void waitForAll(java.util.Vector futures);

    public void waitForAll(java.util.Vector futures, long timeout)
        throws ProActiveException;

    public void waitForTheNth(java.util.Vector futures, int n);

    public void waitForTheNth(java.util.Vector futures, int n, long timeout)
        throws ProActiveException;

    public boolean allAwaited(java.util.Vector futures);

    public boolean isAwaited(Object future);

    public Object getFutureValue(Object future);

    public void enableAC(Object obj) throws java.io.IOException;

    public void disableAC(Object obj) throws java.io.IOException;

    public void setImmediateService(Object obj, String methodName)
        throws java.io.IOException;

    public String getJobId();

//    public Handler searchExceptionHandler(NonFunctionalException ex,
//        Object target);
//
//    public void setExceptionHandler(Handler h, Class exception, int levelID,
//        Object target);
//
//    public void setExceptionHandler(Class handler, Class exception,
//        int levelID, Object target);

    public void exposeAsWebService(Object o, String url, String urn,
        String[] methods);

    public void unExposeAsWebService(String urn, String url);

    public void exposeComponentAsWebService(Component component, String url,
        String componentName);

    public void unExposeComponentAsWebService(String componentName, String url,
        Component component);
}
