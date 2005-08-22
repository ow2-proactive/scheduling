package nonregressiontest.runtime.classloader;

import org.objectweb.proactive.ProActive;


/**
 * @author Matthieu Morel
 *
 */
public class A {
    private String name;

    public A() {
    }

    public void createActiveObjectB() throws Exception {
        Object ao = ProActive.newActive("nonregressiontest.runtime.classloader.B",
                new Object[] { "dummy" });
    }
}
