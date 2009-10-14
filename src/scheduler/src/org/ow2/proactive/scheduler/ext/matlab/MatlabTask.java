/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matlab;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
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
import org.ow2.proactive.scheduler.ext.matlab.exception.MatlabInitException;
import org.ow2.proactive.scheduler.ext.matlab.util.MatlabConfiguration;
import org.ow2.proactive.scheduler.ext.matlab.util.MatlabFinder;
import org.ow2.proactive.scheduler.ext.matlab.util.MatlabJVMInfo;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.*;


/**
 * This class represents a Matlab-specific Task inside the Scheduler
 *
 * @author The ProActive Team
 */
public class MatlabTask extends JavaExecutable implements NotificationListener {

    /**
     * Is this task run in debug mode ?
     */
    protected boolean debug;

    /**
     * Do we keep the Matlab engine between tasks ?
     */
    protected boolean keepEngine = false;

    /**
     * This hostname, for debugging purpose
     */
    protected static String host = null;

    /**
     * the index when the input is the result of a SplitTask
     */
    protected int index = -1;

    /**
     * the lines of inputScript
     */
    protected String inputScript = null;

    /**
     * Node name where this task is being executed
     */
    protected String nodeName = null;

    /**
     * The lines of the Matlab script
     */
    protected ArrayList<String> scriptLines = null;

    /**
     * The URI to which the spawned JVM(Node) is registered
     */
    protected static Map<String, MatlabJVMInfo> jvmInfos = new HashMap<String, MatlabJVMInfo>();

    /**
     * Tells if the shutdownhook has been set up for this runtime
     */
    protected static boolean shutdownhookSet = false;

    /**
     *  Thread which collects the JVM's stdout
     */
    //protected static LoggingThread isLogger = null;
    /**
     *  Thread which collects the JVM's stderr
     */
    //protected static LoggingThread esLogger = null;
    /**
     * tool to build the JavaCommand
     */
    private DummyJVMProcess javaCommandBuilder;

    /**
     * holds the Matlab environment information on this machine
     */
    protected static MatlabConfiguration matlabConfig = null;

    /**
     *  the Active Object worker located in the spawned JVM
     */
    // protected static AOMatlabWorker matlabWorker = null;
    /**
     * the OS where this JVM is running
     */
    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    private static boolean threadstarted = false;

    /**
     *  The process holding the spawned JVM
     */
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

    /**
     * Empty Constructor
     */
    public MatlabTask() {

    }

