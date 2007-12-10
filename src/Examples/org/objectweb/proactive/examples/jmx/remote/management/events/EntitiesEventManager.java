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
package org.objectweb.proactive.examples.jmx.remote.management.events;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

import javax.management.Notification;
import javax.management.ObjectName;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.jmx.remote.management.client.entities.ManageableEntity;
import org.objectweb.proactive.examples.jmx.remote.management.client.entities.RemoteBundle;
import org.objectweb.proactive.examples.jmx.remote.management.client.entities.RemoteCommand;
import org.objectweb.proactive.examples.jmx.remote.management.client.entities.RemoteGateway;
import org.objectweb.proactive.examples.jmx.remote.management.client.entities.RemoteTransaction;
import org.objectweb.proactive.examples.jmx.remote.management.client.entities.RemoteTransactionManager;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.notifications.BundleNotification;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.notifications.TransactionCancelledNotification;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.notifications.TransactionCommandNotification;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.notifications.TransactionCommitedNotification;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.notifications.TransactionNotification;
import org.objectweb.proactive.examples.jmx.remote.management.mbean.BundleInfo;


/**
 *
 * @author vlegrand
 *
 */
public class EntitiesEventManager implements Serializable {

    /**
         *
         */
    private static final long serialVersionUID = 6371894154663904329L;
    private static transient EntitiesEventManager instance;
    public static final String GATEWAY_ADDED = "Gateway added";
    public static final String GATEWAY_CONNECTED = "Gateway connected";
    public static final String GATEWAY_DISCONNECTED = "Gateway diconnected";
    public static final String GATEWAY_UPDATED = "Gateway updated";
    public static final String GROUP_CLOSING = "group closing";
    public static final String GROUP_ADDED = "group added";
    public static final String GROUP_REMOVED = "Group deleted";
    public static final String GROUP_CONNECTING = "Group connecting";
    public static final String BUNDLE_UPDATED = "bundle updated";
    public static final String ENTITY_ADDED = "Entity added";
    public static final String GROUP_UPDATED = "Group updated";
    public static final String GATEWAY_REMOVED = "Gateway removed";
    public static final String ENTITY_REMOVED = "Entity removed";
    public static final String GATEWAYS_IN_GROUP_CONNECTED = "Gateways connected in a sub group";
    public static final String GATEWAY_ADDED_IN_A_GROUP = "Gateway added in a sub group";
    public static final String TRANSACTION_OPENED = "Transaction opened";
    private static HashMap<ManageableEntity, Vector<EntitiesEventListener>> entityListeners =
        new HashMap<ManageableEntity, Vector<EntitiesEventListener>>();
    private static Vector<EntitiesEventListener> allListeners = new Vector<EntitiesEventListener>();
    private HashMap<ObjectName, ManageableEntity> onEntities = new HashMap<ObjectName, ManageableEntity>();
    private ActiveNotificationListener notificationListener;

