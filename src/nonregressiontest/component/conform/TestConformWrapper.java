/**
 * This class allows to launch JUnit tests. It is usefull for Fractal conform tests, and avoid to rewrite it.
 */
package nonregressiontest.component.conform;

import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import nonregressiontest.component.ComponentTest;

import testsuite.test.FunctionalTest;


/**
 * @author cdalmass
 *
 */
public class TestConformWrapper extends ComponentTest {
    static Result r;
    private boolean success = false;

    public TestConformWrapper() {
        super("Fractal conform tests", "Fractal conform tests");
    }

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
    public void action() throws Exception {
        JUnitCore ju = new org.junit.runner.JUnitCore();
        Class[] testsClass = new Class[] { TestTypeFactory.class //, 
                                                                 //TestContentController.class
             };

        for (Class currentTestClass : testsClass) {
            r = ju.run(currentTestClass);
            if (r.wasSuccessful()) {
                success = true;
            } else {
                System.out.println("Run : " + currentTestClass.getSimpleName());
                System.out.println("There are " + r.getFailureCount() +
                    " failure(s) :");
                List<Failure> failures = r.getFailures();
                for (Failure failure : failures) {
                    System.out.println("Test " + failure.getTestHeader() +
                        " failed because of " + failure.getMessage());
                    //System.err.println("Description : " + failure.getDescription());
                    System.out.println("Trace : " + failure.getTrace());
                }
                System.out.println(r.getRunCount() + " test run in " +
                    r.getRunTime() + "ms; with " + r.getFailureCount() +
                    " failure(s).");
            }
        }
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#endTest()
     */
    @Override
    public void endTest() throws Exception {
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Override
    public void initTest() throws Exception {
    }

    public boolean postConditions() throws Exception {
        return success;
    }

    public static void main(String[] args) {
        FunctionalTest test = new TestConformWrapper();
        try {
            test.initTest();
            test.action();
            if (test.postConditions()) {
                System.out.println("TEST SUCCEEDED");
            } else {
                System.err.println("TEST FAILED");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                test.endTest();
                System.exit(0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
