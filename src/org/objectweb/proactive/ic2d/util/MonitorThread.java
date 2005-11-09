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
package org.objectweb.proactive.ic2d.util;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Iterator;

import javax.swing.DefaultListModel;

import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.data.WorldObject;
import org.objectweb.proactive.ic2d.gui.jobmonitor.NodeExploration;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.BasicMonitoredObject;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataAssociation;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredHost;


/**
 * @author ProActiveTeam
 * @version 1.0, July 2005
 * @since ProActive 2.2
 *
 * Model based on job monitoring refresher
 *
 */
public class MonitorThread {
    private static String depth;
    private WorldObject worldObject;
    private IC2DMessageLogger logger;
    private DataAssociation asso;
    private DefaultListModel monitoredHosts;
    private DefaultListModel skippedObjects;
    private Thread refresher;
    private static final int DEFAULT_RMI_PORT = Registry.REGISTRY_PORT;
    private NodeExploration explorator;
    private static long ttr;
    private volatile boolean refresh = true;

    public MonitorThread(String _depth, WorldObject worldObject,
        IC2DMessageLogger logger) {
        this.asso = new DataAssociation();
        depth = _depth;
        this.logger = logger;
        this.worldObject = worldObject;

        createRefresher();

        monitoredHosts = new DefaultListModel();
        skippedObjects = new DefaultListModel();
        //skippedObjects.addElement(new MonitoredJob(ProActive.getJobId()));
        ttr = 30;
        explorator = new NodeExploration(asso, skippedObjects, logger);

        String hostname = null;
        int port = 0;

        explorator.setMaxDepth(depth);

        Iterator it = asso.getHosts().iterator(); // iterator de MonitoredHost
    }

    private void createRefresher() {
        refresh = true;
        refresher = new Thread(new Runnable() {
                    public void run() {
                        //				System.out.println ("Start of refresher thread");
                        while (refresh) {
                            try {
                                //						System.out.println ("Waiting for refresh - ttr = " + ttr + " seconds");
                                //						System.out.println ("Automatic refresh starting");
                                handleHosts();
                                Thread.sleep(ttr * 1000);
                            } catch (InterruptedException e) {
                                //						e.printStackTrace();
                            }
                        }

                        //				System.out.println ("Stop of refresher thread");
                    }
                });
    }

    protected void finalize() throws Throwable {
        stopRefreshing();
        super.finalize();
    }

    private void stopRefreshing() {
        if (refresh) {
            //			System.out.println ("Stoppping refresher thread");
            refresh = false;
            refresher.interrupt();
        }
    }

    /**
     * return the time to refresh
     * @return ttr
     */
    public static long getTtr() {
        return ttr;
    }

    /**
     * set the depth
     * @param _depth new depth
     */
    public static void setDepth(String _depth) {
        depth = _depth;
    }

    /**
     * set the time to refresh
     * @param _ttr new ttr
     */
    public static void setTtr(long _ttr) {
        ttr = _ttr;
    }

    /**
     * change the time to refresh and restart the refresher.
     * @param _ttr
     */
    public void changeTtr(long _ttr) {
        ttr = _ttr;
        if (refresh) {
            refresher.interrupt();
        } else {
            createRefresher();
        }
    }

    /**
     * add a host to monitor
     * @param host full hostname (host + port)
     * @param protocol
     */
    public void addMonitoredHost(String host, String protocol) {
        String hostname;
        int port;
        if (host != null) {
            hostname = UrlBuilder.removePortFromHost(host);
            port = UrlBuilder.getPortFromUrl(host);
            MonitoredHost hostObject = new MonitoredHost(hostname, port,
                    protocol);
            addMonitoredHost(hostObject);
        }
    }

    /**
     * add a host to monitor, represented by the host name, a port and a protocol
     * @param host hostname
     * @param port
     * @param protocol
     */
    public void addMonitoredHost(String host, int port, String protocol) {
        MonitoredHost hostObject = new MonitoredHost(host, port, protocol);
        addMonitoredHost(hostObject);
    }

    /**
     * add a monitored host
     * @param hostObject monitored host
     */
    public void addMonitoredHost(MonitoredHost hostObject) {
        skippedObjects.removeElement(hostObject);
        if (!monitoredHosts.contains(hostObject)) {
            monitoredHosts.addElement(hostObject);
            if (monitoredHosts.size() == 1) {
                refresher.start();
            }
        }

        //		System.out.println ("There are now " + monitoredHosts.size() + " monitored hosts");
    }

    /**
     * remove a monitored host from the list
     * @param hostObject
     */
    public void removeMonitoredHost(MonitoredHost hostObject) {
        monitoredHosts.removeElement(hostObject);
        if (monitoredHosts.size() == 0) {
            stopRefreshing();
        }
    }

    public void removeAsso(BasicMonitoredObject object) {
        asso.removeItem(object);
    }

    /**
     * add an monitored object to the skip objects list
     * @param object monitored object to skip
     */
    public void addObjectToSkip(BasicMonitoredObject object) {
        skippedObjects.addElement(object);
    }

    /**
     * remove a skipped object from the skipped objects list
     * @param object
     */
    public void removeObjectToSkip(BasicMonitoredObject object) {
        skippedObjects.removeElement(object);
    }

    public DefaultListModel getSkippedObjects(int key) {
        DefaultListModel list = new DefaultListModel();
        for (int i = 0, size = skippedObjects.getSize(); i < size; i++) {
            BasicMonitoredObject o = (BasicMonitoredObject) skippedObjects.get(i);
            if (o.getKey() == key) {
                list.addElement(o);
            }
        }

        return list;
    }

    public void updateHosts() {
        new Thread(new Runnable() {
                public void run() {
                    handleHosts();
                }
            }).start();
    }

    private void handleHosts() {
        synchronized (monitoredHosts) {
            asso.clear();
            explorator.setMaxDepth(depth);
            explorator.startExploration();

            for (int i = 0, size = monitoredHosts.size(); i < size; ++i) {
                MonitoredHost hostObject = (MonitoredHost) monitoredHosts.get(i);
                String host = hostObject.getFullName();
                String protocol = hostObject.getMonitorProtocol();
                handleHost(host, protocol);
            }

            explorator.exploreKnownJVM();
            explorator.endExploration();
            Iterator it = asso.getHosts().iterator(); // iterator de MonitoredHost
            while (it.hasNext()) {
                MonitoredHost monitoredhost = (MonitoredHost) it.next();

                //                String tmphost = monitoredhost.getFullName();
                try {
                    worldObject.addHostObject(monitoredhost,
                        asso.getValues(monitoredhost, 2, null));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updateHost(final BasicMonitoredObject hostObject) {
        new Thread(new Runnable() {
                public void run() {
                    asso.deleteItem(hostObject);
                    explorator.startExploration();
                    handleHost(hostObject.getFullName(),
                        ((MonitoredHost) hostObject).getMonitorProtocol());
                    explorator.endExploration();
                }
            }).start();
    }

    private void handleHost(String host, String protocol) {
        String hostname = host;
        int port = DEFAULT_RMI_PORT;
        int pos = host.lastIndexOf(":");
        if (pos != -1) {
            // if the hostname is host:port
            try {
                port = Integer.parseInt(host.substring(1 + pos));
            } catch (NumberFormatException e) {
                port = DEFAULT_RMI_PORT;
            }

            hostname = host.substring(0, pos);
        }

        explorator.exploreHost(hostname, port, protocol);
    }
}
