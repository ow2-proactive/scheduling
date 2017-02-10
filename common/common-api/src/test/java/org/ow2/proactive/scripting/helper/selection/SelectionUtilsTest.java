/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scripting.helper.selection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


public class SelectionUtilsTest {

    private final URL propertyFileURL = getClass().getResource("scripts/SampleCheckProperties");

    @Test
    public void checkExec() {
        assertFalse(SelectionUtils.checkExec(null));
        assertFalse(SelectionUtils.checkExec(""));
        String path = System.getenv("PATH");
        String[] tokens = path.split(File.pathSeparator);
        //Find first PATH entry that contains an executable
        //If no exec is found, test is considered as passed ! (this case should never happen)
        for (String s : tokens) {
            File dir = new File(s);
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                assertTrue(SelectionUtils.checkExec(files[0].getName()));
                break;
            }
        }
    }

    @Test
    public void checkFreeMemory() {
        assertFalse(SelectionUtils.checkFreeMemory(Runtime.getRuntime().freeMemory() + 1));
        assertTrue(SelectionUtils.checkFreeMemory(Runtime.getRuntime().freeMemory()));
        assertTrue(SelectionUtils.checkFreeMemory(Runtime.getRuntime().freeMemory() - 1));
    }

    @Test
    public void checkJavaProperty() {
        assertFalse(SelectionUtils.checkJavaProperty(null, null));
        assertFalse(SelectionUtils.checkJavaProperty("foo", null));
        assertFalse(SelectionUtils.checkJavaProperty(null, "bar"));
        assertFalse(SelectionUtils.checkJavaProperty("foo", "bar"));
        assertFalse(SelectionUtils.checkJavaProperty("java.home", "bar"));
        assertTrue(SelectionUtils.checkJavaProperty("java.vm.version", System.getProperty("java.vm" + ".version")));
        assertTrue(SelectionUtils.checkJavaProperty("java.home", ".*"));
    }

    @Test
    public void checkOSVersion() {
        assertFalse(SelectionUtils.checkOSVersion(null));
        assertTrue(SelectionUtils.checkOSVersion(System.getProperty("os.version")));
    }

    @Test
    public void checkOSArch() {
        assertFalse(SelectionUtils.checkOSArch(null));
        assertTrue(SelectionUtils.checkOSArch(System.getProperty("os.arch")));
        assertTrue(SelectionUtils.checkOSArch(System.getProperty("os.arch").toUpperCase()));
        assertTrue(SelectionUtils.checkOSArch("6"));
    }

    @Test
    public void checkOSName() {
        String osNameRegexPattern = Pattern.quote(System.getProperty("os.name"));
        assertFalse(SelectionUtils.checkOSName(null));
        assertFalse(SelectionUtils.checkOSName("123"));
        assertTrue(SelectionUtils.checkOSName(osNameRegexPattern));
        assertTrue(SelectionUtils.checkOSName(osNameRegexPattern.toUpperCase()));
    }

    @Test
    public void rubyEvaluation() throws URISyntaxException {
        String scriptPath;
        scriptPath = new File(getClass().getResource("scripts/checkProperties.rb").toURI()).getAbsolutePath();
        Assert.assertEquals("selected=true",
                            EngineScript.EvalScript(scriptPath, EngineScript.Language.ruby, resolve(propertyFileURL)));
    }

    @Test
    public void pythonEvaluation() throws URISyntaxException {
        String scriptPath;
        scriptPath = new File(getClass().getResource("scripts/checkProperties.py").toURI()).getAbsolutePath();
        Assert.assertEquals("selected=true",
                            EngineScript.EvalScript(scriptPath,
                                                    EngineScript.Language.python,
                                                    resolve(propertyFileURL)));
    }

    @Test
    public void jsEvaluation() throws URISyntaxException {
        String scriptPath;
        scriptPath = new File(getClass().getResource("scripts/checkProperties.js").toURI()).getAbsolutePath();
        Assert.assertEquals("selected=true",
                            EngineScript.EvalScript(scriptPath,
                                                    EngineScript.Language.javascript,
                                                    resolve(propertyFileURL)));
    }

    private String resolve(URL url) throws URISyntaxException {
        return new File(url.toURI()).getAbsolutePath();
    }
}
