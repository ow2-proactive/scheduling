/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.jmx.provider.ro;

import java.io.IOException;
import java.io.Serializable;
import java.security.AccessControlContext;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
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
import javax.security.auth.Subject;

import org.ow2.proactive.jmx.provider.JMXProviderUtils;


/**
 * This class represents a way to talk to an MBean server through Remote Objects.
 * 
 * @author The ProActive Team 
 */
public class ROConnection implements MBeanServerConnection, Serializable {
    /**  */
    private static final long serialVersionUID = 21L;
    /** The attached MBean server */
    private final transient MBeanServer mbs;
    /** The id of this connection */
    private final String connectionId;
    /** The local reference on the connection server */
    private final transient ROServerImpl server;
    /** The authenticated subject to be used for authorization */
    private final transient Subject subject;
    /** The access control context */
    private final transient AccessControlContext context;

    /**
     * Empty constructor without arguments.
     */
    public ROConnection() {
        this.mbs = null;
        this.connectionId = null;
        this.server = null;
        this.subject = null;
        this.context = null;
    }

    /**
     * Creates a new <code>ROConnection</code> that forwards remote calls to the attached MBean server.</p>
     * @param mbs the MBean server the connection is attached to
     * @param connectionId the MBean server the connection is attached to
     * @param server the MBean server the connection is attached to
     * @param subject the MBean server the connection is attached to
     * @param acc the MBean server the connection is attached to
     */
    public ROConnection(final MBeanServer mbs, final String connectionId, final ROServerImpl server,
            final Subject subject, final AccessControlContext acc) {
        this.mbs = mbs;
        this.server = server;
        this.connectionId = connectionId;
        this.subject = subject;
        this.context = acc;
    }

    //-------------------------------------------------------------------------
    // MBeanServerConnection Wrapper
    //-------------------------------------------------------------------------

