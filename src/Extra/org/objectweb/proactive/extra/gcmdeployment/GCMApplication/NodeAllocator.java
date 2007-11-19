package org.objectweb.proactive.extra.gcmdeployment.GCMApplication;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;
import static org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers.GCM_NODEALLOC_LOGGER;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNodeInternal;


public class NodeAllocator implements NotificationListener {
    final static public int PERIOD = 3000;
    GCMApplicationDescriptorImpl gcma;
    List<VirtualNodeInternal> virtualNodes;
    Map<Node, GCMDeploymentDescriptor> nodePool;

    public NodeAllocator(GCMApplicationDescriptorImpl gcma,
        Collection<VirtualNodeInternal> virtualNodes) {
        this.gcma = gcma;

        this.virtualNodes = new LinkedList<VirtualNodeInternal>();
        this.virtualNodes.addAll(virtualNodes);

        this.nodePool = new ConcurrentHashMap<Node, GCMDeploymentDescriptor>();

        subscribeJMXRuntimeEvent();
        startGreedyThread();
    }

    public void subscribeJMXRuntimeEvent() {
        JMXNotificationManager.getInstance()
                              .subscribe(ProActiveRuntimeImpl.getProActiveRuntime()
                                                             .getMBean()
                                                             .getObjectName(),
            this);
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
        String type = notification.getType();

        if (NotificationType.GCMRuntimeRegistered.equals(type)) {
            GCMRuntimeRegistrationNotificationData data = (GCMRuntimeRegistrationNotificationData) notification.getUserData();

            GCMDeploymentDescriptor nodeProvider = gcma.getGCMDeploymentDescriptorId(data.getDeploymentId());

            for (Node node : data.getNodes()) {
                try {
                    GCM_NODEALLOC_LOGGER.trace("Dispatching: node " +
                        node.getNodeInformation().getURL() + " from " +
                        nodeProvider.getDescriptorFilePath());
                    boolean dispatched = dispatch(node, nodeProvider);
                    if (!dispatched) {
                        GCM_NODEALLOC_LOGGER.trace(
                            "Node rejected by every VN, put it into the nodePool for latter retry");
                        nodePool.put(node, nodeProvider);
                    }
                } catch (Exception e) {
                    // If not handled by us, JMX eats the Exception !
                    GCM_NODEALLOC_LOGGER.warn(e);
                }
            }
        }
    }

    synchronized public boolean dispatch(Node node,
        GCMDeploymentDescriptor nodeProvider) {
        // Check if a virtualNode need this Node to fulfill a NodeProvider requirement
        for (VirtualNodeInternal virtualNode : virtualNodes) {
            boolean dispatched = virtualNode.doesNodeProviderNeed(node,
                    nodeProvider);
            if (dispatched) {
                return true;
            }
        }

        for (VirtualNodeInternal virtualNode : virtualNodes) {
            boolean dispatched = virtualNode.doYouNeed(node, nodeProvider);
            if (dispatched) {
                return true;
            }
        }

        return false;
    }

    synchronized private boolean dispatchToGreedy(Node node,
        GCMDeploymentDescriptor nodeProvider) {
        // To be fair we have to wait that all Virtual Node are Ready
        for (VirtualNodeInternal virtualNode : virtualNodes) {
            boolean dispatched = virtualNode.doYouWant(node, nodeProvider);
            if (dispatched) {
                // For Round Robin between Virtual Node
                virtualNodes.add(virtualNodes.remove(0));
                return true;
            }
        }

        return false;
    }

    private void startGreedyThread() {
        Thread t = new Thread() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(PERIOD);
                        } catch (InterruptedException e) {
                            GCM_NODEALLOC_LOGGER.info(e);
                        }
                        for (Node node : nodePool.keySet()) {
                            boolean dispatched;
                            GCM_NODEALLOC_LOGGER.trace("Redispatching: node " +
                                node.getNodeInformation().getURL() + " from " +
                                nodePool.get(node).getDescriptorFilePath());
                            dispatched = dispatch(node, nodePool.get(node));
                            if (dispatched) {
                                nodePool.remove(node);
                                continue;
                            }

                            GCM_NODEALLOC_LOGGER.trace(
                                "Greedy Dispatching: node " +
                                node.getNodeInformation().getURL() + " from " +
                                nodePool.get(node).getDescriptorFilePath());
                            dispatched = dispatchToGreedy(node,
                                    nodePool.get(node));
                            if (dispatched) {
                                nodePool.remove(node);
                            }
                        }
                    }
                }
            };

        t.setDaemon(true);
        t.start();
    }
}
