package unitTests.gcmdeployment.virtualnode;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNodeImpl;


public class TestGetCurrentNodes {
    final int COUNT_1 = 50;
    final int COUNT_2 = 100;

    VirtualNodeImpl vn;

    @Before
    public void before() {
        vn = new VirtualNodeImpl();
    }

    @Test
    public void test() {
        for (int i = 0; i < COUNT_1; i++) {
            vn.addNode(new NodeMockup(new Integer(i).toString()));
        }

        Assert.assertEquals(COUNT_1, vn.getCurrentNodes().size());
        Assert.assertEquals(COUNT_1, vn.getNbCurrentNodes());

        for (int i = 0; i < COUNT_2; i++) {
            vn.addNode(new NodeMockup(new Integer(COUNT_1 + i).toString()));
        }

        Assert.assertEquals(COUNT_1 + COUNT_2, vn.getCurrentNodes().size());
        Assert.assertEquals(COUNT_1 + COUNT_2, vn.getNbCurrentNodes());
    }
}
