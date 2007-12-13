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
package org.objectweb.proactive.ic2d.jmxmonitoring.data;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.ic2d.jmxmonitoring.finder.RemoteObjectHostRTFinder;
import org.objectweb.proactive.ic2d.jmxmonitoring.finder.RuntimeFinder;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;


/**
 * Holder class for the host data representation.
 */
public class HostObject extends AbstractData {
    private WorldObject parent;
    private String url;
    private String hostName;
    private int port;
    private String protocol;
    private int rank;
    private final static String DEFAULT_NAME = "undefined";

    //Warning: Don't use this variavle directly, use getOSName().
    private String osName;

    //Warning: Don't use this vaiable directly, use getOSVersion().
    private String osVersion;

    /**
     * Creates a new HostObject. Use the method addHost(String url) of the WorldObject class instead.
     * @param url du host
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     */
    protected HostObject(WorldObject parent, String url, int rank) throws MalformedObjectNameException,
            NullPointerException {
        super(new ObjectName(FactoryName.HOST));

        this.hostName = URIBuilder.getHostNameFromUrl(url);
        String name = URIBuilder.getNameFromURI(url);
        this.protocol = URIBuilder.getProtocol(url);
        this.port = URIBuilder.getPortNumber(url);

        this.url = URIBuilder.buildURI(hostName, name, protocol, port).toString();

        this.rank = rank;

        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    public WorldObject getParent() {
        return this.parent;
    }

    @Override
    public String getKey() {
        return this.url;
    }

    @Override
    public String getType() {
        return "host";
    }

    @Override
    public String getName() {
        return getHostName();
    }

    /**
     * Returns the url of this object.
     * @return An url.
     */
    public String getUrl() {
        return this.url;
    }

    public String getHostName() {
        return this.hostName;
    }

    public int getPort() {
        return this.port;
    }

    public String getProtocol() {
        return this.protocol;
    }

    /**
     * Returns the operating system name.
     * @return The operating system name.
     */
    public String getOSName() {
        if (this.osName == null) {
            return DEFAULT_NAME;
        } else {
            return this.osName;
        }
    }

    /**
     * Returns the operating sytem version.
     * @return The operating sytem version.
     */
    public String getOSVersion() {
        if (osVersion == null) {
            return DEFAULT_NAME;
        } else {
            return this.osVersion;
        }
    }

    @Override
    public void explore() {
        System.out.println(this);
        refreshRuntimes();
    }

    @Override
    public int getHostRank() {
        return this.rank;
    }

    @Override
    public String toString() {
        String result = this.hostName + ":" + this.port;
        if (!getOSName().equals(DEFAULT_NAME)) {
            result += (":" + getOSName());
        }
        if (!getOSVersion().equals(DEFAULT_NAME)) {
            result += ("(OS version: " + getOSVersion() + ")");
        }
        return result;
    }

    /**
     * Propose à l'host de s'ajouter s'il y arrive le fils donné en paramètre.
     */
    public void proposeChild() {
    }

    /**
     * Updates the set of IC2D's RuntimeObjects so that it is in sync with the ProActive Runtimes on the monitored Host.
     * The update is performed by comparing the existing RuntimeObjects with the set of ProActive Runtimes
     * returned from <code> RemoteObjectHostRTFinder.getRuntimeObjects(HostObject host) </code>
     *
     */
    @SuppressWarnings("unchecked")
    private void refreshRuntimes() {
        RuntimeFinder rfinder = new RemoteObjectHostRTFinder();
        Collection<RuntimeObject> runtimeObjects = rfinder.getRuntimeObjects(this);

        Map<String, AbstractData> childrenToRemoved = this.getMonitoredChildrenAsMap();

        Iterator<RuntimeObject> it = runtimeObjects.iterator();
        while (it.hasNext()) {
            RuntimeObject runtimeObject = it.next();

            // If this child is a NOT monitored child.
            if (containsChildInNOTMonitoredChildren(runtimeObject.getKey())) {
                continue;
            }

            RuntimeObject child = (RuntimeObject) this.getMonitoredChild(runtimeObject.getKey());

            // If this child is not yet monitored.
            if (child == null) {
                child = runtimeObject;
                addChild(runtimeObject);
                updateOSNameAndVersion(runtimeObject.getConnection());
            }
            // This child is already monitored, but this child maybe contains some not monitord objects.
            child.explore();

            // Removes from the model the not monitored or termined runtimes.
            childrenToRemoved.remove(child.getKey());
        }

        // Some child have to be removed
        for (Iterator<AbstractData> iter = childrenToRemoved.values().iterator(); iter.hasNext();) {
            RuntimeObject child = (RuntimeObject) iter.next();
            child.destroy();
        }
    }

    private void updateOSNameAndVersion(ProActiveConnection connection) {
        if ((this.osName == null) || (this.osVersion == null)) {
            ObjectName OSoname = null;
            try {
                OSoname = new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
            } catch (MalformedObjectNameException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NullPointerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            OperatingSystemMXBean proxyMXBean = (OperatingSystemMXBean) MBeanServerInvocationHandler
                    .newProxyInstance(connection, OSoname, OperatingSystemMXBean.class, false);
            this.osName = proxyMXBean.getName();
            this.osVersion = proxyMXBean.getVersion();

            setChanged();
            notifyObservers(new MVCNotification(MVCNotificationTag.HOST_OBJECT_UPDATED_OSNAME_AND_VERSON,
                this.toString()));
        }
    }

    @Override
    public ProActiveConnection getConnection() {
        // A host object has no JMX ProActiveConnection
        return null;
    }

    @Override
    public synchronized void addChild(AbstractData child) {
        if (child instanceof RuntimeObject) {
            RuntimeObject runtimeObject = (RuntimeObject) child;
            ObjectName oname = runtimeObject.getObjectName();
            JMXNotificationManager.getInstance().subscribe(oname, runtimeObject.getListener(),
                    runtimeObject.getUrl());
        }

        super.addChild(child);
    }
}
