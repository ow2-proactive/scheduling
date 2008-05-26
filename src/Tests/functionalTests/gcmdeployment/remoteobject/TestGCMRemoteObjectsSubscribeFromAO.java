package functionalTests.gcmdeployment.remoteobject;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestGCMRemoteObjectsSubscribeFromAO extends GCMFunctionalTestDefaultNodes {
    public TestGCMRemoteObjectsSubscribeFromAO() {
        super(DeploymentType._1x1);
    }

    @Test
    public void testRemote() throws ActiveObjectCreationException, NodeException, InterruptedException {

        Node node = super.getANode();
        RemoteAO rao = (RemoteAO) PAActiveObject.newActive(RemoteAO.class.getName(),
                new Object[] { super.gcmad }, node);

        Assert.assertTrue(rao.isSuccess().booleanValue());
    }

    static public class RemoteAO implements Serializable, RunActive {
        GCMApplication gcma;
        boolean success = false;

        public RemoteAO() {

        }

        public RemoteAO(GCMApplication gcma) {
            this.gcma = gcma;
        }

        public void callback(Node node, String vnName) {
            System.out.println("Callback occured !");
            success = true;
        }

        public void runActivity(Body body) {
            Service service = new Service(body);

            try {
                GCMVirtualNode vn1 = gcma.getVirtualNode(GCMFunctionalTestDefaultNodes.VN_NAME);
                vn1.subscribeNodeAttachment(PAActiveObject.getStubOnThis(), "callback", true);

                service.blockingServeOldest("callback");
            } catch (Exception e) {
                e.printStackTrace();
            }

            service.blockingServeOldest("isSuccess");
        }

        public BooleanWrapper isSuccess() {
            return new BooleanWrapper(success);
        }
    }
}
