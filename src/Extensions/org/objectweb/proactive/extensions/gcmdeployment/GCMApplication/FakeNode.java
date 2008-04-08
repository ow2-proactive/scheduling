package org.objectweb.proactive.extensions.gcmdeployment.GCMApplication;

import java.rmi.AlreadyBoundException;
import java.util.List;

import org.objectweb.proactive.core.descriptor.services.TechnicalService;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeInternal;


public class FakeNode {
    private GCMApplicationInternal gcma;
    private ProActiveRuntime part;
    private boolean created;

    public FakeNode(GCMApplicationInternal gcma, ProActiveRuntime part) {
        this.part = part;
        this.gcma = gcma;

        created = false;
    }

    public ProActiveRuntime getProActiveRuntime() {
        return part;
    }

    public String getRuntimeURL() {
        return part.getURL();
    }

    public long getCapacity() {
        return part.getVMInformation().getCapacity();
    }

    public Node create(GCMVirtualNodeInternal vn, List<TechnicalService> tsList) {

        Node node = null;
        if (!created) {
            String jobIb = new Long(gcma.getDeploymentId()).toString();

            try {
                node = part.createGCMNode(null, vn.getName(), jobIb, tsList);
            } catch (NodeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (AlreadyBoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            gcma.addNode(node);
        }
        return node;
    }

}
