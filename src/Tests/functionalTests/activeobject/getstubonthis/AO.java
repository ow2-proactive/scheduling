package functionalTests.activeobject.getstubonthis;

import java.io.Serializable;

import org.junit.Assert;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.mop.StubObject;


public class AO implements Serializable, InitActive {
    private boolean ok = false;

    public AO() {
    }

    public void initActivity(Body body) {
        StubObject stub = PAActiveObject.getStubOnThis();
        Assert.assertNotNull(stub);
        ok = true;
    }

    public boolean isOk() {
        return ok;
    }
}