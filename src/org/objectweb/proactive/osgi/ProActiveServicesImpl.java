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
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.handler.Handler;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;

import java.io.IOException;

import java.util.Vector;


public class ProActiveServicesImpl implements ProActiveService {
    public ProActiveServicesImpl() {
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#newActive(java.lang.String, java.lang.Object[])
     */
    public Object newActive(String className, Object[] params)
        throws ActiveObjectCreationException, NodeException {
        return ProActive.newActive(className, params);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#newActive(java.lang.String, java.lang.Object[], java.lang.String)
     */
    public Object newActive(String classname, Object[] constructorParameters,
        String nodeURL) throws ActiveObjectCreationException, NodeException {
        return ProActive.newActive(classname, constructorParameters, nodeURL);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#newActive(java.lang.String, java.lang.Object[], org.objectweb.proactive.core.node.Node)
     */
    public Object newActive(String classname, Object[] constructorParameters,
        Node node) throws ActiveObjectCreationException, NodeException {
        return ProActive.newActive(classname, constructorParameters, node);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#newActive(java.lang.String, java.lang.Object[], org.objectweb.proactive.core.node.Node, org.objectweb.proactive.Active, org.objectweb.proactive.core.body.MetaObjectFactory)
     */
    public Object newActive(String classname, Object[] constructorParameters,
        Node node, Active activity, MetaObjectFactory factory)
        throws ActiveObjectCreationException, NodeException {
        return ProActive.newActive(classname, constructorParameters, node,
            activity, factory);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#newActiveAsGroup(java.lang.String, java.lang.Object[], org.objectweb.proactive.core.descriptor.data.VirtualNode)
     */
    public Object newActiveAsGroup(String classname,
        Object[] constructorParameters, VirtualNode virtualnode)
        throws ActiveObjectCreationException, NodeException {
        return ProActive.newActiveAsGroup(classname, constructorParameters,
            virtualnode);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#newActiveAsGroup(java.lang.String, java.lang.Object[], org.objectweb.proactive.core.descriptor.data.VirtualNode, org.objectweb.proactive.Active, org.objectweb.proactive.core.body.MetaObjectFactory)
     */
    public Object newActiveAsGroup(String classname,
        Object[] constructorParameters, VirtualNode virtualnode,
        Active activity, MetaObjectFactory factory)
        throws ActiveObjectCreationException, NodeException {
        return ProActive.newActiveAsGroup(classname, constructorParameters,
            virtualnode, activity, factory);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#newActiveComponent(java.lang.String, java.lang.Object[], org.objectweb.proactive.core.node.Node, org.objectweb.proactive.Active, org.objectweb.proactive.core.body.MetaObjectFactory, org.objectweb.proactive.core.component.ComponentParameters)
     */
    public Component newActiveComponent(String classname,
        Object[] constructorParameters, Node node, Active activity,
        MetaObjectFactory factory, ComponentParameters componentParameters)
        throws ActiveObjectCreationException, NodeException {
        return ProActive.newActiveComponent(classname, constructorParameters,
            node, activity, factory, componentParameters);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#newActiveComponent(java.lang.String, java.lang.Object[], org.objectweb.proactive.core.descriptor.data.VirtualNode, org.objectweb.proactive.core.component.ComponentParameters)
     */
    public Component newActiveComponent(String className,
        Object[] constructorParameters, VirtualNode vn,
        ComponentParameters componentParameters)
        throws ActiveObjectCreationException, NodeException {
        return ProActive.newActiveComponent(className, constructorParameters,
            vn, componentParameters);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#turnActive(java.lang.Object)
     */
    public Object turnActive(Object target)
        throws ActiveObjectCreationException, NodeException {
        return ProActive.turnActive(target);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#turnActive(java.lang.Object, java.lang.String)
     */
    public Object turnActive(Object target, String nodeURL)
        throws ActiveObjectCreationException, NodeException {
        return ProActive.turnActive(target, nodeURL);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#turnActive(java.lang.Object, org.objectweb.proactive.core.node.Node)
     */
    public Object turnActive(Object target, Node node)
        throws ActiveObjectCreationException, NodeException {
        return ProActive.turnActive(target, node);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#turnActive(java.lang.Object, org.objectweb.proactive.core.node.Node, org.objectweb.proactive.Active, org.objectweb.proactive.core.body.MetaObjectFactory)
     */
    public Object turnActive(Object target, Node node, Active activity,
        MetaObjectFactory factory)
        throws ActiveObjectCreationException, NodeException {
        return ProActive.turnActive(target, node, activity, factory);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#turnActive(java.lang.Object, java.lang.String, org.objectweb.proactive.core.node.Node)
     */
    public Object turnActive(Object target, String nameOfTargetType, Node node)
        throws ActiveObjectCreationException, NodeException {
        return ProActive.turnActive(target, nameOfTargetType, node);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#turnActive(java.lang.Object, java.lang.String, org.objectweb.proactive.core.node.Node, org.objectweb.proactive.Active, org.objectweb.proactive.core.body.MetaObjectFactory)
     */
    public Object turnActive(Object target, String nameOfTargetType, Node node,
        Active activity, MetaObjectFactory factory)
        throws ActiveObjectCreationException, NodeException {
        return ProActive.turnActive(target, nameOfTargetType, node, activity,
            factory);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#turnActiveAsGroup(java.lang.Object, java.lang.String, org.objectweb.proactive.core.descriptor.data.VirtualNode)
     */
    public Object turnActiveAsGroup(Object target, String nameOfTargetType,
        VirtualNode virtualnode)
        throws ActiveObjectCreationException, NodeException {
        return ProActive.turnActiveAsGroup(target, nameOfTargetType, virtualnode);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#register(java.lang.Object, java.lang.String)
     */
    public void register(Object obj, String url) throws IOException {
        ProActive.register(obj, url);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#unregister(java.lang.String)
     */
    public void unregister(String url) throws IOException {
        ProActive.unregister(url);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#lookupActive(java.lang.String, java.lang.String)
     */
    public Object lookupActive(String classname, String url)
        throws ActiveObjectCreationException, IOException {
        return ProActive.lookupActive(classname, url);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#getActiveObjectNodeUrl(java.lang.Object)
     */
    public String getActiveObjectNodeUrl(Object activeObject) {
        return ProActive.getActiveObjectNodeUrl(activeObject);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#isException(java.lang.Object)
     */
    public boolean isException(Object future) {
        return ProActive.isException(future);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#waitFor(java.lang.Object)
     */
    public void waitFor(Object future) {
        ProActive.waitFor(future);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#waitFor(java.lang.Object, long)
     */
    public void waitFor(Object future, long timeout) throws ProActiveException {
        ProActive.waitFor(future, timeout);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#getProactiveDescriptor(java.lang.String)
     */
    public ProActiveDescriptor getProactiveDescriptor(String xmlDescriptorUrl)
        throws ProActiveException {
        return ProActive.getProactiveDescriptor(xmlDescriptorUrl);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#registerVirtualNode(org.objectweb.proactive.core.descriptor.data.VirtualNode, java.lang.String, boolean)
     */
    public void registerVirtualNode(VirtualNode virtualNode,
        String registrationProtocol, boolean replacePreviousBinding)
        throws ProActiveException {
        ProActive.registerVirtualNode(virtualNode, registrationProtocol,
            replacePreviousBinding);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#lookupVirtualNode(java.lang.String)
     */
    public VirtualNode lookupVirtualNode(String url) throws ProActiveException {
        return ProActive.lookupVirtualNode(url);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#unregisterVirtualNode(org.objectweb.proactive.core.descriptor.data.VirtualNode)
     */
    public void unregisterVirtualNode(VirtualNode virtualNode)
        throws ProActiveException {
        ProActive.unregisterVirtualNode(virtualNode);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#getBodyOnThis()
     */
    public Body getBodyOnThis() {
        return ProActive.getBodyOnThis();
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#getStubOnThis()
     */
    public StubObject getStubOnThis() {
        return ProActive.getStubOnThis();
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#migrateTo(java.lang.Object)
     */
    public void migrateTo(Object activeObject) throws MigrationException {
        ProActive.migrateTo(activeObject);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#migrateTo(java.lang.String)
     */
    public void migrateTo(String nodeURL) throws MigrationException {
        ProActive.migrateTo(nodeURL);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#migrateTo(org.objectweb.proactive.core.node.Node)
     */
    public void migrateTo(Node node) throws MigrationException {
        ProActive.migrateTo(node);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#migrateTo(org.objectweb.proactive.Body, java.lang.Object, boolean)
     */
    public void migrateTo(Body bodyToMigrate, Object activeObject,
        boolean priority) throws MigrationException {
        ProActive.migrateTo(bodyToMigrate, activeObject, priority);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#migrateTo(org.objectweb.proactive.Body, java.lang.String, boolean)
     */
    public void migrateTo(Body bodyToMigrate, String nodeURL, boolean priority)
        throws MigrationException {
        ProActive.migrateTo(bodyToMigrate, nodeURL, priority);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#migrateTo(org.objectweb.proactive.Body, org.objectweb.proactive.core.node.Node, boolean)
     */
    public void migrateTo(Body bodyToMigrate, Node node, boolean priority)
        throws MigrationException {
        ProActive.migrateTo(bodyToMigrate, node, priority);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#waitForAny(java.util.Vector)
     */
    public int waitForAny(Vector futures) {
        return ProActive.waitForAny(futures);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#waitForAny(java.util.Vector, long)
     */
    public int waitForAny(Vector futures, long timeout)
        throws ProActiveException {
        return ProActive.waitForAny(futures, timeout);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#waitForAll(java.util.Vector)
     */
    public void waitForAll(Vector futures) {
        ProActive.waitFor(futures);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#waitForAll(java.util.Vector, long)
     */
    public void waitForAll(Vector futures, long timeout)
        throws ProActiveException {
        ProActive.waitForAll(futures, timeout);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#waitForTheNth(java.util.Vector, int)
     */
    public void waitForTheNth(Vector futures, int n) {
        ProActive.waitForTheNth(futures, n);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#waitForTheNth(java.util.Vector, int, long)
     */
    public void waitForTheNth(Vector futures, int n, long timeout)
        throws ProActiveException {
        ProActive.waitForTheNth(futures, n, timeout);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#allAwaited(java.util.Vector)
     */
    public boolean allAwaited(Vector futures) {
        return ProActive.allAwaited(futures);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#isAwaited(java.lang.Object)
     */
    public boolean isAwaited(Object future) {
        return ProActive.isAwaited(future);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#getFutureValue(java.lang.Object)
     */
    public Object getFutureValue(Object future) {
        return ProActive.getFutureValue(future);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#enableAC(java.lang.Object)
     */
    public void enableAC(Object obj) throws IOException {
        ProActive.enableAC(obj);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#disableAC(java.lang.Object)
     */
    public void disableAC(Object obj) throws IOException {
        ProActive.disableAC(obj);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#setImmediateService(java.lang.Object, java.lang.String)
     */
    public void setImmediateService(Object obj, String methodName)
        throws IOException {
        ProActive.setImmediateService(obj, methodName);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#getJobId()
     */
    public String getJobId() {
        return ProActive.getJobId();
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#searchExceptionHandler(org.objectweb.proactive.core.exceptions.NonFunctionalException, java.lang.Object)
     */
    public Handler searchExceptionHandler(NonFunctionalException ex,
        Object target) {
        return ProActive.searchExceptionHandler(ex, target);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#setExceptionHandler(org.objectweb.proactive.core.exceptions.handler.Handler, java.lang.Class, int, java.lang.Object)
     */
    public void setExceptionHandler(Handler h, Class exception, int levelID,
        Object target) {
        ProActive.setExceptionHandler(h, exception, levelID, target);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#setExceptionHandler(java.lang.Class, java.lang.Class, int, java.lang.Object)
     */
    public void setExceptionHandler(Class handler, Class exception,
        int levelID, Object target) {
        ProActive.setExceptionHandler(handler, exception, levelID, target);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#exposeAsWebService(java.lang.Object, java.lang.String, java.lang.String, java.lang.String[])
     */
    public void exposeAsWebService(Object o, String url, String urn,
        String[] methods) {
        ProActive.exposeAsWebService(o, url, urn, methods);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#unExposeAsWebService(java.lang.String, java.lang.String)
     */
    public void unExposeAsWebService(String urn, String url) {
        ProActive.unExposeAsWebService(urn, url);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#exposeComponentAsWebService(org.objectweb.fractal.api.Component, java.lang.String, java.lang.String)
     */
    public void exposeComponentAsWebService(Component component, String url,
        String componentName) {
        ProActive.exposeComponentAsWebService(component, url, componentName);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#unExposeComponentAsWebService(java.lang.String, java.lang.String, org.objectweb.fractal.api.Component)
     */
    public void unExposeComponentAsWebService(String componentName, String url,
        Component component) {
        ProActive.unExposeComponentAsWebService(componentName, url, component);
    }
}