    /**
     *
     *
     */
    private EntitiesEventManager() {
        try {
            this.notificationListener = (ActiveNotificationListener) PAActiveObject.newActive(ActiveNotificationListener.class.getName(),
                    new Object[] {  });
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public static EntitiesEventManager getInstance() {
        if (instance == null) {
            instance = new EntitiesEventManager();
        }
        return instance;
    }

    public void listenTo(ManageableEntity entity) {
        try {
            this.notificationListener.listenTo(entity);
            onEntities.put(entity.getObjectName(), entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
         *
         * @param gateway
         * @param message
         * @param bundle
         */
    public void newEvent(ManageableEntity entity, String message) {
        fireListeners(entity, message + " on " + entity);
    }

    //    public void addConnectionlistener (ManageableEntity listener) {
    //    	System.out.println("EntitiesEventManager.addConnectionlistener() : " + listener);
    ////    	ConnectionListener cl = getConnectionListener(listener); 
    //	
    ////    	this.locationRemoteGateway.put(listener.getUrl(), listener);
    //    }

    //    private synchronized ConnectionListener getConnectionListener (RemoteGateway listener ){
    ////    	ConnectionListener cl = (ConnectionListener)this.locationConnectionListeners.get(listener.getUrl());
    ////    	if (cl == null) {
    ////    	
    ////        	try {
    ////        		cl= ConnectionListener.getInstance();
    //////        		cl = (ConnectionListener) ProActive.turnActive(cl);
    ////        		cl.addListener(this);
    //////        		System.out.println("listener.getO" + listener.getObjectName());
    //////            	cl.listenTo(listener.getObjectName());
    ////            	this.locationConnectionListeners.put(listener.getUrl(), cl);
    ////
    ////			} catch (Exception e) {
    ////				e.printStackTrace();
    ////			}
    ////        
    ////    	}
    //   	return nuyll;
    //    }

    /**
       *
       * @param listener
       * @param entity
       */
    public void subscribe(EntitiesEventListener listener,
        ManageableEntity entity) {
        Vector<EntitiesEventListener> listeners = entityListeners.get(entity);
        if (listeners == null) {
            listeners = new Vector<EntitiesEventListener>();
            entityListeners.put(entity, listeners);
        }
        listeners.addElement(listener);
    }

    /**
     *
     * @param listener
     */
    public void subscribeAll(EntitiesEventListener listener) {
        allListeners.addElement(listener);
    }

    /**
     *
     * @param listener
     */
    public void unsuscribeAllListener(EntitiesEventListener listener) {
        for (ManageableEntity me : entityListeners.keySet()) {
            entityListeners.get(me).removeElement(listener);
        }
        allListeners.removeElement(listener);
    }

    /**
     *
     * @param entity
     * @param message
     * @param bundle
     */
    private void fireListeners(ManageableEntity entity, String message) {
        try {
            Vector<EntitiesEventListener> listeners = entityListeners.get(entity);

            if (listeners != null) {
                for (EntitiesEventListener listener : listeners) {
                    listener.handleEntityEvent(entity, message);
                }
            }
            for (EntitiesEventListener listener : allListeners) {
                listener.handleEntityEvent(entity, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleNotification(Notification notification) {
        try {
            if (notification instanceof BundleNotification) {
                BundleNotification bNotif = (BundleNotification) notification;
                BundleInfo bInfo = bNotif.getBundleInfo();
                String message = notification.getMessage();
                String url = bInfo.getUrlGateway();
                ObjectName on = bNotif.getObjectName();

                int eventType = bNotif.getEventType();

                if (eventType == BundleNotification.BUNDLE_ADDED) {
                    RemoteGateway gateway = (RemoteGateway) onEntities.get(on);
                    gateway.addBundle(bInfo);
                    fireListeners(gateway, notification.toString());
                } else if ((eventType == BundleNotification.BUNDLE_STARTED) ||
                        (eventType == BundleNotification.BUNDLE_STOPPED) ||
                        (eventType == BundleNotification.BUNDLE_UPDATED)) {
                    RemoteBundle bundle = (RemoteBundle) onEntities.get(on);
                    bundle.setBundleInfo(bInfo);
                    fireListeners(bundle, notification.toString());
                } else if (eventType == BundleNotification.BUNDLE_UNINSTALLED) {
                    RemoteGateway gateway = (RemoteGateway) onEntities.get(on);
                    gateway.removeBundle(bInfo);
                    fireListeners(gateway, notification.toString());
                }
            } else if (notification instanceof TransactionCommitedNotification) {
                TransactionNotification n = (TransactionNotification) notification;
                RemoteTransactionManager.getInstance()
                                        .commitTransaction(n.getId());
                fireListeners(RemoteTransactionManager.getInstance()
                                                      .getTransaction(n.getId()),
                    n.toString());
            } else if (notification instanceof TransactionCancelledNotification) {
                TransactionNotification n = (TransactionNotification) notification;
                fireListeners(RemoteTransactionManager.getInstance()
                                                      .getTransaction(n.getId()),
                    n.toString());
                RemoteTransactionManager.getInstance()
                                        .cancelTransaction(n.getId());
            } else if (notification instanceof TransactionCommandNotification) {
                TransactionCommandNotification n = (TransactionCommandNotification) notification;

                RemoteTransaction transaction = RemoteTransactionManager.getInstance()
                                                                        .getTransaction(n.getId());
                transaction.addEntity(new RemoteCommand(transaction,
                        (String) n.getSource()));
                fireListeners(RemoteTransactionManager.getInstance()
                                                      .getTransaction(n.getId()),
                    n.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //	public void handleNotification(Notification n) {
    ////		System.out.println("Notification recue = " + n );
    //		try {
    //		LocalisableObject  source = (LocalisableObject)n.getSource();
    //		String url = source.getUrl();
    //		
    //		if (source instanceof Transaction) {		
    //			RemoteGateway rg = (RemoteGateway)this.transactionIdRemoteGateway.get(new Long(((Transaction)source).getId()));						
    //			rg.handleEntityEvent(rg, n.getMessage(), n.getSource());			 
    //		}  else {
    //			RemoteGateway rg = (RemoteGateway)this.locationRemoteGateway.get(source.getUrl());
    //			fireListeners (rg, n.getMessage(), source);
    //		}
    //		} catch (Exception e) {
    //			e.printStackTrace();
    //		}
    //	}
}
