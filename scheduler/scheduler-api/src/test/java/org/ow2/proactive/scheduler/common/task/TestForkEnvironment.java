/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task;

import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SimpleScript;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Test the ForkEnvironment behavior.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestForkEnvironment {

    private ForkEnvironment forkEnvironment;

    @Before
    public void setup() {
        forkEnvironment = new ForkEnvironment();
    }

    @Test
    public void testInitialValues() {
        Assert.assertEquals(0, forkEnvironment.getSystemEnvironment().size());
        Assert.assertEquals(0, forkEnvironment.getJVMArguments().size());
        Assert.assertEquals(0, forkEnvironment.getAdditionalClasspath().size());
        Assert.assertNull(forkEnvironment.getEnvScript());
    }

    @Test
    public void testJvmArguments() {
        forkEnvironment.addJVMArgument("-Dtoto=tata");
        Assert.assertEquals(1, forkEnvironment.getJVMArguments().size());

        forkEnvironment.addJVMArgument("-Dtoto=tata");
        Assert.assertEquals(2, forkEnvironment.getJVMArguments().size());

        forkEnvironment.addJVMArgument("-Dtoto=titi");
        Assert.assertEquals(3, forkEnvironment.getJVMArguments().size());
        Assert.assertEquals("-Dtoto=tata", forkEnvironment.getJVMArguments().get(0));
        Assert.assertEquals("-Dtoto=tata", forkEnvironment.getJVMArguments().get(1));
        Assert.assertEquals("-Dtoto=titi", forkEnvironment.getJVMArguments().get(2));
        Assert.assertFalse(forkEnvironment.getJVMArguments() == forkEnvironment.getJVMArguments());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullJvmArgument() {
        forkEnvironment.addJVMArgument(null);
    }

    @Test
    public void testAdditionalClasspath() {
        forkEnvironment.addAdditionalClasspath("a");
        Assert.assertEquals(1, forkEnvironment.getAdditionalClasspath().size());

        forkEnvironment.addAdditionalClasspath("b");
        Assert.assertEquals(2, forkEnvironment.getAdditionalClasspath().size());

        forkEnvironment.addAdditionalClasspath("c");
        Assert.assertEquals(3, forkEnvironment.getAdditionalClasspath().size());
        Assert.assertEquals("a", forkEnvironment.getAdditionalClasspath().get(0));
        Assert.assertEquals("b", forkEnvironment.getAdditionalClasspath().get(1));
        Assert.assertEquals("c", forkEnvironment.getAdditionalClasspath().get(2));
        Assert.assertFalse(
                forkEnvironment.getAdditionalClasspath() == forkEnvironment.getAdditionalClasspath());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullAdditionalClasspath() {
        forkEnvironment.addJVMArgument(null);
    }

    @Test
    public void testScript() throws InvalidScriptException {
        forkEnvironment.setEnvScript(new SimpleScript("var a=1;", "js"));
        Assert.assertTrue(forkEnvironment.getEnvScript() != null);
        forkEnvironment.setEnvScript(null);
        Assert.assertTrue(forkEnvironment.getEnvScript() == null);
    }

    @Test
    public void testJavaHome() {
        forkEnvironment.setJavaHome("azerty");
        Assert.assertEquals("azerty", forkEnvironment.getJavaHome());
    }

    @Test
    public void testWorkingDir() {
        forkEnvironment.setWorkingDir("ytreza");
        Assert.assertEquals("ytreza", forkEnvironment.getWorkingDir());
    }

    @Test
    public void testSystemProperties() throws ExecutableCreationException {
        forkEnvironment.addSystemEnvironmentVariable("toto", "ioi");
        Assert.assertEquals(1, forkEnvironment.getSystemEnvironment().size());
        Assert.assertEquals("ioi", forkEnvironment.getSystemEnvironmentVariable("toto"));

        forkEnvironment.addSystemEnvironmentVariable("toto", "oio");
        Assert.assertEquals(1, forkEnvironment.getSystemEnvironment().size());
        Assert.assertEquals("oio", forkEnvironment.getSystemEnvironmentVariable("toto"));

        forkEnvironment.addSystemEnvironmentVariable("toto", "123");
        Assert.assertEquals(1, forkEnvironment.getSystemEnvironment().size());
        Assert.assertEquals("123", forkEnvironment.getSystemEnvironmentVariable("toto"));

        forkEnvironment.addSystemEnvironmentVariable("tata", "456");
        Assert.assertEquals(2, forkEnvironment.getSystemEnvironment().size());
        Assert.assertFalse(forkEnvironment.getSystemEnvironment() == forkEnvironment.getSystemEnvironment());

        ForkEnvironment internalForkEnvironment = new InternalForkEnvironment(forkEnvironment);
        Assert.assertEquals(2, internalForkEnvironment.getSystemEnvironment().size());
        Assert.assertEquals("456", internalForkEnvironment.getSystemEnvironmentVariable("tata"));
        Assert.assertEquals(null, internalForkEnvironment.getSystemEnvironmentVariable("titi"));
        Assert.assertEquals("123", internalForkEnvironment.getSystemEnvironmentVariable("toto"));

        try {
            internalForkEnvironment.addSystemEnvironmentVariable("toto", null);
            forkEnvironment.addSystemEnvironmentVariable(null, "tata");
            throw new RuntimeException(
                    "forkEnvironment.addSystemEnvironmentVariable(null,value) did not throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testEquality() throws ExecutableCreationException {
        InternalForkEnvironment internalForkEnvironment = new InternalForkEnvironment(forkEnvironment);
        Assert.assertEquals(forkEnvironment.getAdditionalClasspath(),
                internalForkEnvironment.getAdditionalClasspath());
        Assert.assertEquals(forkEnvironment.getJavaHome(), internalForkEnvironment.getJavaHome());
        Assert.assertEquals(forkEnvironment.getSystemEnvironment(),
                internalForkEnvironment.getSystemEnvironment());
        Assert.assertEquals(forkEnvironment.getWorkingDir(), internalForkEnvironment.getWorkingDir());
        Assert.assertEquals(forkEnvironment.getEnvScript(), internalForkEnvironment.getEnvScript());
        Assert.assertEquals(forkEnvironment.getJVMArguments(), internalForkEnvironment.getJVMArguments());
    }

}
