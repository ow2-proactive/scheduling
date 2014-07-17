package org.ow2.proactive.scheduler.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class GroovySchedulerStarter {
    public static void main(String[] args) throws IOException {
        GroovyShell shell = getGroovyShell(args);
        String script = getStartupScript();
        shell.evaluate(script);
    }

    private static String getStartupScript() throws IOException {
        return FileUtils.readFileToString(getStartupScriptFile());
    }

    private static File getStartupScriptFile() {
        return new File(getSchedulerDir(), "tools/startup.groovy");
    }

    public static String getSchedulerDir() {
        String jarPath = GroovySchedulerStarter.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return new File(jarPath).getParentFile().getParentFile().getParent();
    }

    private static GroovyShell getGroovyShell(String[] args) {
        Binding binding = new Binding();
        binding.setVariable("args", args);
        return new GroovyShell(binding);
    }
}
