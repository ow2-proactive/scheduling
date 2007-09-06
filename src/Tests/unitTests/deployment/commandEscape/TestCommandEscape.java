package unitTests.deployment.commandEscape;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extra.gcmdeployment.Helpers;
import static unitTests.UnitTests.logger;
public class TestCommandEscape {
    final static String sshLocalhost = "ssh localhost";
    final static String cTrue = "true *";
    final static int vTrue = 0;
    final static String cFalse = "/bin/false \"plop\"";
    final static int vFalse = 1;

    @Test
    public void testCommandEscape() throws IOException, InterruptedException {
        String cmdT = cTrue;
        String cmdF = cFalse;

        logger.debug("Executing: " + cTrue);
        Assert.assertTrue(exec(cTrue) == vTrue);
        logger.debug("Executing: " + cFalse);
        Assert.assertTrue(exec(cFalse) == vFalse);

        cmdT = concat(sshLocalhost, cmdT);
        cmdF = concat(sshLocalhost, cmdF);

        logger.debug("Executing: " + cTrue);
        Assert.assertTrue(exec(cTrue) == vTrue);
        logger.debug("Executing: " + cFalse);
        Assert.assertTrue(exec(cFalse) == vFalse);

        cmdT = concat(sshLocalhost, cmdT);
        cmdF = concat(sshLocalhost, cmdF);

        logger.debug("Executing: " + cTrue);
        Assert.assertTrue(exec(cTrue) == vTrue);
        logger.debug("Executing: " + cFalse);
        Assert.assertTrue(exec(cFalse) == vFalse);

        cmdT = concat(sshLocalhost, cmdT);
        cmdF = concat(sshLocalhost, cmdF);

        logger.debug("Executing: " + cTrue);
        Assert.assertTrue(exec(cTrue) == vTrue);
        logger.debug("Executing: " + cFalse);
        Assert.assertTrue(exec(cFalse) == vFalse);
    }

    static private String concat(String prefixCmd, String cmd) {
        return prefixCmd + " " + Helpers.escapeCommand(cmd);
    }

    static private int exec(String cmd)
        throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        return p.exitValue();
    }
}
