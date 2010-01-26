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
package org.ow2.proactive.scheduler.ext.scilab;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.runtime.StartPARuntime;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.common.util.IOTools.LoggingThread;
import org.ow2.proactive.scheduler.ext.scilab.util.ScilabConfiguration;
import org.ow2.proactive.scheduler.ext.scilab.util.ScilabFinder;
import org.ow2.proactive.scheduler.ext.scilab.util.ScilabJVMInfo;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;


/**
 * This class represents a Scilab-specific Task inside the Scheduler
 * @author The ProActive Team
 */
public class ScilabTask extends JavaExecutable implements NotificationListener {

    final private static String[] DEFAULT_OUT_VARIABLE_SET = { "out" };

    protected boolean debug;

    /**
     * This hostname, for debugging purpose
     */
    protected String host;

    /**
     * the lines of inputScript
     */
    protected String inputScript = null;

    /**
     * Definition of user functions
     */
    protected String functionsDefinition = null;

    /**
     * The lines of the Scilab script
     */
    protected ArrayList<String> scriptLines = null;

    /**
     * Node name where this task is being executed
     */
    protected String nodeName = null;

    /**
     * the array of output variable names
     */
    protected String[] out_set = DEFAULT_OUT_VARIABLE_SET;

    /**
     * Tells if the shutdownhook has been set up for this runtime 
     */
    protected static boolean shutdownhookSet = false;

    /**
     * tool to build the JavaCommand
     */
    protected static DummyJVMProcess javaCommandBuilder;

    /**
     * holds the Scilab environment information
     */
    protected static ScilabConfiguration scilabConfig = null;

    /**
     * the Active Object worker located in the spawned JVM
     */
    protected static Map<String, ScilabJVMInfo> jvmInfos = new HashMap<String, ScilabJVMInfo>();

