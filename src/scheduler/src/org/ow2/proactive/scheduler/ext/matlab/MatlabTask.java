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
package org.ow2.proactive.scheduler.ext.matlab;

import org.jvnet.winp.WinProcess;
import org.jvnet.winp.WinpException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.exceptions.FutureMonitoringPingFailureException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
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
import org.ow2.proactive.scheduler.util.process.ProcessTreeKiller;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.lang.management.ManagementFactory;
import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


/**
 * This class represents a Matlab-specific Task inside the Scheduler
 *
 * @author The ProActive Team
 */
public class MatlabTask extends JavaExecutable {

    /**
     * Is this task run in debug mode ?
     */
    protected boolean debug = false;

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

    protected static int taskCount = 0;
    protected static int taskCountBeforeJVMRespawn;
    protected static int nodeCount = 0;

    protected static boolean startingProcess = false;
    protected static boolean redeploying = false;

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

    private static final long SEMAPHORE_TIMEOUT = 2;
    private static final int RETRY_ACQUIRE = 10;
    private Semaphore semaphore = new Semaphore(0);

    private RegistrationListener registrationListener;

    private PrintStream outDebug;

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
        if (os.equals(OperatingSystem.windows)) {
            WinProcess.enableDebugPrivilege();
            taskCountBeforeJVMRespawn = 30;
        } else {
            taskCountBeforeJVMRespawn = 100;
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

    protected void destroyProcess(MatlabJVMInfo jvminfo) {

        taskCount = 1;

        if (os.equals(OperatingSystem.windows)) {
            destroyProcessWindows(jvminfo);
        } else {
            destroyProcessUnix(jvminfo);
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {

        }
        //jvminfo.getProcess().destroy();

        jvminfo.setProcess(null);
        jvminfo.setWorker(null);

    }

    protected void destroyProcessWindows(MatlabJVMInfo jvminfo) {

        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + host + " MATLAB TASK] Destroying JVM");
            outDebug.println("[" + new java.util.Date() + " " + host + " MATLAB TASK] Destroying JVM");
        }
        try {
            jvminfo.getWorker().terminate();
        } catch (Exception e1) {
        }

        Process proc = jvminfo.getProcess();

        WinProcess pi = new WinProcess(proc);

        try {
            if (debug) {
                System.out.println("Killing process " + pi.getPid());
                outDebug.println("Killing process " + pi.getPid());
            }
            Runtime.getRuntime().exec("taskkill /PID " + pi.getPid() + " /T");

        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, String> modelEnv = new HashMap<String, String>();
        modelEnv.put("NODE_NAME", nodeName);
        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + host +
                " MATLAB TASK] Destroying processes with NODE_NAME=" + nodeName);
            outDebug.println("[" + new java.util.Date() + " " + host +
                " MATLAB TASK] Destroying processes with NODE_NAME=" + nodeName);
        }