    /**
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName)
     */
    public ObjectInstance createMBean(final String className, final ObjectName name)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
            MBeanException, NotCompliantMBeanException, IOException {
        if (this.subject == null) {
            return this.mbs.createMBean(className, name);
        }
        try {
            return (ObjectInstance) Subject.doAsPrivileged(this.subject,
                    new PrivilegedExceptionAction<ObjectInstance>() {
                        public final ObjectInstance run() throws Exception {
                            return mbs.createMBean(className, name);
                        }
                    }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof ReflectionException)
                throw (ReflectionException) e;
            if (e instanceof InstanceAlreadyExistsException)
                throw (InstanceAlreadyExistsException) e;
            if (e instanceof MBeanRegistrationException)
                throw (MBeanRegistrationException) e;
            if (e instanceof MBeanException)
                throw (MBeanException) e;
            if (e instanceof NotCompliantMBeanException)
                throw (NotCompliantMBeanException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName)
     */
    public ObjectInstance createMBean(final String className, final ObjectName name,
            final ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
            InstanceNotFoundException, IOException {
        if (this.subject == null) {
            return this.mbs.createMBean(className, name, loaderName);
        }
        try {
            return (ObjectInstance) Subject.doAsPrivileged(this.subject,
                    new PrivilegedExceptionAction<ObjectInstance>() {
                        public final ObjectInstance run() throws Exception {
                            return mbs.createMBean(className, name, loaderName);
                        }
                    }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof ReflectionException)
                throw (ReflectionException) e;
            if (e instanceof InstanceAlreadyExistsException)
                throw (InstanceAlreadyExistsException) e;
            if (e instanceof MBeanRegistrationException)
                throw (MBeanRegistrationException) e;
            if (e instanceof MBeanException)
                throw (MBeanException) e;
            if (e instanceof NotCompliantMBeanException)
                throw (NotCompliantMBeanException) e;
            if (e instanceof InstanceNotFoundException)
                throw (InstanceNotFoundException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
     */
    public ObjectInstance createMBean(final String className, final ObjectName name, final Object[] params,
            final String[] signature) throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException {
        if (this.subject == null) {
            return this.mbs.createMBean(className, name, params, signature);
        }
        try {
            return (ObjectInstance) Subject.doAsPrivileged(this.subject,
                    new PrivilegedExceptionAction<ObjectInstance>() {
                        public final ObjectInstance run() throws Exception {
                            return mbs.createMBean(className, name, params, signature);
                        }
                    }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof ReflectionException)
                throw (ReflectionException) e;
            if (e instanceof InstanceAlreadyExistsException)
                throw (InstanceAlreadyExistsException) e;
            if (e instanceof MBeanRegistrationException)
                throw (MBeanRegistrationException) e;
            if (e instanceof MBeanException)
                throw (MBeanException) e;
            if (e instanceof NotCompliantMBeanException)
                throw (NotCompliantMBeanException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
     */
    public ObjectInstance createMBean(final String className, final ObjectName name,
            final ObjectName loaderName, final Object[] params, final String[] signature)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
            MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException {
        if (this.subject == null) {
            return this.mbs.createMBean(className, name, params, signature);
        }
        try {
            return (ObjectInstance) Subject.doAsPrivileged(this.subject,
                    new PrivilegedExceptionAction<ObjectInstance>() {
                        public final ObjectInstance run() throws Exception {
                            return mbs.createMBean(className, name, loaderName, params, signature);
                        }
                    }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof ReflectionException)
                throw (ReflectionException) e;
            if (e instanceof InstanceAlreadyExistsException)
                throw (InstanceAlreadyExistsException) e;
            if (e instanceof MBeanRegistrationException)
                throw (MBeanRegistrationException) e;
            if (e instanceof MBeanException)
                throw (MBeanException) e;
            if (e instanceof NotCompliantMBeanException)
                throw (NotCompliantMBeanException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#unregisterMBean(javax.management.ObjectName)
     */
    public void unregisterMBean(final ObjectName name) throws InstanceNotFoundException,
            MBeanRegistrationException, IOException {
        if (this.context == null) {
            this.mbs.unregisterMBean(name);
            return;
        }
        try {
            Subject.doAsPrivileged(this.subject, new PrivilegedExceptionAction<Object>() {
                public final Object run() throws Exception {
                    mbs.unregisterMBean(name);
                    return null; // nothing to return
                }
            }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof InstanceNotFoundException)
                throw (InstanceNotFoundException) e;
            if (e instanceof MBeanRegistrationException)
                throw (MBeanRegistrationException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#getObjectInstance(javax.management.ObjectName)
     */
    public ObjectInstance getObjectInstance(final ObjectName name) throws InstanceNotFoundException,
            IOException {
        if (this.subject == null) {
            return this.mbs.getObjectInstance(name);
        }
        try {
            return (ObjectInstance) Subject.doAsPrivileged(this.subject,
                    new PrivilegedExceptionAction<ObjectInstance>() {
                        public final ObjectInstance run() throws Exception {
                            return mbs.getObjectInstance(name);
                        }
                    }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof InstanceNotFoundException)
                throw (InstanceNotFoundException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#queryMBeans(javax.management.ObjectName, javax.management.QueryExp)
     */
    @SuppressWarnings("unchecked")
    public Set<ObjectInstance> queryMBeans(final ObjectName name, final QueryExp query) throws IOException {
        if (this.context == null) {
            return this.mbs.queryMBeans(name, query);
        }
        try {
            return (Set<ObjectInstance>) Subject.doAsPrivileged(this.subject,
                    new PrivilegedExceptionAction<Set<ObjectInstance>>() {
                        public final Set<ObjectInstance> run() throws Exception {
                            return mbs.queryMBeans(name, query);
                        }
                    }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#queryNames(javax.management.ObjectName, javax.management.QueryExp)
     */
    @SuppressWarnings("unchecked")
    public Set<ObjectName> queryNames(final ObjectName name, final QueryExp query) throws IOException {
        if (this.subject == null) {
            return this.mbs.queryNames(name, query);
        }
        try {
            return (Set<ObjectName>) Subject.doAsPrivileged(this.subject,
                    new PrivilegedExceptionAction<Set<ObjectName>>() {
                        public final Set<ObjectName> run() throws Exception {
                            return mbs.queryNames(name, query);
                        }
                    }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#isRegistered(javax.management.ObjectName)
     */
    public boolean isRegistered(final ObjectName name) throws IOException {
        if (this.subject == null) {
            return this.mbs.isRegistered(name);
        }
        try {
            return (Boolean) Subject.doAsPrivileged(this.subject, new PrivilegedExceptionAction<Boolean>() {
                public final Boolean run() throws Exception {
                    return mbs.isRegistered(name);
                }
            }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#getMBeanCount()
     */
    public Integer getMBeanCount() throws IOException {
        if (this.subject == null) {
            return this.mbs.getMBeanCount();
        }
        try {
            return (Integer) Subject.doAsPrivileged(this.subject, new PrivilegedExceptionAction<Integer>() {
                public final Integer run() throws Exception {
                    return mbs.getMBeanCount();
                }
            }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#getAttribute(javax.management.ObjectName, java.lang.String)
     */
    public Object getAttribute(final ObjectName name, final String attribute) throws MBeanException,
            AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
        if (this.subject == null) {
            return this.mbs.getAttribute(name, attribute);
        }
        try {
            return Subject.doAsPrivileged(this.subject, new PrivilegedExceptionAction<Object>() {
                public final Object run() throws Exception {
                    return mbs.getAttribute(name, attribute);
                }
            }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof MBeanException)
                throw (MBeanException) e;
            if (e instanceof AttributeNotFoundException)
                throw (AttributeNotFoundException) e;
            if (e instanceof InstanceNotFoundException)
                throw (InstanceNotFoundException) e;
            if (e instanceof ReflectionException)
                throw (ReflectionException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#getAttributes(javax.management.ObjectName, java.lang.String[])
     */
    public AttributeList getAttributes(final ObjectName name, final String[] attributes)
            throws InstanceNotFoundException, ReflectionException, IOException {
        if (this.subject == null) {
            return this.mbs.getAttributes(name, attributes);
        }
        try {
            return (AttributeList) Subject.doAsPrivileged(this.subject,
                    new PrivilegedExceptionAction<AttributeList>() {
                        public final AttributeList run() throws Exception {
                            return mbs.getAttributes(name, attributes);
                        }
                    }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof InstanceNotFoundException)
                throw (InstanceNotFoundException) e;
            if (e instanceof ReflectionException)
                throw (ReflectionException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#setAttribute(javax.management.ObjectName, javax.management.Attribute)
     */
    public void setAttribute(final ObjectName name, final Attribute attribute)
            throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException, IOException {
        if (this.subject == null) {
            this.mbs.setAttribute(name, attribute);
            return;
        }
        try {
            Subject.doAsPrivileged(this.subject, new PrivilegedExceptionAction<Object>() {
                public final Object run() throws Exception {
                    mbs.setAttribute(name, attribute);
                    return null; // nothing to return
                }
            }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof InstanceNotFoundException)
                throw (InstanceNotFoundException) e;
            if (e instanceof AttributeNotFoundException)
                throw (AttributeNotFoundException) e;
            if (e instanceof InvalidAttributeValueException)
                throw (InvalidAttributeValueException) e;
            if (e instanceof MBeanException)
                throw (MBeanException) e;
            if (e instanceof ReflectionException)
                throw (ReflectionException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#setAttributes(javax.management.ObjectName, javax.management.AttributeList)
     */
    public AttributeList setAttributes(final ObjectName name, final AttributeList attributes)
            throws InstanceNotFoundException, ReflectionException, IOException {
        if (this.subject == null) {
            return this.mbs.setAttributes(name, attributes);
        }
        try {
            return (AttributeList) Subject.doAsPrivileged(this.subject,
                    new PrivilegedExceptionAction<AttributeList>() {
                        public final AttributeList run() throws Exception {
                            return mbs.setAttributes(name, attributes);
                        }
                    }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof InstanceNotFoundException)
                throw (InstanceNotFoundException) e;
            if (e instanceof ReflectionException)
                throw (ReflectionException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#invoke(javax.management.ObjectName, java.lang.String, java.lang.Object[], java.lang.String[])
     */
    public Object invoke(final ObjectName name, final String operationName, final Object[] params,
            final String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException,
            IOException {
        if (this.subject == null) {
            return this.mbs.invoke(name, operationName, params, signature);
        }
        try {
            return Subject.doAsPrivileged(this.subject, new PrivilegedExceptionAction<Object>() {
                public final Object run() throws Exception {
                    return mbs.invoke(name, operationName, params, signature);
                }
            }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof InstanceNotFoundException)
                throw (InstanceNotFoundException) e;
            if (e instanceof MBeanException)
                throw (MBeanException) e;
            if (e instanceof ReflectionException)
                throw (ReflectionException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#getDefaultDomain()
     */
    public String getDefaultDomain() throws IOException {
        if (this.subject == null) {
            return this.mbs.getDefaultDomain();
        }
        try {
            return (String) Subject.doAsPrivileged(this.subject, new PrivilegedExceptionAction<String>() {
                public final String run() throws Exception {
                    return mbs.getDefaultDomain();
                }
            }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#getDomains()
     */
    public String[] getDomains() throws IOException {
        if (this.subject == null) {
            return this.mbs.getDomains();
        }
        try {
            return (String[]) Subject.doAsPrivileged(this.subject, new PrivilegedExceptionAction<String[]>() {
                public final String[] run() throws Exception {
                    return mbs.getDomains();
                }
            }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#addNotificationListener(javax.management.ObjectName, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    public void addNotificationListener(final ObjectName name, final NotificationListener listener,
            final NotificationFilter filter, final Object handback) throws InstanceNotFoundException,
            IOException {
        if (this.subject == null) {
            this.mbs.addNotificationListener(name, listener, filter, handback);
        } else {
            try {
                Subject.doAsPrivileged(this.subject, new PrivilegedExceptionAction<Object>() {
                    public final Object run() throws Exception {
                        mbs.addNotificationListener(name, listener, filter, handback);
                        return null;
                    }
                }, this.context);
            } catch (final PrivilegedActionException pe) {
                final Exception e = JMXProviderUtils.extractException(pe);
                if (e instanceof InstanceNotFoundException)
                    throw (InstanceNotFoundException) e;
                if (e instanceof IOException)
                    throw (IOException) e;
                throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
            }
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#addNotificationListener(javax.management.ObjectName, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
     */
    public void addNotificationListener(final ObjectName name, final ObjectName listener,
            final NotificationFilter filter, final Object handback) throws InstanceNotFoundException,
            IOException {
        if (this.subject == null) {
            this.mbs.addNotificationListener(name, listener, filter, handback);
            return;
        }
        try {
            Subject.doAsPrivileged(this.subject, new PrivilegedExceptionAction<Object>() {
                public final Object run() throws Exception {
                    mbs.addNotificationListener(name, listener, filter, handback);
                    return null; // nothing to return;
                }
            }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof InstanceNotFoundException)
                throw (InstanceNotFoundException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.ObjectName)
     */
    public void removeNotificationListener(final ObjectName name, final ObjectName listener)
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        if (this.subject == null) {
            this.mbs.removeNotificationListener(name, listener);
            return;
        }
        try {
            Subject.doAsPrivileged(this.subject, new PrivilegedExceptionAction<Object>() {
                public final Object run() throws Exception {
                    mbs.removeNotificationListener(name, listener);
                    return null; // nothing to return;
                }
            }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof InstanceNotFoundException)
                throw (InstanceNotFoundException) e;
            if (e instanceof ListenerNotFoundException)
                throw (ListenerNotFoundException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
     */
    public void removeNotificationListener(final ObjectName name, final ObjectName listener,
            final NotificationFilter filter, final Object handback) throws InstanceNotFoundException,
            ListenerNotFoundException, IOException {
        if (this.subject == null) {
            this.mbs.removeNotificationListener(name, listener, filter, handback);
            return;
        }
        try {
            Subject.doAsPrivileged(this.subject, new PrivilegedExceptionAction<Object>() {
                public final Object run() throws Exception {
                    mbs.removeNotificationListener(name, listener, filter, handback);
                    return null; // nothing to return;
                }
            }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof InstanceNotFoundException)
                throw (InstanceNotFoundException) e;
            if (e instanceof ListenerNotFoundException)
                throw (ListenerNotFoundException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.NotificationListener)
     */
    public void removeNotificationListener(final ObjectName name, final NotificationListener listener)
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        if (this.subject == null) {
            this.mbs.removeNotificationListener(name, listener);
        } else {
            try {
                Subject.doAsPrivileged(this.subject, new PrivilegedExceptionAction<Object>() {
                    public final Object run() throws Exception {
                        mbs.removeNotificationListener(name, listener);
                        return null; // nothing to return;
                    }
                }, this.context);
            } catch (final PrivilegedActionException pe) {
                final Exception e = JMXProviderUtils.extractException(pe);
                if (e instanceof InstanceNotFoundException)
                    throw (InstanceNotFoundException) e;
                if (e instanceof ListenerNotFoundException)
                    throw (ListenerNotFoundException) e;
                if (e instanceof IOException)
                    throw (IOException) e;
                throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
            }
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    public void removeNotificationListener(final ObjectName name, final NotificationListener listener,
            final NotificationFilter filter, final Object handback) throws InstanceNotFoundException,
            ListenerNotFoundException, IOException {
        if (this.subject == null) {
            this.mbs.removeNotificationListener(name, listener, filter, handback);
        } else {
            try {
                Subject.doAsPrivileged(this.subject, new PrivilegedExceptionAction<Object>() {
                    public final Object run() throws Exception {
                        mbs.removeNotificationListener(name, listener, filter, handback);
                        return null; // nothing to return;
                    }
                }, this.context);
            } catch (final PrivilegedActionException pe) {
                final Exception e = JMXProviderUtils.extractException(pe);
                if (e instanceof InstanceNotFoundException)
                    throw (InstanceNotFoundException) e;
                if (e instanceof ListenerNotFoundException)
                    throw (ListenerNotFoundException) e;
                if (e instanceof IOException)
                    throw (IOException) e;
                throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
            }
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#getMBeanInfo(javax.management.ObjectName)
     */
    public MBeanInfo getMBeanInfo(final ObjectName name) throws InstanceNotFoundException,
            IntrospectionException, ReflectionException, IOException {
        if (this.subject == null) {
            return this.mbs.getMBeanInfo(name);
        }
        try {
            return (MBeanInfo) Subject.doAsPrivileged(this.subject,
                    new PrivilegedExceptionAction<MBeanInfo>() {
                        public final MBeanInfo run() throws Exception {
                            return mbs.getMBeanInfo(name);
                        }
                    }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof InstanceNotFoundException)
                throw (InstanceNotFoundException) e;
            if (e instanceof IntrospectionException)
                throw (IntrospectionException) e;
            if (e instanceof ReflectionException)
                throw (ReflectionException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    /**
     * @see javax.management.MBeanServerConnection#isInstanceOf(javax.management.ObjectName, java.lang.String)
     */
    public boolean isInstanceOf(final ObjectName name, final String className)
            throws InstanceNotFoundException, IOException {
        if (this.subject == null) {
            return this.mbs.isInstanceOf(name, className);
        }
        try {
            return (Boolean) Subject.doAsPrivileged(this.subject, new PrivilegedExceptionAction<Boolean>() {
                public final Boolean run() throws Exception {
                    return mbs.isInstanceOf(name, className);
                }
            }, this.context);
        } catch (final PrivilegedActionException pe) {
            final Exception e = JMXProviderUtils.extractException(pe);
            if (e instanceof InstanceNotFoundException)
                throw (InstanceNotFoundException) e;
            if (e instanceof IOException)
                throw (IOException) e;
            throw JMXProviderUtils.newIOException("Got unexpected server exception: " + e, e);
        }
    }

    //-------------------------------------------------------------------------
    // NOT PART OF MBeanServerConnection
    //-------------------------------------------------------------------------

    /**
     * Closes this connection.
     */
    public void close() throws IOException {
        this.server.closeConnectionById(this.connectionId);
    }

    /**
     * Returns the id of this connection.
     * @return the id as a string
     */
    public String getConnectionId() {
        return this.connectionId;
    }
}
