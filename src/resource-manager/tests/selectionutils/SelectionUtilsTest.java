/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package selectionutils;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.scripting.helper.selection.SelectionUtils;


/**
 * SelectionUtilsTest will test the different behavior of the selectionUtils class
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class SelectionUtilsTest {

    @Test
    public void run() throws Throwable {
        String scriptPath;
        String propertyPath = new File(getClass().getResource("scripts/SampleCheckProperties").getFile())
                .getAbsolutePath();

        log("Test javaScript evaluation");
        scriptPath = new File(getClass().getResource("scripts/checkProperties.js").getFile())
                .getAbsolutePath();
        Assert.assertEquals("selected=true", EngineScript.EvalScript(scriptPath,
                EngineScript.Language.javascript, propertyPath));

        log("Test python evaluation");
        scriptPath = new File(getClass().getResource("scripts/checkProperties.py").getFile())
                .getAbsolutePath();
        Assert.assertEquals("selected=1", EngineScript.EvalScript(scriptPath, EngineScript.Language.python,
                propertyPath));

        log("Test ruby evaluation");
        scriptPath = new File(getClass().getResource("scripts/checkProperties.rb").getFile())
                .getAbsolutePath();
        Assert.assertEquals("selected=true", EngineScript.EvalScript(scriptPath, EngineScript.Language.ruby,
                propertyPath));

        log("checkOSName");
        Assert.assertFalse(SelectionUtils.checkOSName(null));
        Assert.assertFalse(SelectionUtils.checkOSName("123"));
        Assert.assertTrue(SelectionUtils.checkOSName(System.getProperty("os.name")));
        Assert.assertTrue(SelectionUtils.checkOSName(System.getProperty("os.name").toUpperCase()));

        log("checkOSArch");
        Assert.assertFalse(SelectionUtils.checkOSArch(null));
        Assert.assertTrue(SelectionUtils.checkOSArch(System.getProperty("os.arch")));
        Assert.assertTrue(SelectionUtils.checkOSArch(System.getProperty("os.arch").toUpperCase()));
        Assert.assertTrue(SelectionUtils.checkOSArch("6"));

        log("checkOSVersion");
        Assert.assertFalse(SelectionUtils.checkOSVersion(null));
        Assert.assertFalse(SelectionUtils.checkOSVersion("1.6"));
        Assert.assertTrue(SelectionUtils.checkOSVersion(System.getProperty("os.version")));

        log("checkJavaProperty");
        Assert.assertFalse(SelectionUtils.checkJavaProperty(null, null));
        Assert.assertFalse(SelectionUtils.checkJavaProperty("foo", null));
        Assert.assertFalse(SelectionUtils.checkJavaProperty(null, "bar"));
        Assert.assertFalse(SelectionUtils.checkJavaProperty("foo", "bar"));
        Assert.assertFalse(SelectionUtils.checkJavaProperty("java.home", "bar"));
        Assert.assertTrue(SelectionUtils.checkJavaProperty("java.home", System.getProperty("java.home")));
        Assert.assertTrue(SelectionUtils.checkJavaProperty("java.home", ".*"));

        log("checkFreeMemory");
        Assert.assertFalse(SelectionUtils.checkFreeMemory(Runtime.getRuntime().freeMemory() + 1));
        Assert.assertTrue(SelectionUtils.checkFreeMemory(Runtime.getRuntime().freeMemory()));
        Assert.assertTrue(SelectionUtils.checkFreeMemory(Runtime.getRuntime().freeMemory() - 1));

        log("checkExec");
        String path = System.getenv("PATH");
        String[] tokens = path.split(File.pathSeparator);
        File dir = new File(tokens[0]);
        File[] files = dir.listFiles();
        String nameOfExistingFile = files[0].getName();
        Assert.assertFalse(SelectionUtils.checkExec(null));
        Assert.assertFalse(SelectionUtils.checkExec(""));
        Assert.assertTrue(SelectionUtils.checkExec(nameOfExistingFile));
    }

    private void log(String s) {
        System.out.println("------------------------------ " + s);
    }

}
