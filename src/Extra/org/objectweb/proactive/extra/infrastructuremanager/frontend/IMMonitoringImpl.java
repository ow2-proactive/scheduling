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
package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMConstants;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMEvent;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMInitialState;
import org.objectweb.proactive.extra.infrastructuremanager.common.NodeEvent;
import org.objectweb.proactive.extra.infrastructuremanager.common.NodeSourceEvent;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreInterface;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;


/**
 * @author Ellendir
 *
 */
public class IMMonitoringImpl implements IMMonitoring, IMEventListener,
    InitActive {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_MONITORING);

    // Attributes
    private IMCoreInterface imcore;
    private HashMap<UniqueID, IMEventListener> IMListeners;

    // ----------------------------------------------------------------------//
    // CONSTRUTORS

    /** ProActive compulsory no-args constructor */
    public IMMonitoringImpl() {
    }

    public IMMonitoringImpl(IMCoreInterface imcore) {
        if (logger.isDebugEnabled()) {
            logger.debug("IMMonitoring constructor");
        }

        IMListeners = new HashMap<UniqueID, IMEventListener>();
        this.imcore = imcore;
    }

    // ----------------------------------------------------------------------//
    // Events handling
    public IMInitialState addIMEventListener(IMEventListener listener,
        IMEvent... events) {
        UniqueID id = ProActiveObject.getContext().getCurrentRequest()
                                     .getSourceBodyID();

        this.IMListeners.put(id, listener);
        return imcore.getIMInitialState();
    }

    private void dispatch(IMEvent methodName, Class<?>[] types, Object... params) {
        try {
            Method method = IMEventListener.class.getMethod(methodName.toString(),
                    types);

            Iterator<UniqueID> iter = this.IMListeners.keySet().iterator();
            while (iter.hasNext()) {
                UniqueID id = iter.next();
                try {
                    method.invoke(IMListeners.get(id), params);
                } catch (Exception e) {
                    iter.remove();
                    logger.error(
                        "!!!!!!!!!!!!!! IM has detected that a listener is not connected anymore !");
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    // =======================================================//
    public StringWrapper echo() {
        return new StringWrapper("Je suis le IMMonitoring");
    }

    // =======================================================//
    public HashMap<String, ProActiveDescriptor> getListDescriptor() {
        if (logger.isDebugEnabled()) {
            logger.debug("getListDescriptor");
        }
        return null;
        //TODO Germ todo
    }

    public HashMap<String, ArrayList<VirtualNode>> getDeployedVirtualNodeByPad() {
        if (logger.isDebugEnabled()) {
            logger.debug("getDeployedVirtualNodeByPad");
        }
        return null;
        //TODO Germ todo
    }

    public ArrayList<IMNode> getListAllIMNodes() {
        if (logger.isDebugEnabled()) {
            logger.debug("getListAllIMNodes");
        }

        return imcore.getListAllNodes();
    }

    public ArrayList<IMNode> getListFreeIMNode() {
        if (logger.isDebugEnabled()) {
            logger.debug("getListFreeIMNode");
        }

        return imcore.getListFreeIMNode();
    }

    public ArrayList<IMNode> getListBusyIMNode() {
        if (logger.isDebugEnabled()) {
            logger.debug("getListBusyIMNode");
        }

        return imcore.getListBusyIMNode();
    }

    public IntWrapper getNumberOfFreeResource() {
        if (logger.isDebugEnabled()) {
            logger.debug("getNumberOfFreeResource");
        }

        return imcore.getSizeListFreeIMNode();
    }

    public IntWrapper getNumberOfBusyResource() {
        if (logger.isDebugEnabled()) {
            logger.debug("getNumberOfBusyResource");
        }

        return imcore.getSizeListBusyIMNode();
    }

    public IntWrapper getNumberOfDownResource() {
        return this.imcore.getSizeListDownIMNode();
    }

    public IntWrapper getNumberOfAllResources() {
        return this.imcore.getNbAllIMNode();
    }

    /** inherited from IMEventListener
     * events generation
     */
    public void imKilledEvent() {
        dispatch(IMEvent.KILLED, null);
    }

    public void imShutDownEvent() {
        dispatch(IMEvent.SHUTDOWN, null);
    }

    public void imShuttingDownEvent() {
        dispatch(IMEvent.SHUTTING_DOWN, null);
    }

    public void imStartedEvent() {
        dispatch(IMEvent.STARTED, null);
    }

    public void nodeAddedEvent(NodeEvent n) {
        dispatch(IMEvent.NODE_ADDED, new Class<?>[] { NodeEvent.class }, n);
    }

    public void nodeBusyEvent(NodeEvent n) {
        dispatch(IMEvent.NODE_BUSY, new Class<?>[] { NodeEvent.class }, n);
    }

    public void nodeDownEvent(NodeEvent n) {
        dispatch(IMEvent.NODE_DOWN, new Class<?>[] { NodeEvent.class }, n);
    }

    public void nodeFreeEvent(NodeEvent n) {
        dispatch(IMEvent.NODE_FREE, new Class<?>[] { NodeEvent.class }, n);
    }

    public void nodeRemovedEvent(NodeEvent n) {
        dispatch(IMEvent.NODE_REMOVED, new Class<?>[] { NodeEvent.class }, n);
    }

    public void nodeToReleaseEvent(NodeEvent n) {
        dispatch(IMEvent.NODE_TO_RELEASE, new Class<?>[] { NodeEvent.class }, n);
    }

    public void nodeSourceAddedEvent(NodeSourceEvent ns) {
        dispatch(IMEvent.NODESOURCE_CREATED,
            new Class<?>[] { NodeSourceEvent.class }, ns);
    }

    public void nodeSourceRemovedEvent(NodeSourceEvent ns) {
        dispatch(IMEvent.NODESOURCE_REMOVED,
            new Class<?>[] { NodeSourceEvent.class }, ns);
    }

    public void initActivity(Body body) {
        try {
            ProActiveObject.register((IMMonitoring) ProActiveObject.getStubOnThis(),
                "//localhost/" + IMConstants.NAME_ACTIVE_OBJECT_IMMONITORING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
