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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.objectweb.proactive.core.rmi.ClassServer;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.examples.jmx.remote.management.command.osgi.InstallCommand;
import org.objectweb.proactive.examples.jmx.remote.management.command.osgi.OSGiCommand;
import org.objectweb.proactive.examples.jmx.remote.management.exceptions.InvalidTransactionException;
import org.objectweb.proactive.examples.jmx.remote.management.exceptions.JMXException;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.IJmx;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.notifications.BundleAddedNotification;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.notifications.BundleUninstalledNotification;
import org.objectweb.proactive.examples.jmx.remote.management.osgi.OSGiStore;
import org.objectweb.proactive.examples.jmx.remote.management.status.Status;
import org.objectweb.proactive.examples.jmx.remote.management.transactions.Transaction;
import org.objectweb.proactive.examples.jmx.remote.management.transactions.TransactionsManager;
import org.objectweb.proactive.examples.jmx.remote.management.utils.Constants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.ungoverned.osgi.service.shell.ShellService;


public class OSGiFramework extends NotificationBroadcasterSupport
    implements OSGiFrameworkMBean, FrameworkListener, BundleListener, IJmx,
        Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6871230015619578483L;
    private String path;
    private transient BundleContext context;
    private transient ObjectName on;
    private static int seqNumber = 0;

    /* JMX MBean attributes */
    private String url;
    private String processor;
    private String osName;
    private String exeEnv;
    private String language;
    private String vendor;
    private String version;
    private ArrayList<BundleInfo> bundles;
    private transient ShellService shell;
    private transient TransactionsManager transactionsManager;
    private int port;

    public OSGiFramework(BundleContext context)
        throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException {
        try {
            this.context = context;
            OSGiStore.getInstance().setContext(this.context);
            this.url = ProActiveInet.getInstance().getInetAddress()
                                    .getCanonicalHostName();
            this.port = ClassServer.getServerSocketPort();
            OSGiStore.getInstance().setUrl(url);
            UrlMBean urlMbean = new Url(this.url + '(' + this.port + ')');
            this.transactionsManager = TransactionsManager.getInstance(this.url);

            this.path = Constants.OSGI_JMX_PATH;
            this.on = new ObjectName(this.path + Constants.ON_GATEWAYS +
                    this.url + '(' + this.port + ')');

            this.context.addBundleListener(this);
            buildBundleList();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (JMXException e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return this.url;
    }

    /**
    * @return the bundles
    */
    public ArrayList<BundleInfo> getBundles() {
        return bundles;
    }

    /**
     * @param bundles the bundles to set
     */
    public void setBundles(ArrayList<BundleInfo> bundlesList) {
        this.bundles = bundlesList;
    }

    public String getVersion() {
        return this.context.getProperty("org.osgi.framework.version");
    }

    public int getPort() {
        return port;
    }

    /**
    *
    */
    public String getVendor() {
        return this.context.getProperty("org.osgi.framework.vendor");
    }

    /**
     *
     */
    public String getLanguage() {
        String l = this.context.getProperty("org.osgi.framework.language");
        return l;
    }

    /**
     *
     */
    public String getOsName() {
        return this.context.getProperty("org.osgi.framework.os.name");
    }

    /**
     *
     */
    public String getProcessor() {
        return this.context.getProperty("org.osgi.framework.processor");
    }

    /**
     *
     */
    public String getExeEnv() {
        return System.getProperty("java.version");
    }

    /**
     *
     */
    public String getProfile() {
        return this.context.getProperty("oscar.cache.profile");
    }

    private void buildBundleList()
        throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException, JMXException {
        this.bundles = new ArrayList<BundleInfo>();
        Bundle[] bundles = this.context.getBundles();
        for (int i = 0; i < bundles.length; i++) {
            Bundle bundle = bundles[i];
            BundleInfo b = new BundleInfo(this, bundle);
            ((IJmx) b).register();
            this.bundles.add(b);
            OSGiStore.getInstance().registerBundle(b.getLocation(), b);
        }
    }

    public Status installBundle(long transactionId, String location) {
        Transaction t;
        try {
            t = TransactionsManager.getInstance().getTransaction(transactionId);
            return t.executeCommand(new InstallCommand(t, location,
                    OSGiCommand.INSTALL));
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
            return new Status(Status.ERR, OSGiCommand.INSTALL + location,
                e.getMessage(), OSGiStore.getInstance().getUrl());
        }
    }

    private void addBundle(Bundle bundle) throws JMXException {
        BundleInfo b = new BundleInfo(this, bundle);
        try {
            ((IJmx) b).register();
        } catch (InstanceAlreadyExistsException e) {
            throw new JMXException(e);
        } catch (MBeanRegistrationException e) {
            throw new JMXException(e);
        } catch (NotCompliantMBeanException e) {
            throw new JMXException(e);
        }
        this.bundles.add(b);
        OSGiStore.getInstance().registerBundle(b.getLocation(), b);

        Notification notification = new BundleAddedNotification("osgi.added.notification",
                b, seqNumber++, "A bundle has been added", this.url, this.on);
        sendNotification(notification);
    }

    private void removeBundle(Bundle b) {
        BundleInfo bInfo = new BundleInfo(this, b);
        for (BundleInfo info : this.bundles) {
            if (info.equals(bInfo)) {
                this.bundles.remove(info);
                break;
            }
        }
        Notification notification = new BundleUninstalledNotification("osgi.uninstalled.notification",
                bInfo, seqNumber++, "Bundle uninstalled", this.url, this.on);
        sendNotification(notification);
    }

    public void frameworkEvent(FrameworkEvent event) {
        if (event.getType() == FrameworkEvent.ERROR) {
        } else if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
        } else if (event.getType() == FrameworkEvent.STARTED) {
        } else if (event.getType() == FrameworkEvent.STARTLEVEL_CHANGED) {
        }
    }

    public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.INSTALLED) {
            try {
                addBundle((Bundle) event.getSource());
            } catch (JMXException e) {
                e.printStackTrace();
            }
        } else if (event.getType() == BundleEvent.UNINSTALLED) {
            removeBundle((Bundle) event.getSource());
        }
    }

    /* JMX operations */
    public void register()
        throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException {
        ManagementFactory.getPlatformMBeanServer().registerMBean(this, this.on);
    }

    public void unregister()
        throws InstanceNotFoundException, MBeanRegistrationException,
            JMXException {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.on);
        Iterator<BundleInfo> i = this.bundles.iterator();
        while (i.hasNext()) {
            ((IJmx) i.next()).unregister();
        }
    }

    public void executeCommand(long transactionId, String command) {
        //			Transaction t = this.transactionsManager.getTransaction(transactionId);
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }
}
