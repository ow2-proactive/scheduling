package org.ow2.proactive.resourcemanager.gui.data;

import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.RMUser;
import org.ow2.proactive.resourcemanager.gui.data.model.RMModel;
import org.ow2.proactive.resourcemanager.gui.views.ResourceExplorerView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesCompactView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesTabView;
import org.ow2.proactive.resourcemanager.gui.views.StatisticsView;


public class EventsReceiver implements InitActive, RMEventListener {

    private static final long RM_SERVER_PING_FREQUENCY = 5000;
    private RMModel model = null;
    private RMMonitoring monitor = null;
    private Thread pinger;

    public EventsReceiver() {
    }

    public EventsReceiver(RMMonitoring monitorStub) {
        this.monitor = monitorStub;
    }

    public void initActivity(Body body) {
        model = RMStore.getInstance().getModel();
        RMInitialState initialState = monitor.addRMEventListener((RMEventListener) PAActiveObject
                .getStubOnThis());
        for (RMNodeSourceEvent nodeSourceEvent : initialState.getNodeSource()) {
            model.addNodeSource(nodeSourceEvent);
        }

        for (RMNodeEvent nodeEvent : initialState.getNodesEvents()) {
            model.addNode(nodeEvent);
        }

        model.setUpdateViews(true);
        // Init opened views AFTER model's construction
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                //init Tab view if tab panel is displayed
                if (ResourcesTabView.getTabViewer() != null) {
                    ResourcesTabView.init();
                }
                //init Tree view if tree panel is displayed
                if (ResourceExplorerView.getTreeViewer() != null) {
                    ResourceExplorerView.init();
                    ResourceExplorerView.getTreeViewer().expandAll();
                }
                //init Tree view if tree panel is displayed
                if (ResourcesCompactView.getCompactViewer() != null) {
                    ResourcesCompactView.getCompactViewer().loadMatrix();
                }
                //init stats view if stats panel is displayed
                if (StatisticsView.getStatsViewer() != null) {
                    StatisticsView.init();
                }
            }
        });
        startPinger();
    }

    private void startPinger() {
        final EventsReceiver thisStub = (EventsReceiver) PAActiveObject.getStubOnThis();
        pinger = new Thread() {
            @Override
            public void run() {
                while (!pinger.isInterrupted()) {
                    try {
                        Thread.sleep(RM_SERVER_PING_FREQUENCY);
                        try {
                            //try to ping RM server
                            RMUser userAO = RMStore.getInstance().getRMUser();
                            if (PAActiveObject.pingActiveObject(userAO)) {
                                //if OK continue
                                continue;
                            } else {
                                //if not, shutdown RM
                                thisStub.rmShutDownEvent(new RMEvent(), true);
                            }
                        } catch (RMException e) {
                            //if exception, considered RM as down
                            thisStub.rmShutDownEvent(new RMEvent(), true);
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };
        pinger.start();
    }

    // ----------------------------------------------------------------------
    // methods implemented from RMEvent listener
    // ----------------------------------------------------------------------   
    /**
     * @see org.ow2.proactive.resourcemanager.gui.interfaces.RMNodeEventListener#nodeAddedEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeEvent)
     */
    public void nodeAddedEvent(RMNodeEvent nodeEvent) {
        model.addNode(nodeEvent);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.gui.interfaces.RMNodeEventListener#nodeRemovedEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeEvent)
     */
    public void nodeRemovedEvent(RMNodeEvent nodeEvent) {
        model.removeNode(nodeEvent);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.gui.interfaces.RMNodeEventListener#nodeBusyEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeEvent)
     */
    public void nodeBusyEvent(RMNodeEvent nodeEvent) {
        model.changeNodeState(nodeEvent);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.gui.interfaces.RMNodeEventListener#nodeDownEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeEvent)
     */
    public void nodeDownEvent(RMNodeEvent nodeEvent) {
        model.changeNodeState(nodeEvent);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.gui.interfaces.RMNodeEventListener#nodeFreeEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeEvent)
     */
    public void nodeFreeEvent(RMNodeEvent nodeEvent) {
        model.changeNodeState(nodeEvent);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.gui.interfaces.RMNodeEventListener#nodeToReleaseEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeEvent)
     */
    public void nodeToReleaseEvent(RMNodeEvent nodeEvent) {
        model.changeNodeState(nodeEvent);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.gui.interfaces.RMNodeEventListener#nodeSourceAddedEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeSourceEvent)
     */
    public void nodeSourceAddedEvent(RMNodeSourceEvent nodeSourceEvent) {
        model.addNodeSource(nodeSourceEvent);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.gui.interfaces.RMNodeEventListener#nodeSourceRemovedEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeSourceEvent)
     */
    public void nodeSourceRemovedEvent(RMNodeSourceEvent nodeSourceEvent) {
        model.removeNodeSource(nodeSourceEvent);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMEventListener#nodeSourceNodesAcquisitionInfoAddedEvent(org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent)
     */
    public void nodeSourceNodesAcquisitionInfoAddedEvent(RMNodeSourceEvent event) {
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMEventListener#rmShutDownEvent(org.ow2.proactive.resourcemanager.common.event.RMEvent)
     */
    public void rmShutDownEvent(RMEvent arg0) {
        pinger.interrupt();
        RMStore.getInstance().shutDownActions(false);
    }

    public void rmShutDownEvent(RMEvent arg0, boolean failed) {
        pinger.interrupt();
        RMStore.getInstance().shutDownActions(failed);
    }

    //TODO add a status bar that show these states ?

    public void rmShuttingDownEvent(RMEvent arg0) {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                RMStatusBarItem.getInstance().setText("RM shutting down");
            }
        });
    }

    public void rmStartedEvent(RMEvent arg0) {
    }

}
