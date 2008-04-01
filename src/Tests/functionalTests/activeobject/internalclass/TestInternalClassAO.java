package functionalTests.activeobject.internalclass;

import java.io.Serializable;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;


/*
 * See PROACTIVE-277
 */
public class TestInternalClassAO {

    @Test
    public void test() throws ActiveObjectCreationException, NodeException {
        AO ao = (AO) PAActiveObject.newActive(AO.class.getName(), new Object[] {});
    }

    public class AO implements Serializable {
        public AO() {
            // Empty
        }
    }
}
