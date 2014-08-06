package org.ow2.proactive_grid_cloud_portal.cli.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.SetDebugModeCommand;

public class ExceptionUtility {

    private ExceptionUtility() {
    }

    public static String stackTraceAsString(Throwable error) {
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);
        error.printStackTrace(writer);
        writer.flush();
        return out.toString();
    }

    public static boolean debugMode(ApplicationContext currentContext) {
        Boolean debug = currentContext.getProperty(SetDebugModeCommand.PROP_DEBUG_MODE, Boolean.class);
        return (debug == null) ? false : debug;
    }
}