        for (WinProcess p : WinProcess.all()) {
            if (p.getPid() < 10)
                continue; // ignore system processes like "idle process"

            boolean matched;

            try {
                matched = hasMatchingEnvVars(p.getEnvironmentVariables(), modelEnv);
            } catch (WinpException e) {
                // likely a missing privilege
                continue;
            }

            if (matched) {
                if (debug) {
                    outDebug.println("Matched :");
                    outDebug.println(p.getCommandLine());

                    String val = p.getEnvironmentVariables().get("NODE_NAME");

                    outDebug.println("NODE_NAME=" + val);
                    outDebug.println("Killing process " + p.getPid());
                }
                try {

                    Runtime.getRuntime().exec("taskkill /PID " + p.getPid() + " /T");

                } catch (IOException e) {
                    e.printStackTrace();
                }
                //p.kill();
            }
        }

    }

    protected void destroyProcessUnix(MatlabJVMInfo jvminfo) {
        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + host + " MATLAB TASK] Destroying JVM");
            outDebug.println("[" + new java.util.Date() + " " + host + " MATLAB TASK] Destroying JVM");
        }
        try {
            jvminfo.getWorker().terminate();
        } catch (Exception e1) {
        }

        Process proc = jvminfo.getProcess();

        Map<String, String> modelEnv = new HashMap<String, String>();
        modelEnv.put("NODE_NAME", nodeName);
        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + host +
                " MATLAB TASK] Destroying processes with NODE_NAME=" + nodeName);
            outDebug.println("[" + new java.util.Date() + " " + host +
                " MATLAB TASK] Destroying processes with NODE_NAME=" + nodeName);
        }

        ProcessTreeKiller.get().kill(proc, modelEnv);

    }

    protected boolean hasMatchingEnvVars(Map<String, String> envVar, Map<String, String> modelEnvVar) {
        if (modelEnvVar.isEmpty())
            // sanity check so that we don't start rampage.
            return false;

        for (Map.Entry<String, String> e : modelEnvVar.entrySet()) {
            String v = envVar.get(e.getKey());
            if (v == null || !v.equals(e.getValue()))
                return false; // no match
        }
        return true;
    }

    protected void handleProcess(MatlabJVMInfo jvminfo) throws Throwable {
        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + host +
                " MATLAB TASK] Checking Processes...");
            outDebug.println("[" + new java.util.Date() + " " + host + " MATLAB TASK] Checking Processes...");
        }
        if (jvminfo.getProcess() == null) {
            // First we try to find MATLAB
            if (matlabConfig == null) {
                if (debug) {
                    System.out.println("[" + new java.util.Date() + " " + host +
                        " MATLAB TASK] Looking for Matlab...");
                    outDebug.println("[" + new java.util.Date() + " " + host +
                        " MATLAB TASK] Looking for Matlab...");
                }
                matlabConfig = MatlabFinder.findMatlab(debug);
                if (debug) {
                    System.out.println(matlabConfig);
                    outDebug.println(matlabConfig);
                }
            }

            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] Starting the Java Process");
                outDebug.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] Starting the Java Process");
            }

            // We spawn a new JVM with the MATLAB library paths
            Process p = startProcess();
            jvminfo.setProcess(p);
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] Process successfully started");
                outDebug.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] Process successfully started");
            }
            if (!shutdownhookSet) {
                if (debug) {
                    System.out.println("[" + new java.util.Date() + " " + host +
                        " MATLAB TASK] Adding shutDownHook");
                    outDebug.println("[" + new java.util.Date() + " " + host +
                        " MATLAB TASK] Adding shutDownHook");
                }
                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    public void run() {
                        for (MatlabJVMInfo info : jvmInfos.values()) {
                            try {
                                destroyProcess(info);
                                //info.getProcess().destroy();
                            } catch (Exception e) {
                            }
                        }
                        shutdownhookSet = true;
                    }
                }));
            }
        }

        //TODO for multi node, threadstarted must be different for each node
        if (!threadstarted) {
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] Starting the Threads");
                outDebug.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] Starting the Threads");
            }
            // We define the loggers which will write on standard output what comes from the java process
            LoggingThread lt1;
            LoggingThread lt2;
            if (debug) {
                lt1 = new LoggingThread(jvminfo.getProcess().getInputStream(), "[" + host + " OUT]",
                    System.out, outDebug);// new PrintStream(new File("D:\\test_out.txt")));//System.out);
                lt2 = new LoggingThread(jvminfo.getProcess().getErrorStream(), "[" + host + " ERR]",
                    System.err, outDebug);// new PrintStream(new File("D:\\test_err.txt")));//System.err);

            } else {
                lt1 = new LoggingThread(jvminfo.getProcess().getInputStream(), "[" + host + " OUT]",
                    System.out);// new PrintStream(new File("D:\\test_out.txt")));//System.out);
                lt2 = new LoggingThread(jvminfo.getProcess().getErrorStream(), "[" + host + " ERR]",
                    System.err);// new PrintStream(new File("D:\\test_err.txt")));//System.err);
            }

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
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host + " MATLAB TASK] Threads started");
                outDebug.println("[" + new java.util.Date() + " " + host + " MATLAB TASK] Threads started");
            }
        } else if (startingProcess) {
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] Connecting process out to threads");
                outDebug.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] Connecting process out to threads");
            }
            jvminfo.getLogger().setInputStream(jvminfo.getProcess().getInputStream());
            jvminfo.getEsLogger().setInputStream(jvminfo.getProcess().getErrorStream());
            if (!redeploying) {
                if (debug) {
                    jvminfo.getLogger().setStream(System.out, outDebug);
                    jvminfo.getEsLogger().setStream(System.err, outDebug);
                } else {
                    jvminfo.getLogger().setStream(System.out);
                    jvminfo.getEsLogger().setStream(System.err);
                }
            }
            startingProcess = false;
        } else {
            if (debug) {
                jvminfo.getLogger().setStream(System.out, outDebug);
                jvminfo.getEsLogger().setStream(System.err, outDebug);
            } else {
                jvminfo.getLogger().setStream(System.out);
                jvminfo.getEsLogger().setStream(System.err);
            }
        }

        AOMatlabWorker sw = jvminfo.getWorker();
        if (sw == null) {
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] waiting for deployment");
                outDebug.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] waiting for deployment");
            }
            waitForRegistration();

            sw = deploy(AOMatlabWorker.class.getName());

            registrationListener.unsubscribeJMXRuntimeEvent();
        }
    }

    /**
     * wait until the child runtime registers itself at the current JVM
     * in case it fails to register (because of any reason), we don't start the task at all exiting with an exception
     */
    private void waitForRegistration() throws InterruptedException {
        int numberOfTrials = 0;
        for (; numberOfTrials < RETRY_ACQUIRE; numberOfTrials++) {
            boolean permit = semaphore.tryAcquire(SEMAPHORE_TIMEOUT, TimeUnit.SECONDS);
            if (permit) {
                break;
            }

        }

        if (numberOfTrials == RETRY_ACQUIRE) {
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] Unable to create a separate java process after " + RETRY_ACQUIRE +
                    " tries");
                outDebug.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] Unable to create a separate java process after " + RETRY_ACQUIRE +
                    " tries");
            }
            throw new IllegalStateException("Unable to create a separate java process after " +
                RETRY_ACQUIRE + " tries");
        }

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
        nodeName = PAActiveObject.getNode().getVMInformation().getName().replace('-', '_') + "_" +
            PAActiveObject.getNode().getNodeInformation().getName().replace('-', '_');
        if (debug) {
            // system temp dir
            String tmpPath = System.getProperty("java.io.tmpdir");

            // log file writer used for debugging
            File logFile = new File(tmpPath, "Task" + nodeName + ".log");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            outDebug = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile, true)));
        }

        MatlabJVMInfo jvminfo = jvmInfos.get(nodeName);
        if (jvminfo == null) {
            jvminfo = new MatlabJVMInfo();
            jvmInfos.put(nodeName, jvminfo);
        }

        int nbAttempts = 1;
        //if (keepEngine) {
        taskCount++;
        if (taskCount == taskCountBeforeJVMRespawn) {
            destroyProcess(jvminfo);
        }
        //}
        redeploying = false;
        while (res == null) {
            handleProcess(jvminfo);
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] Executing the task");
                outDebug.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] Executing the task, try " + nbAttempts);
            }

            // finally we call the internal version of the execute method

            try {
                res = executeInternal(results);
            } catch (FutureMonitoringPingFailureException e) {
                redeploying = true;
                if (debug) {
                    e.printStackTrace(outDebug);
                    System.out.println("[" + new java.util.Date() + " " + host +
                        " MATLAB TASK] Spawned JVM crashed, redeploying");
                    outDebug.println("[" + new java.util.Date() + " " + host +
                        " MATLAB TASK] Spawned JVM crashed, redeploying");
                }
                destroyProcess(jvminfo);

                if (nbAttempts >= 2) {
                    throw e;
                }
                nbAttempts++;
            } finally {
                if (res != null) {
                    jvminfo.getLogger().closeStream();
                    jvminfo.getEsLogger().closeStream();
                }
                if (res != null && debug) {
                    outDebug
                            .println("[" + new java.util.Date() + " " + host + " MATLAB TASK] Closing output");
                    outDebug.close();
                } else if (nbAttempts >= 3 && debug) {
                    outDebug
                            .println("[" + new java.util.Date() + " " + host + " MATLAB TASK] Closing output");
                    outDebug.close();
                }
            }

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
            System.out.println("[" + new java.util.Date() + " " + host + " MatlabTask] Deploying Worker");
            outDebug.println("[" + new java.util.Date() + " " + host + " MatlabTask] Deploying Worker");
        }

        final AOMatlabWorker worker = (AOMatlabWorker) PAActiveObject.newActive(className,
                new Object[] { matlabConfig }, jvminfo.getNode());

        jvminfo.setWorker(worker);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    worker.terminate();
                } catch (Exception e) {
                }
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

        Serializable res = null;
        AOMatlabWorker sw = null;

        // boolean notInitializationTask = inputScript.indexOf("PROACTIVE_INITIALIZATION_CODE") == -1;
        MatlabJVMInfo jvminfo = jvmInfos.get(nodeName);
        sw = jvminfo.getWorker();

        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + host + " MatlabTask] Initializing");
            outDebug.println("[" + new java.util.Date() + " " + host + " MatlabTask] Initializing");
        }

        sw.init(inputScript, scriptLines, debug);

        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + host + " MatlabTask] Executing");
            outDebug.println("[" + new java.util.Date() + " " + host + " MatlabTask] Executing");
        }

        try {

            // We execute the task on the worker
            res = sw.execute(index, results);
            // We wait for the result
            res = (Serializable) PAFuture.getFutureValue(res);

            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host + " MatlabTask] Received result");
                outDebug.println("[" + new java.util.Date() + " " + host + " MatlabTask] Received result");
            }

        } finally {
            if (!keepEngine) {
                if (debug) {
                    System.out.println("[" + new java.util.Date() + " " + host +
                        " MatlabTask] Terminating Matlab engine");
                    outDebug.println("[" + new java.util.Date() + " " + host +
                        " MatlabTask] Terminating Matlab engine");
                }
                try {
                    boolean ok = sw.terminate();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (debug) {
                        e.printStackTrace(outDebug);
                    }
                }

            }
        }

        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + host +
                " MATLAB TASK] Task completed successfully");
            outDebug.println("[" + new java.util.Date() + " " + host +
                " MATLAB TASK] Task completed successfully");

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
            System.out.println("[" + new java.util.Date() + " " + host + " MatlabTask] Starting a new JVM");
            outDebug.println("[" + new java.util.Date() + " " + host + " MatlabTask] Starting a new JVM");
        }
        MatlabJVMInfo jvminfo = jvmInfos.get(nodeName);
        // Build java command
        javaCommandBuilder = new DummyJVMProcess();
        javaCommandBuilder.setClassname(StartPARuntime.class.getName());

        int deployid = new SecureRandom().nextInt();
        jvminfo.setDeployID(deployid);

        registrationListener = new RegistrationListener();
        registrationListener.subscribeJMXRuntimeEvent();

        startingProcess = true;

        javaCommandBuilder.setParameters("-d " + jvminfo.getDeployID() + " -c 1 -p " +
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

        // used to kill the process later
        env.put("NODE_NAME", nodeName);

        // we add matlab directories to PATH (Windows)
        String path = env.get("PATH");

        if (path == null) {
            path = env.get("Path");
        }

        env.put("PATH", addPtolemyLibDirToPath(addMatlabToPath(path)));

        // we set as well the java.library.path property (precaution), we forward as well the RMI port in use

        javaCommandBuilder.setJvmOptions("-Djava.library.path=\"" + libPath + "\"" +
            " -Dproactive.rmi.port=" + CentralPAPropertyRepository.PA_RMI_PORT.getValue());

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

    class RegistrationListener implements NotificationListener {

        private void subscribeJMXRuntimeEvent() {
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] Subscribe JMX Runtime");
                outDebug.println("[" + new java.util.Date() + " " + host +
                    " MATLAB TASK] Subscribe JMX Runtime");

            }
            MatlabJVMInfo jvminfo = jvmInfos.get(nodeName);
            ProActiveRuntimeImpl part = ProActiveRuntimeImpl.getProActiveRuntime();
            part.addDeployment(jvminfo.getDeployID());
            JMXNotificationManager.getInstance().subscribe(part.getMBean().getObjectName(), this);
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host + " MATLAB TASK] Subscribed");
                outDebug.println("[" + new java.util.Date() + " " + host + " MATLAB TASK] Subscribed");
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
                        System.out.println("[" + new java.util.Date() + " " + host +
                            " MATLAB TASK] Notification received");
                        outDebug.println("[" + new java.util.Date() + " " + host +
                            " MATLAB TASK] Notification received");
                    }
                    GCMRuntimeRegistrationNotificationData data = (GCMRuntimeRegistrationNotificationData) notification
                            .getUserData();
                    MatlabJVMInfo jvminfo = jvmInfos.get(nodeName);
                    if (data.getDeploymentId() != jvminfo.getDeployID()) {
                        return;
                    }
                    if (debug) {
                        System.out.println("[" + new java.util.Date() + " " + host +
                            " MATLAB TASK] Notification accepted");
                        outDebug.println("[" + new java.util.Date() + " " + host +
                            " MATLAB TASK] Notification accepted");
                        outDebug.flush();
                    }

                    ProActiveRuntime childRuntime = data.getChildRuntime();
                    if (debug) {
                        System.out.println("[" + new java.util.Date() + " " + host +
                            " MATLAB TASK] Creating Node");
                        outDebug.println("[" + new java.util.Date() + " " + host +
                            " MATLAB TASK] Creating Node");
                    }
                    Node scilabNode = null;
                    try {
                        scilabNode = childRuntime.createLocalNode("Matlab_" + nodeName + "_" + nodeCount,
                                true, null, null, null);
                    } catch (Exception e) {
                        if (debug) {
                            e.printStackTrace();
                            e.printStackTrace(outDebug);
                        }
                        throw e;
                    }
                    nodeCount++;
                    if (debug) {
                        System.out.println("[" + new java.util.Date() + " " + host +
                            " MATLAB TASK] Node Created : " + scilabNode);
                        outDebug.println("[" + new java.util.Date() + " " + host +
                            " MATLAB TASK] Node Created :" + scilabNode);
                    }
                    jvminfo.setNode(scilabNode);

                    if (debug) {
                        System.out.println("[" + new java.util.Date() + " " + host +
                            " MATLAB TASK] waking up main thread");
                        outDebug.println("[" + new java.util.Date() + " " + host +
                            " MATLAB TASK] waking up main thread");

                    }
                    semaphore.release();

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

    /**
     * An utility class to build the Java command
     *
     * @author The ProActive Team
     */
    public static class DummyJVMProcess extends JVMProcessImpl implements Serializable {

        /**  */
		private static final long serialVersionUID = 21L;

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
