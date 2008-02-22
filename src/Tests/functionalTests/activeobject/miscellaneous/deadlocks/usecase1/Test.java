package functionalTests.activeobject.miscellaneous.deadlocks.usecase1;

import org.objectweb.proactive.api.PAActiveObject;

import functionalTests.FunctionalTest;


/**
 * 
 * Tests that the request queue is not locked while serving a method through a serveAll-like call.
 * See JIRA: PROACTIVE-
 *
 */
public class Test extends FunctionalTest {

    @org.junit.Test
    public void action() throws Exception {
        AODeadlock2 ao2 = (AODeadlock2) PAActiveObject.newActive(AODeadlock2.class.getName(), new Object[0]);
        AODeadlock1 ao1 = (AODeadlock1) PAActiveObject.newActive(AODeadlock1.class.getName(),
                new Object[] { ao2 });
        ao2.setAODeadlock1(ao1);

        int iw = ao1.foo();
        System.out.println(iw);
    }
}
