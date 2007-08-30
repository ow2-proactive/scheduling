package unitTests.deployment.pathElement.deploymentID;

import java.rmi.dgc.VMID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extra.gcmdeployment.core.DeploymentID;


public class TestDeploymentID {
    String[] nodes;
    VMID vmid;

    @Before
    public void config() {
        vmid = new VMID();
        nodes = new String[] { "node1", "node2", "node3" };
    }

    @Test
    public void testDeploymentIDConstructor() {
        DeploymentID depID = new DeploymentID(vmid);
        for (String nodeStr : nodes) {
            depID.addDepNode(nodeStr);
        }

        Assert.assertEquals(vmid.toString(), depID.getDeplpoyerVMID());
        for (int i = 0; i < nodes.length; i++) {
            Assert.assertEquals(nodes[i], depID.getDepNode(i));
        }

        DeploymentID depID2 = new DeploymentID(depID.toString());
        Assert.assertEquals(depID, depID2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNodeException() {
        DeploymentID depID = new DeploymentID(vmid);
        depID.addDepNode("toto" + DeploymentID.SEPARATOR + "tata");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorException() {
        new DeploymentID(vmid.toString());
    }
}
