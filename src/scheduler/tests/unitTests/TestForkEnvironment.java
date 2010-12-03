/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package unitTests;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.task.launcher.InternalForkEnvironment;
import org.ow2.proactive.scripting.SimpleScript;


/**
 * Test the forkeEnvironment behavior.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestForkEnvironment {

    @Test
    public void run() throws Throwable {
        ForkEnvironment fe = new ForkEnvironment();
        //test initial values
        Assert.assertEquals(0, fe.getSystemProperties().size());
        Assert.assertEquals(0, fe.getJVMArguments().size());
        Assert.assertEquals(0, fe.getAdditionalClasspath().size());
        Assert.assertNull(fe.getEnvScript());
        //test system properties
        fe.addSystemProperty("toto", "ioi", true);
        Assert.assertEquals(1, fe.getSystemProperties().size());
        Assert.assertEquals("ioi", fe.getSystemProperty("toto"));
        fe.addSystemProperty("toto", "oio", true);
        Assert.assertEquals(1, fe.getSystemProperties().size());
        Assert.assertEquals("ioioio", fe.getSystemProperty("toto"));
        fe.addSystemProperty("toto", "123", false);
        Assert.assertEquals(1, fe.getSystemProperties().size());
        Assert.assertEquals("123", fe.getSystemProperty("toto"));
        fe.addSystemProperty("tata", "456", false);
        Assert.assertEquals(2, fe.getSystemProperties().size());
        Assert.assertFalse(fe.getSystemProperties() == fe.getSystemProperties());
        fe.addSystemProperty("tata", "789", '#');
        Assert.assertEquals(2, fe.getSystemProperties().size());
        Assert.assertEquals("456#789", fe.getSystemProperty("tata"));
        Map<String, String> baseEnv = new HashMap<String, String>();
        baseEnv.put("tata", "123");
        baseEnv.put("titi", "#@");
        baseEnv.put("toto", "eue");
        ForkEnvironment ife = new InternalForkEnvironment(fe, baseEnv);
        Assert.assertEquals(3, ife.getSystemProperties().size());
        Assert.assertEquals("456#789", ife.getSystemProperty("tata"));
        Assert.assertEquals("#@", ife.getSystemProperty("titi"));
        Assert.assertEquals("123", ife.getSystemProperty("toto"));
        ife.addSystemProperty("titi", "@#", ':');
        Assert.assertEquals("#@:@#", ife.getSystemProperty("titi"));
        try {
            ife.addSystemProperty("toto", null, false);
            fe.addSystemProperty(null, "tata", false);
            throw new RuntimeException(
                "fe.addSystemProperty(null,value) did not throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        //test jvm arguments
        fe.addJVMArgument("-Dtoto=tata");
        Assert.assertEquals(1, fe.getJVMArguments().size());
        fe.addJVMArgument("-Dtoto=tata");
        Assert.assertEquals(2, fe.getJVMArguments().size());
        fe.addJVMArgument("-Dtoto=titi");
        Assert.assertEquals(3, fe.getJVMArguments().size());
        Assert.assertEquals("-Dtoto=tata", fe.getJVMArguments().get(0));
        Assert.assertEquals("-Dtoto=tata", fe.getJVMArguments().get(1));
        Assert.assertEquals("-Dtoto=titi", fe.getJVMArguments().get(2));
        Assert.assertFalse(fe.getJVMArguments() == fe.getJVMArguments());
        try {
            fe.addJVMArgument(null);
            throw new RuntimeException("fe.addJVMArgument(null) did not throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        //test additional classpath
        fe.addAdditionalClasspath("a");
        Assert.assertEquals(1, fe.getAdditionalClasspath().size());
        fe.addAdditionalClasspath("b");
        Assert.assertEquals(2, fe.getAdditionalClasspath().size());
        fe.addAdditionalClasspath("c");
        Assert.assertEquals(3, fe.getAdditionalClasspath().size());
        Assert.assertEquals("a", fe.getAdditionalClasspath().get(0));
        Assert.assertEquals("b", fe.getAdditionalClasspath().get(1));
        Assert.assertEquals("c", fe.getAdditionalClasspath().get(2));
        Assert.assertFalse(fe.getAdditionalClasspath() == fe.getAdditionalClasspath());
        try {
            fe.addAdditionalClasspath(null);
            throw new RuntimeException(
                "fe.addAdditionalClasspath(null) did not throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        //test script
        fe.setEnvScript(new SimpleScript("var a=1;", "js"));
        Assert.assertTrue(fe.getEnvScript() != null);
        fe.setEnvScript(null);
        Assert.assertTrue(fe.getEnvScript() == null);
        //test java home
        fe.setJavaHome("azerty");
        Assert.assertEquals("azerty", fe.getJavaHome());
        //test working dir
        fe.setWorkingDir("ytreza");
        Assert.assertEquals("ytreza", fe.getWorkingDir());
        //test equality
        ife = new InternalForkEnvironment(fe, null);
        Assert.assertEquals(fe.getAdditionalClasspath(), ife.getAdditionalClasspath());
        Assert.assertEquals(fe.getJavaHome(), ife.getJavaHome());
        Assert.assertEquals(fe.getSystemProperties(), ife.getSystemProperties());
        Assert.assertEquals(fe.getWorkingDir(), ife.getWorkingDir());
        Assert.assertEquals(fe.getEnvScript(), ife.getEnvScript());
        Assert.assertEquals(fe.getJVMArguments(), ife.getJVMArguments());

    }

}
