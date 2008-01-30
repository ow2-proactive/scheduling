package unitTests.gcmdeployment.virtualnode;

import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNodeImpl;


public class TestWaitReady {
    static final int TIMEOUT = 1000;

    GCMVirtualNodeImpl vn;

    @Before
    public void before() {
        vn = new GCMVirtualNodeImpl();
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
            vn.addNode(new NodeMockup(i));
        }
        vn.waitReady(TIMEOUT);
    }
}