    /**
     * the OS where this JVM is running
     */
    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    /**
     * ProActive No Arg Constructor
     */
    public ScilabTask() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.proactive.scheduler.common.task.Executable#execute(org.ow2.proactive.scheduler.common.task.TaskResult[])
     */
    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        if (results != null) {
            for (TaskResult res : results) {
                if (res.hadException()) {
                    throw res.getException();
                }
            }
        }
        nodeName = PAActiveObject.getNode().getNodeInformation().getName();
        ScilabJVMInfo jvminfo = jvmInfos.get(nodeName);
        if (jvminfo == null) {
            jvminfo = new ScilabJVMInfo();
            jvmInfos.put(nodeName, jvminfo);
        }
        if (jvminfo.getProcess() == null) {

            // First we try to find SCILAB
            if (debug) {
                System.out.println("[" + host + " ScilabTask] launching script to find Scilab");
            }
            scilabConfig = ScilabFinder.findScilab(debug);
            if (debug) {
                System.out.println(scilabConfig);
            }

            if (debug) {
                System.out.println("[" + host + " ScilabTask] Starting the Java Process");
            }
            // We spawn a new JVM with the SCILAB library paths
            Process p = startProcess();
            jvminfo.setProcess(p);

            // We add a shutdownhook to terminate children processes
            if (!shutdownhookSet) {
                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    public void run() {
                        for (ScilabJVMInfo info : jvmInfos.values()) {
                            info.getProcess().destroy();
                        }
                    }
                }));
                shutdownhookSet = true;
            }

            LoggingThread lt1 = new LoggingThread(p.getInputStream(), "[" + host + " OUT]", System.out);
            LoggingThread lt2 = new LoggingThread(p.getErrorStream(), "[" + host + " ERR]", System.err);
            IOTools.RedirectionThread rt1 = new IOTools.RedirectionThread(System.in, p.getOutputStream());

            // We define the loggers which will write on standard output what comes from the java process
            jvminfo.setIsLogger(lt1);
            jvminfo.setEsLogger(lt2);
            jvminfo.setIoThread(rt1);

            // We start the loggers thread
            Thread t1 = new Thread(lt1, "OUT Scilab");
            t1.setDaemon(true);
            t1.start();

            Thread t2 = new Thread(lt2, "ERR Scilab");
            t2.setDaemon(true);
            t2.start();

            Thread t3 = new Thread(rt1, "Redirecting I/O Scilab");
            t3.setDaemon(true);
            t3.start();

        }
        if (debug) {
            System.out.println("[" + host + " ScilabTask] Executing the task");
        }

        // finally we call the internal version of the execute method
        Serializable res = executeInternal(results);

        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.proactive.scheduler.common.task.JavaExecutable#init(java.util.Map)
     */
    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        Object s = args.get("script");

        if (s != null) {
            scriptLines = new ArrayList<String>();
            scriptLines.add((String) s);
        }

        URL scriptURL;

        String u = (String) args.get("scriptUrl");

        if (u != null) {
            scriptURL = new URI(u).toURL();

            InputStream is = scriptURL.openStream();
            scriptLines = IOTools.getContentAsList(is);
        }

        String f = (String) args.get("scriptFile");

        if (f != null) {
            FileInputStream fis = new FileInputStream(f);
            scriptLines = IOTools.getContentAsList(fis);
        }

        if (scriptLines.size() == 0) {
            throw new IllegalArgumentException(
                "Either one of \"script\" \"scripturl\" \"scriptfile\" must be given");
        }

        String d = (String) args.get("debug");
        if (d != null) {
            debug = Boolean.parseBoolean(d);
        }

        String input = (String) args.get("input");

        if (input != null) {
            inputScript = input;
        }

        String functionsDef = (String) args.get("functionsDefinition");
        if (functionsDef != null) {
            functionsDefinition = functionsDef;
        }

        String outputs = (String) args.get("outputs");
        if (outputs != null) {
            out_set = outputs.split("[ ,]+");
        }

        host = java.net.InetAddress.getLocalHost().getHostName();
    }

    /**
     * Deploy the scilab worker AO on the given Node
     *
     * @throws Throwable
     */
    protected AOScilabWorker deploy() throws Throwable {
        Exception ex = null;
        AOScilabWorker worker = null;
        ScilabJVMInfo jvminfo = jvmInfos.get(nodeName);
        if (debug) {
            System.out.println("[" + host + " ScilabTask] Deploying the Worker");
        }

        worker = (AOScilabWorker) PAActiveObject.newActive(AOScilabWorker.class.getName(),
                new Object[] { scilabConfig }, jvminfo.getNode());

        jvminfo.setWorker(worker);

        return worker;
    }

    /**
     * Internal version of the execute method
     *
     * @param results results from preceding tasks
     * @return result of the task
     * @throws Throwable
     */
    protected Serializable executeInternal(TaskResult... results) throws Throwable {
        ScilabJVMInfo jvminfo = jvmInfos.get(nodeName);
        AOScilabWorker sw = jvminfo.getWorker();
        if (sw == null) {
            synchronized (jvminfo) {
                jvminfo.wait();
                sw = deploy();
            }
        }
        if (debug) {
            System.out.println("[" + host + " ScilabTask] Initializing");
        }
        try {
            sw.init(inputScript, functionsDefinition, scriptLines, out_set, debug);
        } catch (Exception e) {
            // in case the active object died
            e.printStackTrace();
            if (debug) {
                System.out.println("[" + host + " ScilabTask] Re-deploying Worker");
            }
            sw = deploy();
            sw.init(inputScript, functionsDefinition, scriptLines, out_set, debug);
        }

        if (debug) {
            System.out.println("[" + host + " ScilabTask] Executing");
        }

        // We execute the task on the worker
        Serializable res = sw.execute(results);
        // We wait for the result
        res = (Serializable) PAFuture.getFutureValue(res);

        if (debug) {
            System.out.println("[" + host + " ScilabTask] Received result");
        }

        return res;
    }

    private final Process startProcess() throws Throwable {
        if (debug) {
            System.out.println("[" + host + " ScilabTask] Starting a new JVM");
        }
        ScilabJVMInfo jvminfo = jvmInfos.get(nodeName);
        // Build java command
        javaCommandBuilder = new DummyJVMProcess();
        javaCommandBuilder.setClassname(StartPARuntime.class.getName());
        int deployid = new SecureRandom().nextInt();
        jvminfo.setDeployID(deployid);
        subscribeJMXRuntimeEvent();

        javaCommandBuilder.setParameters("-d " + deployid + " -p " +
            RuntimeFactory.getDefaultRuntime().getURL());

        // We build the process with a separate environment
        ProcessBuilder pb = new ProcessBuilder();

        // Setting Environment variables
        Map<String, String> env = pb.environment();

        // we add scilab directories to LD_LIBRARY_PATH
        String libPath = env.get("LD_LIBRARY_PATH");
        libPath = addScilabToPath(libPath);

        env.put("LD_LIBRARY_PATH", libPath);

        // we add scilab directories to PATH (Windows)
        String path = env.get("PATH");

        if (path == null) {
            path = env.get("Path");
        }

        env.put("PATH", addScilabToPath(path));

        // This tells scilab to run without graphical interface
        env.put("SCI_JAVA_ENABLE_HEADLESS", "1");

        // Classpath specific
        String classpath = addScilabJarsToClassPath(javaCommandBuilder.getClasspath());
        javaCommandBuilder.setClasspath(classpath);

        // We add the Scilab specific env variables
        env.put("SCI", scilabConfig.getScilabSCIDir());
        env.put("SCIDIR", scilabConfig.getScilabSCIDir());

        // javaCommandBuilder.setJavaPath(System.getenv("JAVA_HOME") +
        //     "/bin/java");
        // we set as well the java.library.path property (precaution)
        javaCommandBuilder.setJvmOptions("-Djava.library.path=\"" + libPath + "\"" +
            " -Dproactive.rmi.port=" + Integer.parseInt(PAProperties.PA_RMI_PORT.getValue()));

        pb.command(javaCommandBuilder.getJavaCommand());

        return pb.start();
    }

    private void subscribeJMXRuntimeEvent() {
        ScilabJVMInfo jvminfo = jvmInfos.get(nodeName);
        ProActiveRuntimeImpl part = ProActiveRuntimeImpl.getProActiveRuntime();
        part.addDeployment(jvminfo.getDeployID());
        JMXNotificationManager.getInstance().subscribe(part.getMBean().getObjectName(), this);
    }

    /**
     * Utility function to add SCILAB jar files to the given path-like string
     *
     * @param classpath path-like string
     * @return an augmented path
     */
    private String addScilabJarsToClassPath(String classpath) {

        String newPath;

        if (classpath == null) {
            newPath = "";
        } else {
            newPath = os.pathSeparator() + classpath;
        }

        newPath = (scilabConfig.getScilabSCIDir() + os.fileSeparator() + "modules" + os.fileSeparator() +
            "jvm" + os.fileSeparator() + "jar" + os.fileSeparator() + "org.scilab.modules.jvm.jar") +
            newPath;

        return newPath;
    }

    /**
     * Utility function to add SCILAB directories to the given path-like string
     *
     * @param path path-like string
     * @return an augmented path
     */
    private String addScilabToPath(String path) {
        String newPath;

        if (path == null) {
            newPath = "";
        } else {
            newPath = os.pathSeparator() + path;
        }

        newPath = (scilabConfig.getScilabHome() + os.fileSeparator() + scilabConfig.getScilabLibDir()) +
            newPath;

        File thirdpartyFolder = new File(scilabConfig.getScilabHome() + os.fileSeparator() +
            scilabConfig.getScilabLibDir() + os.fileSeparator() + ".." + os.fileSeparator() + "thirdparty");
        if (thirdpartyFolder.exists() && (os == OperatingSystem.unix)) {
            newPath = thirdpartyFolder.getPath() + os.pathSeparator() + newPath;
        }

        return newPath;
    }

    public void handleNotification(Notification notification, Object handback) {
        try {
            String type = notification.getType();

            if (NotificationType.GCMRuntimeRegistered.equals(type)) {
                GCMRuntimeRegistrationNotificationData data = (GCMRuntimeRegistrationNotificationData) notification
                        .getUserData();
                ScilabJVMInfo jvminfo = jvmInfos.get(nodeName);
                if (data.getDeploymentId() != jvminfo.getDeployID()) {
                    return;
                }
                synchronized (jvminfo) {

                    ProActiveRuntime childRuntime = data.getChildRuntime();

                    Node scilabNode = childRuntime.createLocalNode("Scilab_" + nodeName, false, null, null,
                            null);
                    jvminfo.setNode(scilabNode);
                    jvminfo.notifyAll();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            notifyAll();
        }

    }

    /**
     * An utility class to build the Java command
     *
     * @author The ProActive Team
     */
    private static class DummyJVMProcess extends JVMProcessImpl {

        /**
         *
         */
        public List<String> getJavaCommand() {
            String javaCommand = buildJavaCommand();
            List<String> javaCommandList = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(javaCommand, " ");

            while (st.hasMoreElements()) {
                javaCommandList.add(st.nextToken());
            }

            return javaCommandList;
        }
    }

}
