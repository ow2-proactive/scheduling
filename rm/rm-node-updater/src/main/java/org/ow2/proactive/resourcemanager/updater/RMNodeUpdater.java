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
package org.ow2.proactive.resourcemanager.updater;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 *
 * A wrapper class to use with agents.
 * It either detects that node.jar must be updated, then downloads it start a node.
 * When an agent detects it and restart the node it just proxies the request to RMNodeStarer.
 *
 */
public class RMNodeUpdater {

    /**
     * Url used to download node.jar
     */
    private static final String NODE_URL_PROPERTY = "node.jar.url";

    /**
     * Local path where the node jar should be stored
     */
    private static final String NODE_JAR_SAVEAS_PROPERTY = "node.jar.saveas";

    /**
     * Default name of the local node jar
     */
    private static final String DEFAULT_SCHEDULER_NODE_JAR = "node.jar";

    /**
     * optional One-Jar property, path used to expand librairies
     */
    private static final String ONEJAR_EXPAND_DIR_PROPERTY = "one-jar.expand.dir";

    /**
     * Java Property prefix used to enter extra java command line options
     * For example -Xmn256m can be configured by using -DXtraOption1=Xmn256m
     */
    private static final String XTRA_OPTION = "XtraOption";

    private static boolean isLocalJarUpToDate(String url, String filePath) {

        try {
            URLConnection urlConnection = new URL(url).openConnection();
            File file = new File(filePath);

            System.out.println("Url date=" + new Date(urlConnection.getLastModified()));
            System.out.println("File date=" + new Date(file.lastModified()));

            if (!file.exists() || file.lastModified() < urlConnection.getLastModified()) {
                System.out.println("Local jar " + file + " is obsolete or not present");
            } else {
                System.out.println("Local jar " + file + " is up to date");
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean makeNodeUpToDate() {
        if (System.getProperty(NODE_URL_PROPERTY) != null) {

            String jarUrl = System.getProperty(NODE_URL_PROPERTY);
            String jarFile = DEFAULT_SCHEDULER_NODE_JAR;

            if (System.getProperty(NODE_JAR_SAVEAS_PROPERTY) != null) {
                jarFile = System.getProperty(NODE_JAR_SAVEAS_PROPERTY);
            }

            if (!isLocalJarUpToDate(jarUrl, jarFile)) {
                System.out.println("Downloading node.jar from " + jarUrl + " to " + jarFile);

                try {
                    File destination = new File(jarFile);
                    File lockFile = null;
                    FileLock lock = null;

                    if (destination.exists()) {

                        lockFile = new File(StandardSystemProperty.JAVA_IO_TMPDIR.value(), "lock");
                        if (!lockFile.exists()) {
                            lockFile.createNewFile();
                        }

                        System.out.println("Getting the lock on " + lockFile.getAbsoluteFile());
                        FileChannel channel = new RandomAccessFile(lockFile, "rw").getChannel();
                        lock = channel.lock();

                        if (isLocalJarUpToDate(jarUrl, jarFile)) {
                            System.out.println("Another process downloaded node.jar - don't do it anymore");
                            System.out.println("Releasing the lock on " + lockFile.getAbsoluteFile());
                            lock.release();
                            channel.close();

                            return false;
                        }
                    }

                    FileUtils.copyURLToFile(new URL(jarUrl), destination);
                    System.out.println("Download finished");

                    cleanExpandDirectory(jarFile);

                    if (lock != null && lockFile != null) {
                        System.out.println("Releasing the lock on " + lockFile.getAbsoluteFile());
                        lock.release();
                    }
                    return true;

                } catch (Exception e) {
                    System.err.println("Cannot download node.jar from " + jarUrl);
                    e.printStackTrace();
                    return false;
                }
            } else {
                return true;
            }
        } else {
            throw new IllegalArgumentException("No java property " + NODE_URL_PROPERTY +
                                               " specified. This property must be set when using " +
                                               RMNodeUpdater.class.getSimpleName());
        }
    }

    private static void cleanExpandDirectory(String jarFile) throws IOException {
        File directoryToClean;
        String oneJarExpandDir = System.getProperty(ONEJAR_EXPAND_DIR_PROPERTY);
        if (oneJarExpandDir == null) {
            // Default scheme used by one-jar
            String jar = new File(jarFile).getName().replaceFirst("\\.[^\\.]*$", "");
            directoryToClean = new File(StandardSystemProperty.JAVA_IO_TMPDIR.value(), jar);
        } else {
            directoryToClean = new File(oneJarExpandDir);
        }

        FileUtils.deleteQuietly(directoryToClean);
    }

    public static void main(String[] args) throws Exception {
        while (!makeNodeUpToDate()) {
            Thread.sleep(5000);
        }
        String jarFile = DEFAULT_SCHEDULER_NODE_JAR;

        if (System.getProperty(NODE_JAR_SAVEAS_PROPERTY) != null) {
            jarFile = System.getProperty(NODE_JAR_SAVEAS_PROPERTY);
        }

        System.out.println("Launching a computing node");
        ProcessBuilder pb = generateSubProcess(args, jarFile);

        final Process p = pb.start();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    p.destroy();
                } catch (Exception e) {
                    // ignore
                }
            }
        }));
        p.waitFor();
    }

    private static ProcessBuilder generateSubProcess(String[] args, String jarFile) {
        ProcessBuilder pb;
        List<String> command = new ArrayList<>();
        if (StandardSystemProperty.OS_NAME.value().toLowerCase().contains("windows")) {
            command.add((new File(StandardSystemProperty.JAVA_HOME.value(), "bin/java.exe")).getAbsolutePath());
        } else {
            command.add((new File(StandardSystemProperty.JAVA_HOME.value(), "bin/java")).getAbsolutePath());
        }
        command.addAll(extractUserSystemPropertiesFromCurrentJVM());
        command.add("-jar");
        command.add(jarFile);
        command.addAll(Lists.newArrayList(args));
        pb = new ProcessBuilder(command);
        pb.inheritIO();
        if (pb.environment().containsKey("CLASSPATH")) {
            pb.environment().remove("CLASSPATH");
        }
        return pb;
    }

    private static List<String> extractUserSystemPropertiesFromCurrentJVM() {
        ArrayList<String> commandLineProperties = new ArrayList<>();

        Set<String> standardPropertySet = Sets.union(allSystemProperties(), allInternalProperties());

        for (String propertyName : System.getProperties().stringPropertyNames()) {
            if (!standardPropertySet.contains(propertyName)) {
                commandLineProperties.add("-D" + propertyName + "=" + System.getProperty(propertyName));
            }
        }
        commandLineProperties.addAll(allNonStandardXOptionsConvertedToProperties());
        return commandLineProperties;
    }

    private static Set<String> allSystemProperties() {
        Set<String> standardPropertySet = new HashSet<>();
        for (StandardSystemProperty stdProperty : StandardSystemProperty.values()) {
            if (stdProperty != StandardSystemProperty.JAVA_IO_TMPDIR &&
                stdProperty != StandardSystemProperty.JAVA_LIBRARY_PATH) {
                // tmp dir and java library path can be overridden by user
                standardPropertySet.add(stdProperty.key());
            }
        }
        return standardPropertySet;
    }

    private static Set<String> allInternalProperties() {
        Set<String> internalPropertySet = new HashSet<>();
        for (String propertyName : System.getProperties().stringPropertyNames()) {
            if (propertyName.startsWith("sun.") || (propertyName.startsWith(XTRA_OPTION))) {
                internalPropertySet.add(propertyName);
            } else {
                switch (propertyName) {
                    case "file.encoding.pkg":
                    case "user.script":
                    case "user.country":
                    case "java.runtime.version":
                    case "java.awt.graphicsenv":
                    case "java.endorsed.dirs":
                    case "user.variant":
                    case "user.timezone":
                    case "java.runtime.name":
                    case "java.vendor.url.bug":
                    case "java.security.manager":
                    case "java.awt.printerjob":
                    case "awt.toolkit":
                    case "java.vm.info":
                        internalPropertySet.add(propertyName);
                        break;
                    default:
                        // do nothing
                }

            }
        }
        return internalPropertySet;
    }

    private static List<String> allNonStandardXOptionsConvertedToProperties() {
        List<String> xOptions = new ArrayList<>();
        for (String propertyName : System.getProperties().stringPropertyNames()) {
            if (propertyName.startsWith(XTRA_OPTION)) {
                xOptions.add("-" + System.getProperty(propertyName));
            }
        }
        return xOptions;
    }

}
