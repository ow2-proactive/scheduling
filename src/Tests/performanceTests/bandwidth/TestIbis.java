package performanceTests.bandwidth;

import java.io.Serializable;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.xml.VariableContractType;

import performanceTests.HudsonReport;
import performanceTests.Performance;
import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestIbis extends GCMFunctionalTestDefaultNodes {
    static {
        PAProperties.PA_COMMUNICATION_PROTOCOL.setValue("ibis");
    }

    final static public byte buf[] = new byte[10 * 1024 * 1024]; // 1Mo

    public TestIbis() {
        super(DeploymentType._1x1);
        super.vContract.setVariableFromProgram(super.VAR_JVMARG, PAProperties.PA_COMMUNICATION_PROTOCOL
                .getCmdLine() +
            "ibis", VariableContractType.DescriptorDefaultVariable);
    }

    @Test
    public void test() throws ActiveObjectCreationException, NodeException {
        Server server = (Server) PAActiveObject.newActive(Server.class.getName(), new Object[] {}, super
                .getANode());
        Client client = (Client) PAActiveObject.newActive(Client.class.getName(), new Object[] { server });
        client.startTest();
    }

    static public class Server implements Serializable {
        boolean firstRequest = true;
        long count = 0;
        long startTime;

        public Server() {

        }

        public int serve(byte[] buf) {
            if (firstRequest) {
                startTime = System.currentTimeMillis();
                firstRequest = false;
            }

            count++;
            return 0;
        }

        public void finish() {
            long endTime = System.currentTimeMillis();
            double size = (1.0 * TestIbis.buf.length * count) / (1024 * 1024);

            System.out.println("Size: " + size);
            System.out.println("Duration: " + (endTime - startTime));

            double bandwith = (1000.0 * size) / (endTime - startTime);
            System.out.println("Bandwidth " + bandwith);
            HudsonReport.reportToHudson(TestIbis.class, bandwith);
        }
    }

    static public class Client implements Serializable {
        private Server server;

        public Client() {

        }

        public Client(Server server) {
            this.server = server;
        }

        public int startTest() {
            // Warmup
            for (int i = 0; i < 10; i++) {
                server.serve(TestIbis.buf);
            }
            System.out.println("End of warmup");

            long startTime = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() - startTime > Performance.DURATION)
                    break;

                server.serve(TestIbis.buf);
            }
            server.finish();

            // startTest must be sync 
            return 0;
        }
    }
}
