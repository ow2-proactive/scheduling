package org.ow2.proactive.scheduler.ext.matsci.worker.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;


/**
 * MatSciConfigurationParser
 *
 * @author The ProActive Team
 */
public class MatSciConfigurationParser {

    protected static File findSchedulerHome() throws IOException, URISyntaxException {
        File home = null;
        Class cz = MatSciConfigurationParser.class;
        URL res = cz.getResource("/org/ow2/proactive/scheduler");
        if (res == null)
            throw new IllegalStateException("Can't find resource /org/ow2/proactive/scheduler");
        URLConnection conn = res.openConnection();
        if (conn instanceof JarURLConnection) {
            URL jarFileURL = ((JarURLConnection) conn).getJarFileURL();
            File jarFile = new File(jarFileURL.toURI());
            home = jarFile.getParentFile().getParentFile().getParentFile();
        } else {
            File ext = new File(res.toURI());
            home = ext.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile()
                    .getParentFile();
        }
        return home;
    }

    protected static void checkDir(File dir, File conf) throws Exception {
        if (!dir.exists()) {
            throw new IOException("In " + conf + ", " + dir + " doesn't exist");
        }
        if (!dir.isDirectory()) {
            throw new IOException("In " + conf + ", " + dir + " is not a directory");
        }
        if (!dir.canRead()) {
            throw new IOException("In " + conf + ", " + dir + " is not readable");
        }
    }

    protected static void checkFile(File file, File conf, boolean executable) throws Exception {
        if (!file.exists()) {
            throw new IOException("In " + conf + ", " + file + " doesn't exist");
        }
        if (!file.isFile()) {
            throw new IOException("In " + conf + ", " + file + " is not a file");
        }
        if (!file.canRead()) {
            throw new IOException("In " + conf + ", " + file + " is not readable");
        }
        if (executable && !file.canRead()) {
            throw new IOException("In " + conf + ", " + file + " is not executable");
        }
    }
}
