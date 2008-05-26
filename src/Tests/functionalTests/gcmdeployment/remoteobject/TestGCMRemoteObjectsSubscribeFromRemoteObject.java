package functionalTests.gcmdeployment.remoteobject;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Semaphore;

import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestGCMRemoteObjectsSubscribeFromRemoteObject extends GCMFunctionalTestDefaultNodes {
    public TestGCMRemoteObjectsSubscribeFromRemoteObject() {
        super(DeploymentType._1x1);
    }

    @Test
    public void testRemote() throws InterruptedException, ProActiveException, URISyntaxException {

        GCMVirtualNode vn1 = super.gcmad.getVirtualNode(GCMFunctionalTestDefaultNodes.VN_NAME);

        Node node = super.getANode();

        RemoteAO rao = (RemoteAO) PAActiveObject.newActive(RemoteAO.class.getName(), new Object[] {}, node);
        String url = rao.createRemoteObject();
        RemoteObject remoteObject = RemoteObjectHelper.lookup(new URI(url));
        RO ro = (RO) RemoteObjectHelper.generatedObjectStub(remoteObject);

        rao.setVirtualNode(vn1);
        ro.setVirtualNode(vn1);
        ro.setRemoteObject(ro);
        ro.setCallback();
        ro.waitSuccess();
    }

    static public class RO {
        private GCMVirtualNode vn;
        private RO _this;
        private Semaphore sem = new Semaphore(0);

        boolean success = false;

        public RO() {

        }

        public void setVirtualNode(GCMVirtualNode vn) {
            this.vn = vn;
        }

        public void setRemoteObject(RO _this) {
            this._this = _this;
        }

        public void setCallback() {
            try {
                vn.subscribeNodeAttachment(_this, "callback", true);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            System.out.println("SUBSCRIBED");
        }

        public void callback(Node node, String vnName) {
            System.out.println("Callback occured !");
            sem.release();
        }

        public void waitSuccess() throws InterruptedException {
            System.out.println("Waiting for semaphore");
            sem.acquire();
            System.out.println("Semaphore acquired");
        }

    }

    static public class RemoteAO implements Serializable {
        GCMVirtualNode vn;

        public RemoteAO() {

        }

        public void setVirtualNode(GCMVirtualNode vn) {
            this.vn = vn;
        }

        public String createRemoteObject() {
            try {
                RO ro = new RO();
                RemoteObjectExposer<RO> roe = new RemoteObjectExposer<RO>(RO.class.getName(), ro);
                URI uri = RemoteObjectHelper.generateUrl("remoteObject");
                roe.activateProtocol(uri);
                return roe.getURL();
            } catch (UnknownProtocolException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
