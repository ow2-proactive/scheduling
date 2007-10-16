/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
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