    /**
     * Convenience constructor
     *
     * @param inputScript script that will be launched and will produce an input to the main script
     * @param mainScript  main script to execute
     */
    public MatlabTask(String inputScript, String mainScript) {
        this.inputScript = inputScript;
        this.scriptLines = new ArrayList<String>();
        this.scriptLines.add(mainScript);
    }

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        if (results != null) {
            for (TaskResult res : results) {
                if (res.hadException()) {
                    throw res.getException();
                }
            }
        }
        Serializable res = null;
        nodeName = PAActiveObject.getNode().getNodeInformation().getName();
        MatlabJVMInfo jvminfo = jvmInfos.get(nodeName);
        if (jvminfo == null) {
            jvminfo = new MatlabJVMInfo();
            jvmInfos.put(nodeName, jvminfo);
        }
        if (jvminfo.getProcess() == null) {
            // First we try to find MATLAB
            if (debug) {
                System.out.println("[" + host + " MATLAB TASK] Looking for Matlab...");
            }
            matlabConfig = MatlabFinder.findMatlab(debug);
            if (debug) {
                System.out.println(matlabConfig);
            }

            if (debug) {
                System.out.println("[" + host + " MATLAB TASK] Starting the Java Process");
            }

            // We spawn a new JVM with the MATLAB library paths
            Process p = startProcess();
            jvminfo.setProcess(p);
            if (!shutdownhookSet) {
                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    public void run() {
                        for (MatlabJVMInfo info : jvmInfos.values()) {
                            info.getProcess().destroy();
                        }
                        shutdownhookSet = true;
                    }
                }));
            }
        } else {

            if (!threadstarted) {
                if (debug) {
                    System.out.println("[" + host + " MATLAB TASK] Starting the Threads");
                }
                // We define the loggers which will write on standard output what comes from the java process
                LoggingThread lt1 = new LoggingThread(jvminfo.getProcess().getInputStream(), "[" + host +
                    " OUT]", System.out);// new PrintStream(new File("D:\\test_out.txt")));//System.out);
                LoggingThread lt2 = new LoggingThread(jvminfo.getProcess().getErrorStream(), "[" + host +
                    " ERR]", System.err);// new PrintStream(new File("D:\\test_err.txt")));//System.err);

                jvminfo.setLogger(lt1);
                jvminfo.setEsLogger(lt2);

                // We start the loggers thread

                Thread t1 = new Thread(lt1, "OUT Matlab");
                t1.setDaemon(true);
                t1.start();

                Thread t2 = new Thread(lt2, "ERR Matlab");
                t2.setDaemon(true);
                t2.start();

                threadstarted = true;

            } else {
                jvminfo.getLogger().setStream(System.out);
                jvminfo.getEsLogger().setStream(System.err);
            }

        }

        if (debug) {
            System.out.println("[" + host + " MATLAB TASK] Executing the task");
        }

        // finally we call the internal version of the execute method
        res = executeInternal(results);

        if (debug) {
            System.out.println("[" + host + " MATLAB TASK] Task completed successfully");
        }

        return res;
    }

    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        // Retrieving task parameters

        // main script to execute (embedded, url or file)
        Object s = args.get("script");

        if (s != null) {
            scriptLines = new ArrayList<String>();
            scriptLines.add((String) s);
        }

        URL scriptURL;

        Object u = args.get("scriptUrl");

        if (u != null) {
            scriptURL = new URI((String) u).toURL();

            InputStream is = scriptURL.openStream();
            scriptLines = IOTools.getContentAsList(is);
        }

        Object f = args.get("scriptFile");

        if (f != null) {
            FileInputStream fis = new FileInputStream((String) f);
            scriptLines = IOTools.getContentAsList(fis);
        }

        if (scriptLines.size() == 0) {
            throw new IllegalArgumentException(
                "Either one of \"script\" \"scripturl\" \"scriptfile\" must be given");
        }

        // an input script, launched before the main script (embedded only)
        Object input = args.get("input");

        if (input != null) {
            inputScript = (String) input;
        }

        // index when doing fork/join taskflows
        Object ind = args.get("index");

        if (ind != null) {
            index = Integer.parseInt((String) ind);
        }

        String d = (String) args.get("debug");
        if (d != null) {
            debug = Boolean.parseBoolean(d);
        }

        String ke = (String) args.get("keepEngine");
        if (ke != null) {
            keepEngine = Boolean.parseBoolean(ke);
        }

        host = java.net.InetAddress.getLocalHost().getHostName();
    }

    /**
     * Deploy an Active Object on the given Node uri
     *
     * @throws Throwable
     */
    protected AOMatlabWorker deploy(String className) throws Throwable {
        ProActiveException ex = null;
        MatlabJVMInfo jvminfo = jvmInfos.get(nodeName);

        if (debug) {
            System.out.println("[" + host + " MatlabTask] Deploying Worker");
        }

        final AOMatlabWorker worker = (AOMatlabWorker) PAActiveObject.newActive(className,
                new Object[] { matlabConfig }, jvminfo.getNode());

        jvminfo.setWorker(worker);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                worker.terminate();
            }
        }));

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

        boolean notInitializationTask = inputScript.indexOf("PROACTIVE_INITIALIZATION_CODE") == -1;
        MatlabJVMInfo jvminfo = jvmInfos.get(nodeName);
        AOMatlabWorker sw = jvminfo.getWorker();
        if (sw == null) {
            synchronized (jvminfo) {
                jvminfo.wait();
                sw = deploy(AOMatlabWorker.class.getName());
            }
        }

        if (debug) {
            System.out.println("[" + host + " MatlabTask] Initializing");
        }
        try {
            sw.init(inputScript, scriptLines, debug);
        } catch (Exception e) {
            if (debug) {
                System.out.println("[" + host + " MatlabTask] Worker is dead redeploying");
            }
            sw = deploy(AOMatlabWorker.class.getName());
            sw.init(inputScript, scriptLines, debug);

        }
        if (debug) {
            System.out.println("[" + host + " MatlabTask] Executing");
        }
        Serializable res = null;
        try {

            // We execute the task on the worker
            res = sw.execute(index, results);
            // We wait for the result
            res = (Serializable) PAFuture.getFutureValue(res);

            if (debug) {
                System.out.println("[" + host + " MatlabTask] Received result");
            }
            if (notInitializationTask && !keepEngine) {
                if (debug) {
                    System.out.println("[" + host + " MatlabTask] Terminating Matlab engine");
                }
                sw.terminate();
            }

        } catch (Exception e) {
            System.out.println("[" + host + " MatlabTask] Terminating Matlab engine");
            sw.terminate();
            throw e;
        }

        return res;
    }

    /**
     * Starts the java process on the given Node uri
     *
     * @return process
     * @throws Throwable
     */
    private final Process startProcess() throws Throwable {
        if (debug) {
            System.out.println("[" + host + " MatlabTask] Starting a new JVM");
        }
        MatlabJVMInfo jvminfo = jvmInfos.get(nodeName);
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

        // Classpath specific
        String classpath = prependPtolemyLibDirToClassPath(javaCommandBuilder.getClasspath());
        javaCommandBuilder.setClasspath(classpath);

        // we add matlab directories to LD_LIBRARY_PATH
        String libPath = env.get("LD_LIBRARY_PATH");
        libPath = addPtolemyLibDirToPath(addMatlabToPath(libPath));

        env.put("LD_LIBRARY_PATH", libPath);

        // we add matlab directories to PATH (Windows)
        String path = env.get("PATH");

        if (path == null) {
            path = env.get("Path");
        }

        env.put("PATH", addPtolemyLibDirToPath(addMatlabToPath(path)));

        // we set as well the java.library.path property (precaution), we forward as well the RMI port in use

        javaCommandBuilder.setJvmOptions("-Djava.library.path=\"" + libPath + "\"" +
            " -Dproactive.rmi.port=" + Integer.parseInt(PAProperties.PA_RMI_PORT.getValue()));

        if (debug) {
            System.out.println("Starting Process:");
            System.out.println(javaCommandBuilder.getJavaCommand());
            System.out.println("With Environment: {");
            for (Map.Entry<String, String> entry : pb.environment().entrySet()) {
                System.out.println(entry.getKey() + "=" + entry.getValue());
            }
            System.out.println("}");
        }

        pb.command(javaCommandBuilder.getJavaCommand());

        return pb.start();
    }

    private void subscribeJMXRuntimeEvent() {
        MatlabJVMInfo jvminfo = jvmInfos.get(nodeName);
        ProActiveRuntimeImpl part = ProActiveRuntimeImpl.getProActiveRuntime();
        part.addDeployment(jvminfo.getDeployID());
        JMXNotificationManager.getInstance().subscribe(part.getMBean().getObjectName(), this);
    }

    public void handleNotification(Notification notification, Object handback) {
        try {
            String type = notification.getType();

            if (NotificationType.GCMRuntimeRegistered.equals(type)) {
                GCMRuntimeRegistrationNotificationData data = (GCMRuntimeRegistrationNotificationData) notification
                        .getUserData();
                MatlabJVMInfo jvminfo = jvmInfos.get(nodeName);
                if (data.getDeploymentId() != jvminfo.getDeployID()) {
                    return;
                }
                synchronized (jvminfo) {

                    ProActiveRuntime childRuntime = data.getChildRuntime();

                    Node scilabNode = childRuntime.createLocalNode("Matlab_" + nodeName, false, null, null,
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

    private String addPtolemyLibDirToPath(String path) {
        String newpath = path;
        newpath = newpath + os.pathSeparator() + matlabConfig.getPtolemyPath();
        return newpath;
    }

    private String prependPtolemyLibDirToClassPath(String classPath) throws IOException, URISyntaxException,
            MatlabInitException {
        String newcp = classPath;
        newcp = matlabConfig.getPtolemyPath() + os.pathSeparator() + newcp;
        return newcp;
    }

    /**
     * Utility function to add MATLAB directories to the given path-like string
     *
     * @param path path-like string
     * @return an augmented path
     */
    private String addMatlabToPath(String path) {
        String newPath;

        if (path == null) {
            newPath = "";
        } else {
            newPath = os.pathSeparator() + path;
        }

        String lastDir = null;
        int lastIndex = matlabConfig.getMatlabLibDirName().lastIndexOf(os.fileSeparator());
        if (lastIndex != -1) {
            lastDir = matlabConfig.getMatlabLibDirName().substring(lastIndex + 1);
        } else {
            lastDir = matlabConfig.getMatlabLibDirName();
        }

        newPath = (matlabConfig.getMatlabHome() + os.fileSeparator() + matlabConfig.getMatlabBinDir()) +
            newPath;
        newPath = (matlabConfig.getMatlabHome() + os.fileSeparator() + matlabConfig.getMatlabLibDirName()) +
            os.pathSeparator() + newPath;
        newPath = (matlabConfig.getMatlabHome() + os.fileSeparator() + "sys" + os.fileSeparator() + "os" +
            os.fileSeparator() + lastDir) +
            os.pathSeparator() + newPath;

        return newPath;
    }

    /**
     * An utility class to build the Java command
     *
     * @author The ProActive Team
     */
    public static class DummyJVMProcess extends JVMProcessImpl implements Serializable {

        public DummyJVMProcess() {
            super();
        }

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
