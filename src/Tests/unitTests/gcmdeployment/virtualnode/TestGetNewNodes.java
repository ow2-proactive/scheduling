package unitTests.gcmdeployment.virtualnode;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNodeImpl;


public class TestGetNewNodes {
    final int COUNT_1 = 50;
    final int COUNT_2 = 100;

    GCMVirtualNodeImpl vn;

    @Before
    public void before() {
        vn = new GCMVirtualNodeImpl();
    }

    @Test
    public void test() {
        for (int i = 0; i < COUNT_1; i++) {
            vn.addNode(new NodeMockup(new Integer(i).toString()));
        }

        Assert.assertEquals(COUNT_1, vn.getNewNodes().size());
        Assert.assertEquals(0, vn.getNewNodes().size());

        for (int i = 0; i < COUNT_2; i++) {
            vn.addNode(new NodeMockup(new Integer(COUNT_1 + i).toString()));
        }

        Assert.assertEquals(COUNT_2, vn.getNewNodes().size());
        Assert.assertEquals(0, vn.getNewNodes().size());
    }
}
