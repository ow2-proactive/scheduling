/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.ow2.proactive.scheduler.ext.matsci.common;

import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.runtime.StartPARuntime;
import org.ow2.proactive.scheduler.ext.common.util.PropertiesDumper;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


/**
 * JVMSpawnHelper
 *
 * @author The ProActive Team
 */
public class JVMSpawnHelper {

    private RegistrationListener registrationListener;

    /**
    * tool to build the JavaCommand
    */
    private DummyJVMProcess javaCommandBuilder;

    private long SEMAPHORE_TIMEOUT;
    private int RETRY_ACQUIRE;

    private Semaphore semaphore = new Semaphore(0);

    boolean debug = false;

    private PrintStream outDebug;

    private File nodeTmpDir;

    private String nodeName;

    private static int nodeCount = 0;

    /**
     * This hostname, for debugging purpose
     */
    private static String host = null;

    // protected static Process process = null;
    static {
        if (host == null) {
            try {
                host = java.net.InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    public JVMSpawnHelper(boolean debug, PrintStream outDebug, File nodeTmpDir, String nodeName,
            long timeout, int retry) {
        this.debug = debug;
        this.outDebug = outDebug;
        this.nodeTmpDir = nodeTmpDir;
        this.SEMAPHORE_TIMEOUT = timeout;
        this.RETRY_ACQUIRE = retry;
        this.nodeName = nodeName;
    }

    /**
     * Starts the java process on the given Node uri
     *
     * @return process
     * @throws Throwable
     */
    public final Process startProcess(String nodeBaseName, ProcessInitializer init, ProcessListener listener)
            throws Throwable {

        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] Starting a new JVM");
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] Starting a new JVM");
        }

        // Build java command
        javaCommandBuilder = new DummyJVMProcess();
        javaCommandBuilder.setClassname(StartPARuntime.class.getName());

        int deployID = new SecureRandom().nextInt();
        listener.setDeployID(deployID);

        registrationListener = new RegistrationListener(listener, nodeBaseName);
        registrationListener.subscribeJMXRuntimeEvent();

        javaCommandBuilder.setParameters("-d " + deployID + " -c 1 -p " +
            RuntimeFactory.getDefaultRuntime().getURL());

        javaCommandBuilder.setJvmOptions("-Dproactive.configuration=" + writeConfigFile());

        // We build the process with a separate environment
        ProcessBuilder pb = new ProcessBuilder();

        // Setting Environment variables
        Map<String, String> env = pb.environment();

        // Specific to the extension
        init.initProcess(javaCommandBuilder, env);

        if (debug) {
            System.out.println("Starting Process:");
            outDebug.println("Starting Process:");
            System.out.println(javaCommandBuilder.getJavaCommand());
            outDebug.println(javaCommandBuilder.getJavaCommand());
            System.out.println("With Environment: {");
            outDebug.println("With Environment: {");
            for (Map.Entry<String, String> entry : pb.environment().entrySet()) {
                System.out.println(entry.getKey() + "=" + entry.getValue());
                outDebug.println(entry.getKey() + "=" + entry.getValue());
            }
            System.out.println("}");
            outDebug.println("}");
        }

        pb.command(javaCommandBuilder.getJavaCommand());

        return pb.start();
    }

