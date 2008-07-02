package org.ow2.proactive.resourcemanager.gui.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.gui.interfaces.RMCoreEventListener;
import org.ow2.proactive.resourcemanager.gui.interfaces.RMNodeEventListener;


/**
 * @author The ProActive Team
 */
public class ResourceManagerController implements RMEventListener, Serializable {

    // The shared instance view as a direct reference
    private static ResourceManagerController localView = null;
    // The shared instance view as an active object
    private static ResourceManagerController activeView = null;

    private List<RMNodeEventListener> nodeListeners = null;
    private List<RMCoreEventListener> coreListeners = null;
    private List<RMNodeEvent> busyNodes = null;
    private List<RMNodeEvent> downNodes = null;
    private List<RMNodeEvent> freeNodes = null;
    private List<RMNodeEvent> toReleaseNodes = null;
    private List<RMNodeSourceEvent> sourceNodes = null;

    private RMInitialState initialState = null;

    /**
     * The default constructor.
     * 
     * @param imMonitoring
     */
    public ResourceManagerController() {
        this.nodeListeners = new ArrayList<RMNodeEventListener>();
        this.coreListeners = new ArrayList<RMCoreEventListener>();
        this.busyNodes = new ArrayList<RMNodeEvent>();
        this.downNodes = new ArrayList<RMNodeEvent>();
        this.freeNodes = new ArrayList<RMNodeEvent>();
        this.toReleaseNodes = new ArrayList<RMNodeEvent>();
        this.sourceNodes = new ArrayList<RMNodeSourceEvent>();
    }

    public void addNodeListener(RMNodeEventListener listener) {
        nodeListeners.add(listener);
    }

    public void addCoreListener(RMCoreEventListener listener) {
        coreListeners.add(listener);
    }

    public void removeCoreListener() {
        coreListeners.clear();
    }

    /**
     * To get the busyNodes
     * 
     * @return the busyNodes
     */
    public List<RMNodeEvent> getBusyNodes() {
        return busyNodes;
    }

    /**
     * To get the downNodes
     * 
     * @return the downNodes
     */
    public List<RMNodeEvent> getDownNodes() {
        return downNodes;
    }

    /**
     * To get the freeNodes
     * 
     * @return the freeNodes
     */
    public List<RMNodeEvent> getFreeNodes() {
        return freeNodes;
    }

    /**
     * To get the toReleaseNodes
     * 
     * @return the toReleaseNodes
     */
    public List<RMNodeEvent> getToReleaseNodes() {
        return toReleaseNodes;
    }

