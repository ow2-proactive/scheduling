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
package org.objectweb.proactive.core.jmx;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.jmx.listeners.ListenerAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.util.wrapper.GenericTypeWrapper;


/**
 * This class represents a way to talk to an MBean server through ProActive.
 * @author ProActive Team
 *
 */
public class ProActiveConnection implements Serializable, MBeanServerConnection,
    ProActiveInternalObject {

    /**

     */
    private transient MBeanServer mbs;

    //private HashMap<NotificationListener, ListenerAdapter> listenerMap = new HashMap<NotificationListener, ListenerAdapter>();
    private HashMap<ObjectName, ListenerAdapter> objectNameToListenerMap = new HashMap<ObjectName, ListenerAdapter>();

    /**
     * Empty no arg constructor
     *
     */
    public ProActiveConnection() {
    }

    /**
     * Creates a ProActiveConnection which is attached to a Mbean server
     * @param server the MBean server the connection is attached to
     */
    public ProActiveConnection(MBeanServer server) {
        this.mbs = server;
    }

    /* *
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName)
     */
    public ObjectInstance createMBean(String arg0, ObjectName arg1,
        ObjectName arg2)
        throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException, InstanceNotFoundException,
            ReflectionException, MBeanException {
        return this.mbs.createMBean(arg0, arg1, arg2);
    }

    /* *
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
     */
    public ObjectInstance createMBean(String className, ObjectName name,
        Object[] params, String[] signature)
        throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException, ReflectionException, MBeanException {
        return this.mbs.createMBean(className, name, params, signature);
    }

    /**
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
     */
    public ObjectInstance createMBean(String className, ObjectName name,
        ObjectName loaderName, Object[] params, String[] signature)
        throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException, InstanceNotFoundException,
            ReflectionException, MBeanException {
        return this.mbs.createMBean(className, name, loaderName, params,
            signature);
    }

    /* *
     * @see javax.management.MBeanServerConnection#unregisterMBean(javax.management.ObjectName)
     */
    public void unregisterMBean(ObjectName name)
        throws InstanceNotFoundException, MBeanRegistrationException {
        this.mbs.unregisterMBean(name);
    }

    /**
     * @see javax.management.MBeanServerConnection#getObjectInstance(javax.management.ObjectName)
     */
    @SuppressWarnings("unused")
    public ObjectInstance getObjectInstance(ObjectName name)
        throws InstanceNotFoundException, IOException {
        return this.mbs.getObjectInstance(name);
    }

    /**
     * @see javax.management.MBeanServerConnection#queryMBeans(javax.management.ObjectName, javax.management.QueryExp)
     */
    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query)
        throws IOException {
        return this.mbs.queryMBeans(name, query);
    }

    /**
     * @see javax.management.MBeanServerConnection#queryNames(javax.management.ObjectName, javax.management.QueryExp)
     */
    @SuppressWarnings("unused")
    public Set queryNames(ObjectName name, QueryExp query)
        throws IOException {
        return this.mbs.queryNames(name, query);
    }

    /**
     * @see javax.management.MBeanServerConnection#isRegistered(javax.management.ObjectName)
     */
    @SuppressWarnings("unused")
    public boolean isRegistered(ObjectName name) throws IOException {
        return this.mbs.isRegistered(name);
    }

    /**
     * @see javax.management.MBeanServerConnection#getMBeanCount()
     */
    @SuppressWarnings("unused")
    public Integer getMBeanCount() throws IOException {
        return this.mbs.getMBeanCount();
    }

    /**
     * @see javax.management.MBeanServerConnection#getAttribute(javax.management.ObjectName, java.lang.String)
     */
    @SuppressWarnings("unused")
    public Object getAttribute(ObjectName name, String attribute)
        throws MBeanException, AttributeNotFoundException,
            InstanceNotFoundException, ReflectionException, IOException {
        //      System.out.println("Get Attribute " + attribute + " --  " + name);
        return this.mbs.getAttribute(name, attribute);
    }

    /**
     * @see javax.management.MBeanServerConnection#getAttributes(javax.management.ObjectName, java.lang.String[])
     */
    @SuppressWarnings("unused")
    public AttributeList getAttributes(ObjectName name, String[] attributes)
        throws InstanceNotFoundException, ReflectionException, IOException {
        return mbs.getAttributes(name, attributes);
    }

    /**
     * @see javax.management.MBeanServerConnection#setAttribute(javax.management.ObjectName, javax.management.Attribute)
     */
    @SuppressWarnings("unused")
    public void setAttribute(ObjectName name, Attribute attribute)
        throws InstanceNotFoundException, AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException,
            IOException {
        this.mbs.setAttribute(name, attribute);
    }

    /**
     * @see javax.management.MBeanServerConnection#setAttributes(javax.management.ObjectName, javax.management.AttributeList)
     */
    @SuppressWarnings("unused")
    public AttributeList setAttributes(ObjectName name, AttributeList attributes)
        throws InstanceNotFoundException, ReflectionException, IOException {
        return this.mbs.setAttributes(name, attributes);
    }

    /**
     * @see javax.management.MBeanServerConnection#invoke(javax.management.ObjectName, java.lang.String, java.lang.Object[], java.lang.String[])
     */
    @SuppressWarnings("unused")
    public Object invoke(ObjectName name, String operationName,
        Object[] params, String[] signature)
        throws InstanceNotFoundException, MBeanException, ReflectionException,
            IOException {
        return this.mbs.invoke(name, operationName, params, signature);
    }

    /**
     * @see javax.management.MBeanServerConnection#getDefaultDomain()
     */
    @SuppressWarnings("unused")
    public String getDefaultDomain() throws IOException {
        return this.mbs.getDefaultDomain();
    }

    /**
     * @see javax.management.MBeanServerConnection#getDomains()
     */
    @SuppressWarnings("unused")
    public String[] getDomains() throws IOException {
        return this.mbs.getDomains();
    }

    /**
     * @see javax.management.MBeanServerConnection#addNotificationListener(javax.management.ObjectName, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
     */
    @SuppressWarnings("unused")
    public void addNotificationListener(ObjectName name, ObjectName listener,
        NotificationFilter filter, Object handback)
        throws InstanceNotFoundException, IOException {
        // System.out.println("ProActiveConnection: add Notification Listener " +
        //    listener.toString() + " for object " + name);
        this.mbs.addNotificationListener(name, listener, filter, handback);
    }

    /**
     * @see javax.management.MBeanServerConnection#addNotificationListener(javax.management.ObjectName, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    @SuppressWarnings("unused")
    public void addNotificationListener(ObjectName name,
        NotificationListener listener, NotificationFilter filter,
        Object handback) throws InstanceNotFoundException, IOException {
        try {
            ListenerAdapter tl = new ListenerAdapter(listener, mbs, name);
            //    System.out.println("ProActiveConnection: add ListenerAdapter " +
            //        tl.toString() + " for object " + name);
            //  this.listenerMap.put(listener, tl);
            this.objectNameToListenerMap.put(name, tl);
            this.mbs.addNotificationListener(name, tl, filter, handback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.ObjectName)
     */
    @SuppressWarnings("unused")
    public void removeNotificationListener(ObjectName name, ObjectName listener)
        throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        // ListenerAdapter tl = this.listenerMap.get(listener);
        ListenerAdapter tl = this.objectNameToListenerMap.get(name);
        this.mbs.removeNotificationListener(name, tl);
    }

    /**
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
     */
    @SuppressWarnings("unused")
    public void removeNotificationListener(ObjectName name,
        ObjectName listener, NotificationFilter filter, Object handback)
        throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        ListenerAdapter tl = this.objectNameToListenerMap.get(name);
        this.mbs.removeNotificationListener(name, tl, filter, handback);
    }

    /**
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.NotificationListener)
     */
    @SuppressWarnings("unused")
    public void removeNotificationListener(ObjectName name,
        NotificationListener listener)
        throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        ListenerAdapter tl = this.objectNameToListenerMap.get(name);
        this.mbs.removeNotificationListener(name, tl);
    }

    /**
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    @SuppressWarnings("unused")
    public void removeNotificationListener(ObjectName name,
        NotificationListener listener, NotificationFilter filter,
        Object handback)
        throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        // ListenerAdapter tl = this.listenerMap.get(listener);
        ListenerAdapter tl = this.objectNameToListenerMap.get(name);
        //     System.out.println("ProActiveConnection: removing ListenerAdapter " +
        //         tl.toString() + " for object " + name);
        this.mbs.removeNotificationListener(name, tl, filter, handback);
    }

    /**
     * @see javax.management.MBeanServerConnection#getMBeanInfo(javax.management.ObjectName)
     */
    @SuppressWarnings("unused")
    public MBeanInfo getMBeanInfo(ObjectName name)
        throws InstanceNotFoundException, IntrospectionException,
            ReflectionException, IOException {
        return this.mbs.getMBeanInfo(name);
    }

    /**
     * @see javax.management.MBeanServerConnection#isInstanceOf(javax.management.ObjectName, java.lang.String)
     */
    @SuppressWarnings("unused")
    public boolean isInstanceOf(ObjectName name, String className)
        throws InstanceNotFoundException, IOException {
        return this.mbs.isInstanceOf(name, className);
    }

    /**
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName)
     */
    @SuppressWarnings("unused")
    public ObjectInstance createMBean(String className, ObjectName name)
        throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException,
            NotCompliantMBeanException, IOException {
        return this.mbs.createMBean(className, name);
    }

    /**
     * This method is the same as createMBean but returns a reifiable type in order to perform ProActive asynchronous call
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName)
     */
    public GenericTypeWrapper createMBeanAsynchronous(String className,
        ObjectName name) {
        try {
            return new GenericTypeWrapper<ObjectInstance>(this.createMBean(
                    className, name));
        } catch (Exception e) {
            return new GenericTypeWrapper<Exception>(e);
        }
    }

    /**
      * This method is the same as createMBean but returns a reifiable type in order to perform ProActive asynchronous call
      * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName)
      */
    public GenericTypeWrapper createMBeanAsynchronous(String className,
        ObjectName name, ObjectName loaderName) {
        try {
            return new GenericTypeWrapper<ObjectInstance>(this.createMBean(
                    className, name, loaderName));
        } catch (Exception e) {
            return new GenericTypeWrapper<Exception>(e);
        }
    }

    /* *
     * This method is the same as createMBean but returns a reifiable type in order to perform ProActive asynchronous call
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
     */
    public GenericTypeWrapper createMBeanAsynchronous(String className,
        ObjectName name, Object[] params, String[] signature) {
        try {
            return new GenericTypeWrapper<ObjectInstance>(this.createMBean(
                    className, name, params, signature));
        } catch (Exception e) {
            return new GenericTypeWrapper<Exception>(e);
        }
    }

    /**
     * This method is the same as createMBean but returns a reifiable type in order to perform ProActive asynchronous call
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
     */
    public GenericTypeWrapper createMBeanAsynchronous(String className,
        ObjectName name, ObjectName loaderName, Object[] params,
        String[] signature) {
        try {
            return new GenericTypeWrapper<ObjectInstance>(this.createMBean(
                    className, name, loaderName, params, signature));
        } catch (Exception e) {
            return new GenericTypeWrapper<Exception>(e);
        }
    }

    /**
     * This method is the same as getObjectInstance but returns a reifiable type in order to perform ProActive asynchronous call
     * @see javax.management.MBeanServerConnection#getObjectInstance(javax.management.ObjectName)
     */
    public GenericTypeWrapper getObjectInstanceAsynchronous(ObjectName name) {
        try {
            return new GenericTypeWrapper<ObjectInstance>(this.getObjectInstance(
                    name));
        } catch (Exception e) {
            return new GenericTypeWrapper<Exception>(e);
        }
    }

    /**
     *   This method is the same as queryMBeans but returns a reifiable type in order to perform ProActive asynchronous call
     * @see javax.management.MBeanServerConnection#queryMBeans(javax.management.ObjectName, javax.management.QueryExp)
     */
    public GenericTypeWrapper queryMBeansAsynchronous(ObjectName name,
        QueryExp query) {
        try {
            return new GenericTypeWrapper<Set>(this.queryMBeans(name, query));
        } catch (IOException e) {
            return new GenericTypeWrapper<Exception>(e);
        }
    }

    /**
     * This method  is the same as queryNames but returns a reifiable type in order to perform ProActive asynchronous call
    * @see javax.management.MBeanServerConnection#queryNames(javax.management.ObjectName, javax.management.QueryExp)
    */
    public GenericTypeWrapper queryNamesAsynchronous(ObjectName name,
        QueryExp query) {
        try {
            return new GenericTypeWrapper<Set>(this.queryNames(name, query));
        } catch (IOException e) {
            return new GenericTypeWrapper<Exception>(e);
        }
    }

    /**
     * This method  is the same as getMBeanCount but returns a reifiable type in order to perform ProActive asynchronous call
     * @see javax.management.MBeanServerConnection#getMBeanInfo(javax.management.ObjectName)
    */
    public GenericTypeWrapper getMBeanCountAsynchronous() {
        try {
            return new GenericTypeWrapper<Integer>(this.getMBeanCount());
        } catch (IOException e) {
            return new GenericTypeWrapper<Exception>(e);
        }
    }

    /**
     * This method  is the same as getAttribute  but returns a reifiable type in order to perform ProActive asynchronous call
     * @see javax.management.MBeanServerConnection#getAttribute(javax.management.ObjectName, java.lang.String)
     */
    public GenericTypeWrapper getAttributeAsynchronous(ObjectName name,
        String attribute) {
        try {
            return new GenericTypeWrapper<Object>(this.getAttribute(name,
                    attribute));
        } catch (Exception e) {
            return new GenericTypeWrapper<Exception>(e);
        }
    }

    /**
     * This method  is the same as getAttributes  but returns a reifiable type in order to perform ProActive asynchronous call
     * @see javax.management.MBeanServerConnection#getAttributes(javax.management.ObjectName, java.lang.String[])
     */
    public GenericTypeWrapper getAttributesAsynchronous(ObjectName name,
        String[] attributes) {
        try {
            return new GenericTypeWrapper<AttributeList>(this.getAttributes(
                    name, attributes));
        } catch (Exception e) {
            return new GenericTypeWrapper<Exception>(e);
        }
    }

    /**
     * This method  is the same as setAttributes  but returns a reifiable type in order to perform ProActive asynchronous call
           * @see javax.management.MBeanServerConnection#setAttributes(javax.management.ObjectName, javax.management.AttributeList)
           */
    public GenericTypeWrapper setAttributesAsynchronous(ObjectName name,
        AttributeList attributes) {
        try {
            return new GenericTypeWrapper<AttributeList>(this.setAttributes(
                    name, attributes));
        } catch (Exception e) {
            return new GenericTypeWrapper<Exception>(e);
        }
    }

    /**
     * This method  is the same as invoke  but returns a reifiable type in order to perform ProActive asynchronous call
     * @see javax.management.MBeanServerConnection#invoke(javax.management.ObjectName, java.lang.String, java.lang.Object[], java.lang.String[])
           */
    public GenericTypeWrapper invokeAsynchronous(ObjectName name,
        String operationName, Object[] params, String[] signature) {
        try {
            return new GenericTypeWrapper<Object>(this.invoke(name,
                    operationName, params, signature));
        } catch (Exception e) {
            return new GenericTypeWrapper<Exception>(e);
        }
    }

    /**
     * This method  is the same as getDefaultDomain  but returns a reifiable type in order to perform ProActive asynchronous call
     * @see javax.management.MBeanServerConnection#getDefaultDomain()
           */
    public GenericTypeWrapper getDefaultDomainAsynchronous() {
        try {
            return new GenericTypeWrapper<String>(this.getDefaultDomain());
        } catch (IOException e) {
            return new GenericTypeWrapper<Exception>(e);
        }
    }

    /**
     * This method  is the same as getDomain  but returns a reifiable type in order to perform ProActive asynchronous call
     * @see javax.management.MBeanServerConnection#getDomains()
           */
    public GenericTypeWrapper getDomainsAsynchronous() {
        try {
            return new GenericTypeWrapper<String[]>(this.getDomains());
        } catch (IOException e) {
            return new GenericTypeWrapper<Exception>(e);
        }
    }

    /**
     * This method  is the same as getMBeanInfo  but returns a reifiable type in order to perform ProActive asynchronous call
           * @see javax.management.MBeanServerConnection#getMBeanInfo(javax.management.ObjectName)
            *    */
    public GenericTypeWrapper getMBeanInfoAsynchronous(ObjectName name) {
        try {
            return new GenericTypeWrapper<MBeanInfo>(this.getMBeanInfo(name));
        } catch (Exception e) {
            return new GenericTypeWrapper<Exception>(e);
        }
    }

    public UniqueID getUniqueID() {
        return PAActiveObject.getBodyOnThis().getID();
    }

    public void unsubscribeFromRegistry() {
        Body myBody = PAActiveObject.getBodyOnThis();
        if (myBody instanceof AbstractBody) {
            RemoteObjectExposer roe = ((AbstractBody) myBody).getRemoteObjectExposer();
            roe.unregisterAll();
        }
    }
}
