package nonregressiontest.component;

import testsuite.test.FunctionalTest;

/**
 * @author Matthieu Morel
 *
 */
public abstract class ComponentTest extends FunctionalTest {

    /**
     * 
     */
    public ComponentTest() {
        super();
        // TODO Auto-generated constructor stub
    }
    /**
     * @param name
     */
    public ComponentTest(String name) {
        super("Components : " + name);
        // TODO Auto-generated constructor stub
    }
    /**
     * @param name
     * @param description
     */
    public ComponentTest(String name, String description) {
        super("Components : " + name, description);
        // TODO Auto-generated constructor stub
    }
    public boolean preConditions() throws Exception {
        if (!"enable".equals(System.getProperty("proactive.future.ac"))) {
            throw new Exception("The components framework needs the automatic continuations (system property 'proactive.future.ac' set to 'enable') to be operative");
        }
        return true;
    }
}