    /**
     * wait until the child runtime registers itself at the current JVM
     * in case it fails to register (because of any reason), we don't start the task at all exiting with an exception
     */
    public void waitForRegistration() throws InterruptedException {
        int numberOfTrials = 0;
        for (; numberOfTrials < RETRY_ACQUIRE; numberOfTrials++) {
            boolean permit = semaphore.tryAcquire(SEMAPHORE_TIMEOUT, TimeUnit.SECONDS);
            if (permit) {
                break;
            }

        }

        if (numberOfTrials == RETRY_ACQUIRE) {
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Unable to create a separate java process after " +
                    RETRY_ACQUIRE + " tries");
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Unable to create a separate java process after " +
                    RETRY_ACQUIRE + " tries");
            }
            throw new IllegalStateException("Unable to create a separate java process after " +
                RETRY_ACQUIRE + " tries");
        }

    }

    public URL writeConfigFile() throws IOException, URISyntaxException {
        File tmpConf = new File(nodeTmpDir, "ProActiveConfiguration.xml");
        PropertiesDumper.dumpProperties(tmpConf);
        return tmpConf.toURI().toURL();
    }

    public void unsubscribeJMXRuntimeEvent() {
        registrationListener.unsubscribeJMXRuntimeEvent();
    }

    class RegistrationListener implements NotificationListener {

        ProcessListener listener;

        String nodeBaseName;

        public RegistrationListener(ProcessListener listener, String nodeBaseName) {
            this.listener = listener;
            this.nodeBaseName = nodeBaseName;
        }

        private void subscribeJMXRuntimeEvent() {
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Subscribe JMX Runtime");
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Subscribe JMX Runtime");

            }
            ProActiveRuntimeImpl part = ProActiveRuntimeImpl.getProActiveRuntime();
            part.addDeployment(listener.getDeployID());
            JMXNotificationManager.getInstance().subscribe(part.getMBean().getObjectName(), this);
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Subscribed");
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Subscribed");
            }

        }

        private void unsubscribeJMXRuntimeEvent() {
            ProActiveRuntimeImpl part = ProActiveRuntimeImpl.getProActiveRuntime();
            try {
                ManagementFactory.getPlatformMBeanServer().removeNotificationListener(
                        part.getMBean().getObjectName(), this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //JMXNotificationManager.getInstance().unsubscribe(part.getMBean().getObjectName(), this);
        }

        public void handleNotification(Notification notification, Object handback) {
            try {
                String type = notification.getType();

                if (NotificationType.GCMRuntimeRegistered.equals(type)) {
                    if (debug) {
                        System.out.println("[" + new java.util.Date() + " " + host + " " +
                            this.getClass().getSimpleName() + "] Notification received");
                        outDebug.println("[" + new java.util.Date() + " " + host + " " +
                            this.getClass().getSimpleName() + "] Notification received");
                    }
                    GCMRuntimeRegistrationNotificationData data = (GCMRuntimeRegistrationNotificationData) notification
                            .getUserData();
                    if (data.getDeploymentId() != listener.getDeployID()) {
                        return;
                    }
                    if (debug) {
                        System.out.println("[" + new java.util.Date() + " " + host + " " +
                            this.getClass().getSimpleName() + "] Notification accepted");
                        outDebug.println("[" + new java.util.Date() + " " + host + " " +
                            this.getClass().getSimpleName() + "] Notification accepted");
                        outDebug.flush();
                    }

                    ProActiveRuntime childRuntime = data.getChildRuntime();
                    if (debug) {
                        System.out.println("[" + new java.util.Date() + " " + host + " " +
                            this.getClass().getSimpleName() + "] Creating Node");
                        outDebug.println("[" + new java.util.Date() + " " + host + " " +
                            this.getClass().getSimpleName() + "] Creating Node");
                    }
                    Node scilabNode = null;
                    try {
                        scilabNode = childRuntime.createLocalNode(nodeBaseName + "_" + nodeName + "_" +
                            nodeCount, true, null, null);
                    } catch (Exception e) {
                        if (debug) {
                            e.printStackTrace();
                            e.printStackTrace(outDebug);
                        }
                        throw e;
                    }
                    nodeCount++;
                    if (debug) {
                        System.out.println("[" + new java.util.Date() + " " + host + " " +
                            this.getClass().getSimpleName() + "] Node Created : " + scilabNode);
                        outDebug.println("[" + new java.util.Date() + " " + host + " " +
                            this.getClass().getSimpleName() + "] Node Created :" + scilabNode);
                    }
                    listener.setNode(scilabNode);

                    if (debug) {
                        System.out.println("[" + new java.util.Date() + " " + host + " " +
                            this.getClass().getSimpleName() + "] Waking up main thread");
                        outDebug.println("[" + new java.util.Date() + " " + host + " " +
                            this.getClass().getSimpleName() + "] Waking up main thread");

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                if (debug) {
                    e.printStackTrace(outDebug);
                }
            } finally {
                semaphore.release();
            }

        }

    }
}
