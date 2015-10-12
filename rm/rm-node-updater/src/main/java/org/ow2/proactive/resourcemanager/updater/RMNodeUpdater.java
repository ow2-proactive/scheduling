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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.updater;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Date;


/**
 *
 * A wrapper class to use with agents.
 * It either detects that node.jar must be updated, then downloads it start a node.
 * When an agent detects it and restart the node it just proxies the request to RMNodeStarer.
 *
 */
public class RMNodeUpdater {

    private static final String NODE_URL_PROPERTY = "node.jar.url";
    private static final String NODE_JAR_PROPERTY = "node.jar.saveas";
    private static final String TMP_DIR_PROPERTY = "java.io.tmpdir";
    public static final String ONE_JAR_JAR_PATH = "one-jar.jar.path";
    public static final String SCHEDULER_NODE_JAR = "scheduler-node.jar";

    private static boolean isLocalJarUpToDate(String url, String filePath) {

        try {
            URLConnection urlConnection = new URL(url).openConnection();
            File file = new File(filePath);

            System.out.println("Url date=" + new Date(urlConnection.getLastModified()));
            System.out.println("File date=" + new Date(file.lastModified()));

            if (file.lastModified() < urlConnection.getLastModified()) {
                System.out.println("Local jar " + file + " is obsolete");
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
            String jarFile = SCHEDULER_NODE_JAR;

            if (System.getProperty(NODE_JAR_PROPERTY) != null) {
                jarFile = System.getProperty(NODE_JAR_PROPERTY);
            }

            if (!isLocalJarUpToDate(jarUrl, jarFile)) {
                System.out.println("Downloading node.jar from " + jarUrl + " to " + jarFile);

                try {
                    File destination = new File(jarFile);
                    File lockFile = null;
                    FileLock lock = null;

                    if (destination.exists()) {

                        lockFile = new File(System.getProperty(TMP_DIR_PROPERTY) + "/lock");
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

                    if (lock != null && lockFile != null) {
                        System.out.println("Releasing the lock on " + lockFile.getAbsoluteFile());
                        lock.release();
                    }

                    return true;

                } catch (Exception e) {
                    System.err.println("Cannot download node.jar from " + jarUrl);
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("No java property " + NODE_URL_PROPERTY +
                " specified. Do not check for the new version.");
        }

        return false;
    }

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        makeNodeUpToDate();

        if (System.getProperty(ONE_JAR_JAR_PATH) == null) {
            System.setProperty(ONE_JAR_JAR_PATH, SCHEDULER_NODE_JAR);
        }
        System.out.println("Launching a computing node");
        Class<?> cls = Class.forName("OneJar");
        Method meth = cls.getMethod("main", String[].class);
        meth.invoke(null, (Object) args);
    }
}
