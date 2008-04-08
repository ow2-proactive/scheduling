package unitTests.gcmdeployment.virtualnode;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.FakeNode;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeImpl;


public class TestGetNewNodes {
    final int COUNT_1 = 50;
    final int COUNT_2 = 100;

    GCMVirtualNodeImpl vn;
    GCMApplicationDescriptorMockup gcma;
    ProActiveRuntimeImpl part;

    @Before
    public void before() {
        vn = new GCMVirtualNodeImpl();
        gcma = new GCMApplicationDescriptorMockup();
        part = ProActiveRuntimeImpl.getProActiveRuntime();
        part.setCapacity(COUNT_1 + COUNT_2);
    }

    @Test
    public void test() {
        for (int i = 0; i < COUNT_1; i++) {
            vn.addNode(new FakeNode(gcma, part));
        }

        Assert.assertEquals(COUNT_1, vn.getNewNodes().size());
        Assert.assertEquals(0, vn.getNewNodes().size());

        for (int i = 0; i < COUNT_2; i++) {
            vn.addNode(new FakeNode(gcma, part));
        }

        Assert.assertEquals(COUNT_2, vn.getNewNodes().size());
        Assert.assertEquals(0, vn.getNewNodes().size());
    }
}
