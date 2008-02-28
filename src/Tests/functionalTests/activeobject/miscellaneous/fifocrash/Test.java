package functionalTests.activeobject.miscellaneous.fifocrash;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.xml.VariableContractType;

import functionalTests.FunctionalTestDefaultNodes;


/**
 * Tests that a crash in the receive reply from an ActiveObject doesn't crash the sender's Active Object.
 * See JIRA: PROACTIVE-234
 */
public class Test extends FunctionalTestDefaultNodes {

    boolean success = false;

    public Test() {
        super(DeploymentType._1x1);
        super.vContract.setVariableFromProgram("jvmargDefinedByTest", "-Xmx512M",
                VariableContractType.DescriptorDefaultVariable);
    }

    @org.junit.Test
    public void action() throws Exception {
        Node node = super.getANode();
        AOCrash2 ao2 = (AOCrash2) PAActiveObject.newActive(AOCrash2.class.getName(), new Object[] {}, node);
        AOCrash1 ao1 = (AOCrash1) PAActiveObject.newActive(AOCrash1.class.getName(), new Object[] { ao2 },
                node);
        // The call to foo will trigger a receiveReply on object ao1 from object ao2
        ao1.foo();
        // We terminate ao1 like a warrior
        ao1.terminate();

        // We test the life expectancy of ao2
        for (int i = 0; i < 20; i++) {
            Thread.sleep(100);
            // If the timeout expires, then ao2 is really dead
            BooleanWrapper bw = ao2.alive();
            PAFuture.waitFor(bw, 5000);

        }

    }

}
