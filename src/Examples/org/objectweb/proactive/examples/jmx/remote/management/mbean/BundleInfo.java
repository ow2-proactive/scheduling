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
package org.objectweb.proactive.examples.jmx.remote.management.mbean;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.objectweb.proactive.examples.jmx.remote.management.command.CommandMBean;
import org.objectweb.proactive.examples.jmx.remote.management.command.osgi.OSGiCommand;
import org.objectweb.proactive.examples.jmx.remote.management.command.osgi.StartCommand;
import org.objectweb.proactive.examples.jmx.remote.management.command.osgi.StopCommand;
import org.objectweb.proactive.examples.jmx.remote.management.command.osgi.UninstallCommand;
import org.objectweb.proactive.examples.jmx.remote.management.command.osgi.UpdateCommand;
import org.objectweb.proactive.examples.jmx.remote.management.exceptions.InvalidTransactionException;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.IJmx;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.notifications.BundleStartedNotification;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.notifications.BundleStoppedNotification;
import org.objectweb.proactive.examples.jmx.remote.management.osgi.OSGiStore;
import org.objectweb.proactive.examples.jmx.remote.management.status.Status;
import org.objectweb.proactive.examples.jmx.remote.management.transactions.Transaction;
import org.objectweb.proactive.examples.jmx.remote.management.transactions.TransactionsManager;
import org.objectweb.proactive.examples.jmx.remote.management.utils.MethodRepresentation;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;


