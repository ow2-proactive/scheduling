package org.ow2.proactive.scripting.helper.selection;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


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
        assertTrue(SelectionUtils.checkJavaProperty("java.vm.version", System.getProperty("java.vm"
            + ".version")));
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
                EngineScript.EvalScript(scriptPath, EngineScript.Language.python, resolve(propertyFileURL)));
    }

    @Test
    public void jsEvaluation() throws URISyntaxException {
        String scriptPath;
        scriptPath = new File(getClass().getResource("scripts/checkProperties.js").toURI()).getAbsolutePath();
        Assert.assertEquals("selected=true", EngineScript.EvalScript(scriptPath,
                EngineScript.Language.javascript, resolve(propertyFileURL)));
    }

    private String resolve(URL url) throws URISyntaxException {
        return new File(url.toURI()).getAbsolutePath();
    }
}