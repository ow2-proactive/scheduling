package nonregressiontest.group;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.core.group.spmd.ProSPMD;
import org.objectweb.proactive.core.util.UrlBuilder;


public class A implements InitActive, RunActive, EndActive,
    java.io.Serializable {
    private String name = "anonymous";
    private boolean onewayCallReceived = false;
    private String nodename;
    private String hostname;

    public A() {
    }

    public A(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void onewayCall() {
        this.onewayCallReceived = true;
    }

    public void onewayCall(A a) {
        this.onewayCallReceived = true;
    }

    public boolean isOnewayCallReceived() {
        return this.onewayCallReceived;
    }

    public A asynchronousCall() {
        return new A(this.name + "_Clone");
    }

    public A asynchronousCall(A a) {
        return new A(a.getName() + "_Clone");
    }

    public String getHostName() {
        try { //return the name of the Host 
            return UrlBuilder.getHostNameorIP(java.net.InetAddress.getLocalHost())
                             .toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            return "getName failed";
        }
    }

    public String getNodeName() {
        try {
            //return the name of the Node  
            return ProActive.getBodyOnThis().getNodeURL().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            return "getNodeName failed";
        }
    }

    public void moveTo(String nodeURL) throws Exception {
        // System.out.println(" I am going to migate");
        ProActive.migrateTo(nodeURL);
        // System.out.println("migration done");
    }

    public void endBodyActivity() throws Exception {
        ProActive.getBodyOnThis().terminate();
    }

    public void initActivity(Body body) {
        // System.out.println("Initialization of the Activity");
    }

    public void runActivity(Body body) {
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        while (body.isActive()) {
            // The synchro policy is FIFO
            service.blockingServeOldest();
        }
    }

    public void endActivity(Body body) {
        // System.out.println("End of the activity of this Active Object");
    }

    public A asynchronousCallException() throws Exception {
        throw new Exception();
    }

    public void invokeBarrier() {
        ProSPMD.barrier("MyBarrier");
    }
}