public class BundleInfo extends NotificationBroadcasterSupport implements BundleListener, DynamicMBean,
        Serializable, IJmx {
    public static final int INSTALLED = Bundle.INSTALLED;
    public static final int ACTIVE = Bundle.ACTIVE;
    public static final int RESOLVED = Bundle.RESOLVED;
    public static final int STARTING = Bundle.STARTING;
    public static final int STOPPING = Bundle.STOPPING;
    public static final int UNINSTALLED = Bundle.UNINSTALLED;
    private static final MethodRepresentation[] JMX_OPERATIONS = new MethodRepresentation[] {
            new MethodRepresentation("start", new String[] { Long.class.getName() }, "Starts the bundle"),
            new MethodRepresentation("stop", new String[] { Long.class.getName() }, "Stops the bundle"),
            new MethodRepresentation("update", new String[] { Long.class.getName() }, "Updates the bundle"),
            new MethodRepresentation("uninstall", new String[] { Long.class.getName() },
                "Uninstall  the bundle"), };

    /* JMX attributes */
    private HashMap<String, Object> headers;
    private OSGiFrameworkMBean parent;
    private transient ObjectName on;
    private transient Bundle bundle;
    private String path;
    private String name;
    private String location;

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    public BundleInfo() {
    }

    private long id;
    private int state;
    private String urlGateway;

    public BundleInfo(OSGiFramework parent, Bundle bundle) {
        this.bundle = bundle;
        this.urlGateway = parent.getUrl();

        copyBundleInfo();
        try {
            this.path = parent.getPath() + "type=bundles";

            this.on = new ObjectName(this.path + ",url=" + this.urlGateway + '(' + parent.getPort() + ')' +
                ",name=" + this.name);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        this.parent = parent;
        OSGiStore.getInstance().getContext().addBundleListener(this);
        getProvidedServices();
        getServicesInUse();
    }

    private void copyBundleInfo() {
        this.headers = new HashMap<String, Object>();
        this.id = bundle.getBundleId();
        this.state = bundle.getState();

        /* headers copy ... */
        Dictionary hTmp = bundle.getHeaders();
        Enumeration<String> e = hTmp.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            Object value = hTmp.get(key);
            this.headers.put(key, value);
        }
        this.headers.put("id", bundle.getBundleId());
        this.headers.put("state", bundle.getState());
        this.headers.put("location", bundle.getLocation());
        this.name = (String) this.headers.get("Bundle-Name");
        this.location = bundle.getLocation();
    }

    /* JMX Operations */
    public Status start(Long idTransaction) {
        try {
            Transaction t = TransactionsManager.getInstance().getTransaction(idTransaction.longValue());
            CommandMBean c = new StartCommand(t, this.id);
            return t.executeCommand(c);
        } catch (InvalidTransactionException e) {
            return new Status(Status.ERR, OSGiCommand.START + id, e.getMessage(), OSGiStore.getInstance()
                    .getUrl());
        }
    }

    public Status stop(Long idTransaction) {
        try {
            Transaction t = TransactionsManager.getInstance().getTransaction(idTransaction.longValue());
            CommandMBean c = new StopCommand(t, this.id);
            return t.executeCommand(c);
        } catch (InvalidTransactionException e) {
            return new Status(Status.ERR, OSGiCommand.STOP + id, e.getMessage(), OSGiStore.getInstance()
                    .getUrl());
        }
    }

    public Status update(Long idTransaction) {
        try {
            Transaction t = TransactionsManager.getInstance().getTransaction(idTransaction.longValue());
            CommandMBean c = new UpdateCommand(t, this.id);
            return t.executeCommand(c);
        } catch (InvalidTransactionException e) {
            return new Status(Status.ERR, OSGiCommand.UPDATE + id, e.getMessage(), OSGiStore.getInstance()
                    .getUrl());
        }
    }

    public Status uninstall(Long idTransaction) {
        try {
            Transaction t = TransactionsManager.getInstance().getTransaction(idTransaction.longValue());
            CommandMBean c = new UninstallCommand(t, this.id);
            return t.executeCommand(c);
        } catch (InvalidTransactionException e) {
            return new Status(Status.ERR, OSGiCommand.UNINSTALL + id, e.getMessage(), OSGiStore.getInstance()
                    .getUrl());
        }
    }

    /* ServiceInfos creations */
    private void getProvidedServices() {
        if (this.state != UNINSTALLED) {
            ServiceReference[] servicesReferences = this.bundle.getRegisteredServices();
            if (servicesReferences != null) {
                for (int i = 0; i < servicesReferences.length; i++) {
                    ServiceReference sr = servicesReferences[i];
                    ServiceInfo si = new ServiceInfo(this, sr);
                }
            }
        }
    }

    private void getServicesInUse() {
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /* IJmx implementation */
    public void register() throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException {
        ManagementFactory.getPlatformMBeanServer().registerMBean(this, this.on);
    }

    public void unregister() throws InstanceNotFoundException, MBeanRegistrationException {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.on);
    }

    /* implementation of Dynamic MBean */
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        return this.headers.get(attribute).toString();
    }

    public AttributeList getAttributes(String[] attributes) {
        AttributeList al = new AttributeList(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            al.add(new Attribute(attributes[i], this.headers.get(attributes[i]).toString()));
        }
        return al;
    }

    private MBeanAttributeInfo[] getAttributesInfos() {
        MBeanAttributeInfo[] attInfos = new MBeanAttributeInfo[this.headers.size()];
        Iterator i = this.headers.keySet().iterator();
        int j = 0;
        while (i.hasNext()) {
            String attribute = (String) i.next();
            MBeanAttributeInfo info = new MBeanAttributeInfo(attribute, this.headers.get(attribute)
                    .getClass().getName(), attribute, true, false, false);
            attInfos[j++] = info;
        }
        return attInfos;
    }

    private MBeanOperationInfo[] getOperationsInfos() {
        MBeanOperationInfo[] mbo = new MBeanOperationInfo[JMX_OPERATIONS.length];
        try {
            for (int i = 0; i < JMX_OPERATIONS.length; i++) {
                MethodRepresentation mr = JMX_OPERATIONS[i];
                MBeanOperationInfo mboi = new MBeanOperationInfo(mr.getDescription(), this.getClass()
                        .getMethod(mr.getName(), mr.getParamsTypes()));
                mbo[i] = mboi;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return mbo;
    }

    private MBeanNotificationInfo[] getNotificationsInfo() {
        MBeanNotificationInfo[] mbnis = new MBeanNotificationInfo[] { new MBeanNotificationInfo(new String[] {
                "name=ATTRIBUTE_CHANGE", "descriptorType=notification", "log=T", "severity=5",
                "displayName=jmx.attribute.change" }, "BundleNotification",
            "Emitted when a bundle event occurs") };
        return mbnis;
    }

    public MBeanInfo getMBeanInfo() {
        MBeanAttributeInfo[] attributes = getAttributesInfos();
        MBeanConstructorInfo[] constructors = new MBeanConstructorInfo[] {};
        MBeanOperationInfo[] operations = getOperationsInfos();
        MBeanNotificationInfo[] notifications = getNotificationsInfo();
        MBeanInfo mbi = new MBeanInfo(this.getClass().getName(), "Mbean matching an OSGi service",
            attributes, constructors, operations, notifications);
        return mbi;
    }

    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException,
            ReflectionException {
        try {
            Class<?>[] paramTypes = new Class[signature.length];
            for (int i = 0; i < signature.length; i++) {
                paramTypes[i] = Class.forName(signature[i]);
            }
            Method m = this.getClass().getMethod(actionName, paramTypes);
            return m.invoke(this, params);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException {
    }

    public AttributeList setAttributes(AttributeList attributes) {
        return null;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    private static long seqNumber = 0;

    /**
     *
     */
    public void bundleChanged(BundleEvent event) {
        if (event.getBundle().equals(this.bundle)) {
            copyBundleInfo();
            if (event.getType() == BundleEvent.STARTED) {
                Notification notification = new BundleStartedNotification("osgi.started.notification", this,
                    seqNumber++, "Bundle started", this.urlGateway, this.on);
                sendNotification(notification);
            } else if (event.getType() == BundleEvent.STOPPED) {
                Notification notification = new BundleStoppedNotification("osgi.stopped.notification", this,
                    seqNumber++, "Bundle stopped", this.urlGateway, this.on);
                sendNotification(notification);
            } else if (event.getType() == BundleEvent.UPDATED) {
                Notification notification = new BundleStartedNotification("osgi.updated.notification", this,
                    seqNumber++, "Bundle updated", this.urlGateway, this.on);
                sendNotification(notification);
            }
        }
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the state
     */
    public int getState() {
        return state;
    }

    public String getUrlGateway() {
        return urlGateway;
    }

    public boolean equals(BundleInfo b) {
        return this.location.equals(b.getLocation());
    }

    @Override
    public String toString() {
        return this.name;
    }
}
