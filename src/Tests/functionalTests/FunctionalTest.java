package functionalTests;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;


public class FunctionalTest {
    protected Logger logger = Logger.getLogger("testsuite");

    /**
     * Kill all ProActive runtimes
     */
    @BeforeClass
    @AfterClass
    public static void killProActive() {
        Helper.killJVMs();
    }
}