    /**
     * To get the sourceNodes
     * 
     * @return the sourceNodes
     */
    public List<RMNodeSourceEvent> getSourceNodes() {
        return sourceNodes;
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.frontend.RMEventListener#rmKilledEvent(org.objectweb.proactive.extensions.resourcemanager.common.event.RMEvent)
     */
    public void rmKilledEvent(RMEvent event) {
        rmKilledEventInternal();
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.frontend.RMEventListener#rmShutDownEvent(org.objectweb.proactive.extensions.resourcemanager.common.event.RMEvent)
     */
    public void rmShutDownEvent(RMEvent event) {
        rmShutDownEventInternal();
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.frontend.RMEventListener#rmShuttingDownEvent(org.objectweb.proactive.extensions.resourcemanager.common.event.RMEvent)
     */
    public void rmShuttingDownEvent(RMEvent event) {
        rmShuttingDownEventInternal();
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.frontend.RMEventListener#rmStartedEvent(org.objectweb.proactive.extensions.resourcemanager.common.event.RMEvent)
     */
    public void rmStartedEvent(RMEvent event) {
        rmStartedEventInternal();
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.frontend.RMEventListener#nodeAddedEvent(org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeEvent)
     */
    public void nodeAddedEvent(RMNodeEvent nodeEvent) {
        nodeAddedEventInternal(nodeEvent);
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.frontend.RMEventListener#nodeBusyEvent(org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeEvent)
     */
    public void nodeBusyEvent(RMNodeEvent nodeEvent) {
        nodeBusyEventInternal(nodeEvent);

    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.frontend.RMEventListener#nodeDownEvent(org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeEvent)
     */
    public void nodeDownEvent(RMNodeEvent nodeEvent) {
        nodeDownEventInternal(nodeEvent);
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.frontend.RMEventListener#nodeFreeEvent(org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeEvent)
     */
    public void nodeFreeEvent(RMNodeEvent nodeEvent) {
        nodeFreeEventInternal(nodeEvent);
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.frontend.RMEventListener#nodeRemovedEvent(org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeEvent)
     */
    public void nodeRemovedEvent(RMNodeEvent nodeEvent) {
        nodeRemovedEventInternal(nodeEvent);
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.frontend.RMEventListener#nodeSourceAddedEvent(org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeSourceEvent)
     */
    public void nodeSourceAddedEvent(RMNodeSourceEvent nodeSourceEvent) {
        nodeSourceAddedEventInternal(nodeSourceEvent);
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.frontend.RMEventListener#nodeSourceRemovedEvent(org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeSourceEvent)
     */
    public void nodeSourceRemovedEvent(RMNodeSourceEvent nodeSourceEvent) {
        nodeSourceRemovedEventInternal(nodeSourceEvent);
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.frontend.RMEventListener#nodeToReleaseEvent(org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeEvent)
     */
    public void nodeToReleaseEvent(RMNodeEvent nodeEvent) {
        nodeToReleaseEventInternal(nodeEvent);
    }

    private void rmKilledEventInternal() {
        for (RMCoreEventListener l : coreListeners)
            l.imKilledEvent();
    }

    private void rmShutDownEventInternal() {
        for (RMCoreEventListener l : coreListeners)
            l.imShutDownEvent();
    }

    private void rmShuttingDownEventInternal() {
        for (RMCoreEventListener l : coreListeners)
            l.imShuttingDownEvent();
    }

    private void rmStartedEventInternal() {
        for (RMCoreEventListener l : coreListeners)
            l.imStartedEvent();
    }

    private void nodeAddedEventInternal(RMNodeEvent nodeEvent) {
        for (RMNodeEventListener l : nodeListeners)
            l.nodeAddedEvent(nodeEvent);
    }

    private void nodeRemovedEventInternal(RMNodeEvent nodeEventodeEvent) {
        for (RMNodeEventListener l : nodeListeners)
            l.nodeRemovedEvent(nodeEventodeEvent);
    }

    private void nodeBusyEventInternal(RMNodeEvent nodeEvent) {
        for (RMNodeEventListener l : nodeListeners)
            l.nodeBusyEvent(nodeEvent);
    }

    private void nodeDownEventInternal(RMNodeEvent nodeEvent) {
        for (RMNodeEventListener l : nodeListeners)
            l.nodeDownEvent(nodeEvent);
    }

    private void nodeFreeEventInternal(RMNodeEvent nodeEvent) {
        for (RMNodeEventListener l : nodeListeners)
            l.nodeFreeEvent(nodeEvent);
    }

    private void nodeToReleaseEventInternal(RMNodeEvent nodeEvent) {
        for (RMNodeEventListener l : nodeListeners)
            l.nodeToReleaseEvent(nodeEvent);
    }

    private void nodeSourceAddedEventInternal(RMNodeSourceEvent nodeSourceEvent) {
        for (RMNodeEventListener l : nodeListeners)
            l.nodeSourceAddedEvent(nodeSourceEvent);
    }

    private void nodeSourceRemovedEventInternal(RMNodeSourceEvent nodeSourceEvent) {
        for (RMNodeEventListener l : nodeListeners)
            l.nodeSourceRemovedEvent(nodeSourceEvent);
    }

    public boolean init() {
        RMMonitoring imMonitoring = RMStore.getInstance().getRMMonitoring();
        initialState = imMonitoring.addRMEventListener((RMEventListener) PAActiveObject.getStubOnThis());
        return true; // just for synchronous call
    }

    public static ResourceManagerController getLocalView() {
        if (localView == null) {
            localView = new ResourceManagerController();
        }
        return localView;
    }

    public static ResourceManagerController getActiveView() {
        if (activeView == null) {
            turnActive();
        }
        return activeView;
    }

    public static ResourceManagerController turnActive() {
        try {
            activeView = (ResourceManagerController) PAActiveObject.turnActive(getLocalView());
            return activeView;
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void clearInstances() {
        localView = null;
        activeView = null;
    }

    public RMInitialState getInitialState() {
        return initialState;
    }
}