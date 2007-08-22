package unitTests.commandEscape;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extra.gcmdeployment.Helpers;


public class TestCommandEscape {
    final static String sshLocalhost = "ssh localhost";
    final static String cTrue = "true *";
    final static int vTrue = 0;
    final static String cFalse = "\"false\"";
    final static int vFalse = 1;

    @Test
    public void testCommandEscape() throws IOException, InterruptedException {
        String cmdT = cTrue;
        String cmdF = cFalse;

        Assert.assertTrue(exec(cTrue) == vTrue);
        Assert.assertTrue(exec(cTrue) == vTrue);

        cmdT = concat(sshLocalhost, cmdT);
        cmdF = concat(sshLocalhost, cmdF);

        Assert.assertTrue(exec(cTrue) == vTrue);
        Assert.assertTrue(exec(cTrue) == vTrue);

        cmdT = concat(sshLocalhost, cmdT);
        cmdF = concat(sshLocalhost, cmdF);

        Assert.assertTrue(exec(cTrue) == vTrue);
        Assert.assertTrue(exec(cTrue) == vTrue);

        cmdT = concat(sshLocalhost, cmdT);
        cmdF = concat(sshLocalhost, cmdF);

        Assert.assertTrue(exec(cTrue) == vTrue);
        Assert.assertTrue(exec(cTrue) == vTrue);
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
