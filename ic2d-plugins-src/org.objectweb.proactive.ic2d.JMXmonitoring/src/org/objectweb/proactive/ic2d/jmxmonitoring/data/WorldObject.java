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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MalformedObjectNameException;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;


/**
 * Holder class for all monitored hosts and virtual nodes
 * @author The ProActive Team
 */
public class WorldObject extends AbstractData {
    // -------------------------------------------
    // --- Constants -----------------------------
    // -------------------------------------------
    public static boolean HIDE_P2PNODE_MONITORING = true;
    public static boolean DEFAULT_ENABLE_AUTO_RESET = false;
    public final static String ADD_VN_MESSAGE = "Add a virtual node";
    public final static String REMOVE_VN_MESSAGE = "Remove a virtual node";

    // 60 s
    public static int MAX_AUTO_RESET_TIME = 60;

    // 1 s
    public static int MIN_AUTO_RESET_TIME = 1;

    // 7 s
    public static int DEFAULT_AUTO_RESET_TIME = 7;
    private static int DEFAULT_MAX_DEPTH = 3;

    public enum methodName {
        ADD_CHILD, REMOVE_CHILD, RESET_COMMUNICATIONS;
    };

    // -------------------------------------------
    // --- Variables -----------------------------
    // -------------------------------------------
    private int currentAutoResetTime = DEFAULT_AUTO_RESET_TIME;
    private boolean enableAutoReset = DEFAULT_ENABLE_AUTO_RESET;
    private String name;
    private boolean enableMonitoring = true;

    //private static Logger logger = ProActiveLogger.getLogger(Loggers.JMX);

    /** Contains all virtual nodes. */
    private Map<String, VirtualNodeObject> vnChildren;

    /**
     * A map of all known active objects.
     * This map is used to have good performances.
     */
    private Map<UniqueID, ActiveObject> activeObjects;
    private Map<UniqueID, ActiveObject> migrations;
    private int maxDepth = DEFAULT_MAX_DEPTH;

    /**
     * Thread
     */
    private MonitorThread monitorThread;
    private JMXNotificationManager notificationManager;
    private boolean hideP2P = HIDE_P2PNODE_MONITORING;

    // -------------------------------------------
    // --- Constructor ---------------------------
    // -------------------------------------------

    /**
     * Create a new WorldObject
     * @param connection A ProActiveConnection
     */
    public WorldObject() {
        super(null);
        this.activeObjects = new ConcurrentHashMap<UniqueID, ActiveObject>();
        this.migrations = new ConcurrentHashMap<UniqueID, ActiveObject>();
        this.vnChildren = new ConcurrentHashMap<String, VirtualNodeObject>();

        // Record the model
        this.name = ModelRecorder.getInstance().addModel(this);

        // Adds a MonitorTread refresher
        monitorThread = new MonitorThread(this);
        addObserver(monitorThread);

        // Creates a notification manager
        notificationManager = JMXNotificationManager.getInstance();
    }

    // -------------------------------------------
    // --- Methods -------------------------------
    // -------------------------------------------

