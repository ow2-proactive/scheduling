package nonregressiontest.ft.cic;

import nonregressiontest.ft.Agent;
import nonregressiontest.ft.Collector;
import nonregressiontest.ft.ReInt;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.exceptions.body.ServiceFailedCalleeNFE;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.core.exceptions.manager.TypedNFEListener;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.JVMProcessImpl;

import testsuite.test.FunctionalTest;


public class Test extends FunctionalTest {
    private int result = 0;
    private JVMProcessImpl server;
    private static String FT_XML_LOCATION_UNIX = Test.class.getResource(
            "/nonregressiontest/ft/testFT_CIC.xml").getPath();
    private static int AWAITED_RESULT = -395585227;

    /**
     *
     */
    public Test() {
        super("Active Object failure and recovery with CIC protocol. (This test can be quite long ...)",
            "AO fails during the computation, and is restarted. " +
            "Communications between passive object, non-ft active object and ft active object.");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        // deployer le FTServer !
        this.server = new JVMProcessImpl(new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());
        // this.server = new JVMProcessImpl(new org.objectweb.proactive.core.process.AbstractExternalProcess.NullMessageLogger());
        this.server.setClassname(
            "org.objectweb.proactive.core.body.ft.servers.StartFTServer");
        this.server.startProcess();
        Thread.sleep(3000);

        // ProActive descriptor
        ProActiveDescriptor pad;
        VirtualNode vnode;

        //	create nodes
        pad = ProActive.getProactiveDescriptor(Test.FT_XML_LOCATION_UNIX);
        pad.activateMappings();
        vnode = pad.getVirtualNode("Workers");
        Node[] nodes = vnode.getNodes();

        Agent a = (Agent) ProActive.newActive(Agent.class.getName(),
                new Object[0], nodes[0]);
        Agent b = (Agent) ProActive.newActive(Agent.class.getName(),
                new Object[0], nodes[1]);

        // not ft !
        Collector c = (Collector) ProActive.newActive(Collector.class.getName(),
                new Object[0]);

        // a *normal* ServiceFailedCalleeNFE could appear; ignore ot for test
        ProActive.addNFEListenerOnAO(a, new TypedNFEListener(ServiceFailedCalleeNFE.class, NFEListener.NOOP_LISTENER));
        ProActive.addNFEListenerOnAO(b, new TypedNFEListener(ServiceFailedCalleeNFE.class, NFEListener.NOOP_LISTENER));
        
        a.initCounter(1);
        b.initCounter(1);
        a.setNeighbour(b);
        b.setNeighbour(a);
        a.setLauncher(c);

        c.go(a, 500);

        //failure in 11 sec...
        Thread.sleep(11000);
        try {
            nodes[1].getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        ReInt r = c.getResult();
        this.result = r.getValue();

        //cleaning
        this.server.stopProcess();
        pad.killall(false);

        //System.out.println(" ---------> RES = " + r.getValue()); //1771014405
    }

    public boolean postConditions() throws Exception {
        return this.result == Test.AWAITED_RESULT;
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
    }
}
