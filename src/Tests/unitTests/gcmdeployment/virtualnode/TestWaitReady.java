package unitTests.gcmdeployment.virtualnode;

import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.FakeNode;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeImpl;


public class TestWaitReady {
    static final int TIMEOUT = 1000;

    GCMVirtualNodeImpl vn;
    GCMApplicationDescriptorMockup gcma;
    ProActiveRuntimeImpl part;

    @BeforeClass
    static public void setCapacity() {
        ProActiveRuntimeImpl.getProActiveRuntime().setCapacity(5);
    }

    @Before
    public void before() {
        vn = new GCMVirtualNodeImpl();
        gcma = new GCMApplicationDescriptorMockup();
        part = ProActiveRuntimeImpl.getProActiveRuntime();
    }

    @Test(expected = TimeoutException.class)
    public void timeoutReached() throws TimeoutException {
        vn.setCapacity(5);
        vn.waitReady(TIMEOUT);
    }

    @Test
    public void everythingOK() throws TimeoutException {
        vn.setCapacity(5);
        for (int i = 0; i < 5; i++) {
            vn.addNode(new FakeNode(gcma, part));
        }
        vn.waitReady(TIMEOUT);
    }
}
