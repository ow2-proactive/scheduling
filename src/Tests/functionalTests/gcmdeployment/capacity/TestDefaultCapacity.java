package functionalTests.gcmdeployment.capacity;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extra.gcmdeployment.core.StartRuntime;

import functionalTests.FunctionalTest;


public class TestDefaultCapacity extends FunctionalTest {
    @Test
    public void testCapacityAutoDetection() {
        new Thread() {
                public void run() {
                    StartRuntime.main(new String[] {  });
                }
            }.start();

        /* Be sure that the StartRuntime thread has been scheduled
         * Otherwise getCapacity will return -1 due to a race condition
         */
        Thread.yield();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ProActiveRuntimeImpl part = ProActiveRuntimeImpl.getProActiveRuntime();

        long cap = part.getVMInformation().getCapacity();
        long nproc = Runtime.getRuntime().availableProcessors();
        Assert.assertEquals(nproc, cap);
        Assert.assertEquals(nproc, part.getLocalNodes().size());
    }
}
