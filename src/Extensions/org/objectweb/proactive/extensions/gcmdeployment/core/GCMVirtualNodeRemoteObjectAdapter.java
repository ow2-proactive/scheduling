package org.objectweb.proactive.extensions.gcmdeployment.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.remoteobject.SynchronousProxy;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.gcmdeployment.Topology;


public class GCMVirtualNodeRemoteObjectAdapter extends Adapter<GCMVirtualNode> implements GCMVirtualNode,
        Serializable {

    boolean isLocal = true;
    transient GCMVirtualNode vn;

    @Override
    protected void construct() {
        System.out.println("CONSTRUCT CALLED");
        vn = GCMVirtualNodeImpl.getLocal(target.getUniqueID());
        if (vn == null) {
            System.out.println("VIRTUAL NODE IS REMOTE");
            isLocal = false;
            vn = target;
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        System.out.println("READOBJECT CALLED");
        vn = GCMVirtualNodeImpl.getLocal(target.getUniqueID());
        if (vn == null) {
            System.out.println("VIRTUAL NODE IS REMOTE");
            isLocal = false;
            vn = target;
        }
    }

    public Node getANode() {
        return vn.getANode();
    }

    public Node getANode(int timeout) {
        return vn.getANode(timeout);
    }

    public List<Node> getCurrentNodes() {
        return vn.getCurrentNodes();
    }

    public Topology getCurrentTopology() {
        return vn.getCurrentTopology();
    }

    public String getName() {
        return vn.getName();
    }

    public long getNbCurrentNodes() {
        return vn.getNbCurrentNodes();
    }

    public long getNbRequiredNodes() {
        return vn.getNbRequiredNodes();
    }

    public List<Node> getNewNodes() {
        return vn.getNewNodes();
    }

    public boolean isGreedy() {
        return vn.isGreedy();
    }

    public boolean isReady() {
        return vn.isReady();
    }

    public boolean subscribeIsReady(Object client, String methodName) {

        // TODO check vn.isLocal = true
        return vn.subscribeIsReady(client, methodName);
    }

    public void subscribeNodeAttachment(Object client, String methodName, boolean withHistory)
            throws ProActiveException {
        if (!isLocal && (client instanceof BodyProxy) || (client instanceof ProxyForGroup) ||
            (client instanceof SynchronousProxy)) {
            throw new ProActiveException(
                "Remote subscription is only possible when client is an Active Object, a Group or a Remote Object");
        }

        vn.subscribeNodeAttachment(client, methodName, withHistory);

    }

    public void unsubscribeIsReady(Object client, String methodName) {
        // TODO check vn.isLocal = true
        vn.unsubscribeIsReady(client, methodName);
    }

    public void unsubscribeNodeAttachment(Object client, String methodName) {
        // TODO check vn.isLocal = true
        vn.unsubscribeNodeAttachment(client, methodName);
    }

    public void updateTopology(Topology topology) {
        vn.updateTopology(topology);
    }

    public void waitReady() {
        vn.waitReady();
    }

    public void waitReady(int timeout) throws ProActiveTimeoutException {
        vn.waitReady(timeout);
    }

    public UniqueID getUniqueID() {
        return vn.getUniqueID();
    }
}
