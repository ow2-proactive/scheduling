package functionalTests.gcmdeployment.remoteobject;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestGCMRemoteObjects extends GCMFunctionalTestDefaultNodes {
    public TestGCMRemoteObjects() {
        super(1, 1);
    }

    @Test
    public void testRemote() throws ActiveObjectCreationException, NodeException, InterruptedException {
        Node node = super.getANode();
        AO ao = (AO) PAActiveObject.newActive(AO.class.getName(), new Object[] { super.gcmad }, node);
        Assert.assertTrue(ao.finished());
    }

    static public class AO implements Serializable, InitActive {
        GCMApplication gcma;
        boolean success = true;

        public AO() {

        }

        public AO(GCMApplication gcma) {
            this.gcma = gcma;
        }

        public void initActivity(Body body) {
            try {
                GCMVirtualNode vn1 = gcma.getVirtualNode(GCMFunctionalTestDefaultNodes.VN_NAME);
                if (vn1 == null)
                    success = false;

                boolean atLeastOne = false;
                for (GCMVirtualNode vn : gcma.getVirtualNodes().values()) {
                    atLeastOne = true;
                    for (Node node : vn.getCurrentNodes()) {
                        System.out.println(node.getNodeInformation().getURL());
                    }
                }
                if (atLeastOne != true)
                    success = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean finished() {
            return success;
        }
    }
}