    /**
     * Add a host to the WorldObject
     * @param url The url of the host.
     * @param rank The rank of the depth.(0 if the user want ot monitor this host,
     * 1,2,3...if this host was discovered.)
     */
    public void addHost(String url, int rank) {
        try {
            addChild(new HostObject(this, url, rank));
            //TODO emil: I have removed this line 
            //check if it was here with a purpuse
            //notifyObservers();
        } catch (MalformedObjectNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setChanged();

        //this notification will be handled by the MonitorThread only for 
        //the first child added. It will start a refreshing thread
        if (getMonitoredChildrenSize() == 1) {
            notifyObservers(new MVCNotification(MVCNotificationTag.WORLD_OBJECT_FIRST_CHILD_ADDED));
        }

        // notifyObservers(new Notification(MVCNotifications.ADD_CHILD));
    }

    /**
     * Add a host to the WorldObject
     * @param url The url of the host.
     */
    public void addHost(String url) {
        this.addHost(url, 0);
    }

    @Override
    public void removeChild(AbstractData child) {
        super.removeChild(child);
        setChanged();
        //this notification will be handled by the MonitorThread only for 
        //the last child added. It will stop a refreshing thread
        if (getMonitoredChildrenSize() == 0) {
            notifyObservers(new MVCNotification(MVCNotificationTag.WORLD_OBJECT_LAST_CHILD_REMOVED));
        }
        notifyObservers(new MVCNotification(MVCNotificationTag.REMOVE_CHILD));
    }

    /**
     * Find an active object in the map of the known active objects.
     * @param id The Unique id of the active object.
     * @return an active object.
     */
    public ActiveObject findActiveObject(UniqueID id) {
        return this.activeObjects.get(id);
    }

    /**
     * Records a new active object in the map of the known active objects.
     * @param ao The active object to add.
     */
    public void addActiveObject(ActiveObject ao) {
        //Note: until now, this method is only called in the constructor of an (ic2d) ActiveObject
        synchronized (activeObjects) {
            synchronized (migrations) {
                ActiveObject oldAO = this.activeObjects.get(ao.getUniqueID());
                if (oldAO != null) {
                    // It's maybe a migration
                    // If the url of the two active objects are different, this is a migration
                    if (!ao.getParent().getUrl().equals(oldAO.getParent().getUrl())) {
                        migrations.put(ao.getUniqueID(), oldAO);
                    }
                }
                this.activeObjects.put(ao.getUniqueID(), ao);
            } //synchronized (migrations)
        } //  synchronized (activeObjects) 
    }

    /**
     * Removes an active object in the map of the known active objects.
     * @param ao The active object to remove.
     */
    public void removeActiveObject(UniqueID id) {
        ActiveObject ao = null;
        synchronized (migrations) {
            ao = migrations.remove(id);
        }
        if (ao == null) {
            synchronized (activeObjects) {
                ao = this.activeObjects.remove(id);
            }
        }

        if (ao == null) {
            System.out.println("Represntation of Active Object " + id + " already removed.");
        } else {
            System.out.println("Stop monitoring " + ao + ", on " + ao.getParent());
            ao.resetCommunications();
            ao.getParent().removeChild(ao);
        }
    }

    @Override
    public <T extends AbstractData> T getParent() {
        return null;
    }

    @Override
    public void explore() {
        super.exploreEachChild();
    }

    @Override
    public WorldObject getWorldObject() {
        return this;
    }

    @Override
    public String getKey() {
        return this.name;
    }

    @Override
    public String getType() {
        return "world object";
    }

    @Override
    public int getHostRank() {
        return 0;
    }

    @Override
    public int getDepth() {
        return this.maxDepth;
    }

    /**
     * Changes the max depth.
     * @param depth The new max depth.
     */
    public void setDepth(int depth) {
        this.maxDepth = depth;
    }

    public MonitorThread getMonitorThread() {
        return this.monitorThread;
    }

    public void addMigration(ActiveObject ao) {
        this.migrations.put(ao.getUniqueID(), ao);
    }

    public JMXNotificationManager getNotificationManager() {
        return this.notificationManager;
    }

    ////////// NEW DATA

    /**
     * Returns the name of this world.
     * @return The name of this world.
     */
    public String getName() {
        return name;
    }

    /**
     * Enables the auto reset action
     * @param enable
     */
    public void setEnableAutoResetTime(boolean enable) {
        enableAutoReset = enable;
    }

    /**
     * Returns true if the auto reset time is enabled, false otherwise
     * @return true if the auto reset time is enabled, false otherwise
     */
    public boolean enableAutoResetTime() {
        return enableAutoReset;
    }

    public void enableMonitoring(boolean enable) {
        this.enableMonitoring = enable;
    }

    public boolean getEnableMonitoring() {
        return this.enableMonitoring;
    }

    /**
     * Change the current auto reset time
     * @param time The new time
     */
    public void setAutoResetTime(int time) {
        currentAutoResetTime = time;
    }

    /**
     * Returns the current auto reset time
     * @return The current auto reset time
     */
    public int getAutoResetTime() {
        return this.currentAutoResetTime;
    }

    /**
     * Add a virtual node to this object
     * @param vn
     */
    protected void addVirtualNode(VirtualNodeObject vn) {
        vnChildren.put(vn.getKey(), vn);
        setChanged();
        Hashtable<String, VirtualNodeObject> data = new Hashtable<String, VirtualNodeObject>();
        data.put(ADD_VN_MESSAGE, vn);
        //VirtualNodesGroup object will use the information within data
        notifyObservers(new MVCNotification(MVCNotificationTag.WORLD_OBJECT_ADD_VIRTUAL_NODE, data));
    }

    /**
     * Remove a virtual node to this object
     * @param vn
     */
    protected void removeVirtualNode(VirtualNodeObject vn) {
        vnChildren.remove(vn.getKey());
        setChanged();
        Hashtable<String, VirtualNodeObject> data = new Hashtable<String, VirtualNodeObject>();
        data.put(REMOVE_VN_MESSAGE, vn);
        notifyObservers(new MVCNotification(MVCNotificationTag.WORLD_OBJECT_REMOVE_VIRTUAL_NODE, data));
    }

    public VirtualNodeObject getVirtualNode(String virtualNodeName) {
        return vnChildren.get(virtualNodeName);
    }

    public List<VirtualNodeObject> getVNChildren() {
        return new ArrayList<VirtualNodeObject>(this.vnChildren.values());
    }

    /**
     * Use to hide or nor the p2p objects.
     * @param hide true for hide the p2p object, false otherwise
     */
    public void hideP2P(boolean hide) {
        this.hideP2P = hide;
        getMonitorThread().forceRefresh();
    }

    /**
     * Return true if the p2p objects ars hidden, false otherwise
     * @return true if the p2p objects ars hidden, false otherwise
     */
    public boolean isP2PHidden() {
        return this.hideP2P;
    }
}
