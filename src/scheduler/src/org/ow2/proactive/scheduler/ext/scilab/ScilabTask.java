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
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.common.util.IOTools.LoggingThread;
import org.ow2.proactive.scheduler.ext.scilab.util.ScilabConfiguration;
import org.ow2.proactive.scheduler.ext.scilab.util.ScilabFinder;
import org.ow2.proactive.scheduler.ext.scilab.util.ScilabJVMInfo;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


/**
 * This class represents a Scilab-specific Task inside the Scheduler
 * @author The ProActive Team
 */
public class ScilabTask extends JavaExecutable {

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
     * Scilab Function name
     */

    protected String functionName = null;

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

    protected static Thread shutdownHook = null;

    private static boolean threadstarted = false;

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

    private PrintStream outDebug;

    protected static int nodeCount = 0;

    private static final long SEMAPHORE_TIMEOUT = 2;
    private static final int RETRY_ACQUIRE = 30;

    private static final int MAX_NB_ATTEMPTS = 5;
    private Semaphore semaphore = new Semaphore(0);

    private RegistrationListener registrationListener;

    protected static boolean startingProcess = false;

    private static boolean redeploying = false;

    private int nbAttempts = 0;

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

        nodeName = PAActiveObject.getNode().getVMInformation().getName().replace('-', '_') + "_" +
            PAActiveObject.getNode().getNodeInformation().getName().replace('-', '_');
        if (debug) {
            // system temp dir
            String tmpPath = System.getProperty("java.io.tmpdir");

            // log file writer used for debugging
            File logFile = new File(tmpPath, "ScilabTask" + nodeName + ".log");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            outDebug = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile, true)));
        }

        ScilabJVMInfo jvminfo = jvmInfos.get(nodeName);
        if (jvminfo == null) {
            jvminfo = new ScilabJVMInfo();
            jvmInfos.put(nodeName, jvminfo);
        }

        Serializable res = null;

        nbAttempts = 1;

        redeploying = false;

        while (res == null) {

            handleProcess(jvminfo);

            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host +
                    " ScilabTask] Executing the task");
                outDebug.println("[" + new java.util.Date() + " " + host + " ScilabTask] Executing the task");
            }

            try {
                // finally we call the internal version of the execute method
                redeploying = false;
                res = executeInternal(results);
            } catch (UnsatisfiedLinkError e) {
                redeploy(e, jvminfo,
                        "Scilab Engine couldn't initialize, this can happen sometimes even when paths are correct");
            } catch (NoClassDefFoundError e) {
                redeploy(e, jvminfo,
                        "Scilab Engine couldn't initialize, this can happen sometimes even when paths are correct");
            } catch (FutureMonitoringPingFailureException e) {
                redeploy(e, jvminfo, "Scilab Engine crashed");
            } catch (ScilabInitializationException e) {
                redeploy(e, jvminfo, "Scilab Engine initialization hanged");

            } finally {
                if (res != null) {
                    jvminfo.getIsLogger().closeStream();
                    jvminfo.getEsLogger().closeStream();
                }
                if (res != null && debug) {
                    outDebug.println("[" + new java.util.Date() + " " + host + " ScilabTask] Closing output");
                    outDebug.close();
                } else if (nbAttempts >= MAX_NB_ATTEMPTS && debug) {
                    outDebug.println("[" + new java.util.Date() + " " + host + " ScilabTask] Closing output");
                    outDebug.close();
                }
            }
        }

        return res;
    }

    private void redeploy(Throwable e, ScilabJVMInfo jvminfo, String message) throws Throwable {
        redeploying = true;
        if (debug) {
            e.printStackTrace(outDebug);
            System.out.println("[" + new java.util.Date() + " " + host + " ScilabTask] " + message +
                ", redeploying");
            outDebug.println("[" + new java.util.Date() + " " + host + " ScilabTask] " + message +
                ", redeploying");
        }

        destroyProcess(jvminfo);

        if (nbAttempts >= MAX_NB_ATTEMPTS) {
            throw e;
        }
        nbAttempts++;
    }

    protected void destroyProcess(ScilabJVMInfo jvminfo) {

        try {
            jvminfo.getNode().killAllActiveObjects();
        } catch (Exception e1) {
        }

        jvminfo.getProcess().destroy();

        //jvminfo.getProcess().destroy();

        jvminfo.setProcess(null);
        jvminfo.setWorker(null);
        removeShutdownHook();

    }

    protected void addShutdownHook() {
        shutdownHook = new Thread(new Runnable() {
            public void run() {
                for (ScilabJVMInfo info : jvmInfos.values()) {
                    try {
                        destroyProcess(info);
                    } catch (Exception e) {
                    }
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        shutdownhookSet = true;
    }

    protected void removeShutdownHook() {
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
        shutdownHook = null;
        shutdownhookSet = false;
    }

    protected void handleProcess(ScilabJVMInfo jvminfo) throws Throwable {
        if (jvminfo.getProcess() == null) {

            if (scilabConfig == null) {
                // First we try to find SCILAB
                if (debug) {
                    System.out.println("[" + new java.util.Date() + " " + host +
                        " ScilabTask] launching script to find Scilab");
                    outDebug.println("[" + new java.util.Date() + " " + host +
                        " ScilabTask] launching script to find Scilab");
                }
                scilabConfig = ScilabFinder.findScilab(debug);

                if (debug) {
                    System.out.println("[" + new java.util.Date() + " " + host + " ScilabTask] " +
                        scilabConfig);
                    outDebug
                            .println("[" + new java.util.Date() + " " + host + " ScilabTask] " + scilabConfig);
                }
            }

            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host +
                    " ScilabTask] Starting the Java Process");
                outDebug.println("[" + new java.util.Date() + " " + host +
                    " ScilabTask] Starting the Java Process");
            }
            // We spawn a new JVM with the SCILAB library paths
            Process p = startProcess();
            jvminfo.setProcess(p);

            // We add a shutdownhook to terminate children processes
            if (!shutdownhookSet) {
                addShutdownHook();
            }
        }

        if (!threadstarted) {

            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host +
                    " ScilabTask] Starting the Threads");
                outDebug.println("[" + new java.util.Date() + " " + host +
                    " ScilabTask] Starting the Threads");
            }

            LoggingThread lt1 = null;
            LoggingThread lt2 = null;

            if (debug) {
                lt1 = new LoggingThread(jvminfo.getProcess().getInputStream(), "[" + host + " OUT]",
                    System.out, outDebug);
                lt2 = new LoggingThread(jvminfo.getProcess().getErrorStream(), "[" + host + " ERR]",
                    System.err, outDebug);
            } else {
                lt1 = new LoggingThread(jvminfo.getProcess().getInputStream(), "[" + host + " OUT]",
                    System.out);
                lt2 = new LoggingThread(jvminfo.getProcess().getErrorStream(), "[" + host + " ERR]",
                    System.err);
            }

            IOTools.RedirectionThread rt1 = new IOTools.RedirectionThread(System.in, jvminfo.getProcess()
                    .getOutputStream());

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
            threadstarted = true;

        } else if (startingProcess) {
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host +
                    " ScilabTask] Connecting process out to threads");
                outDebug.println("[" + new java.util.Date() + " " + host +
                    " ScilabTask] Connecting process out to threads");
            }
            jvminfo.getIsLogger().setInputStream(jvminfo.getProcess().getInputStream());
            jvminfo.getEsLogger().setInputStream(jvminfo.getProcess().getErrorStream());
            jvminfo.getIoThread().setOutputStream(jvminfo.getProcess().getOutputStream());

            if (!redeploying) {
                if (debug) {
                    jvminfo.getIsLogger().setStream(System.out, outDebug);
                    jvminfo.getEsLogger().setStream(System.err, outDebug);
                } else {
                    jvminfo.getIsLogger().setStream(System.out);
                    jvminfo.getEsLogger().setStream(System.err);
                }
            }

            startingProcess = false;
        } else {
            if (debug) {
                jvminfo.getIsLogger().setStream(System.out, outDebug);
                jvminfo.getEsLogger().setStream(System.err, outDebug);
            } else {
                jvminfo.getIsLogger().setStream(System.out);
                jvminfo.getEsLogger().setStream(System.err);
            }
        }

        AOScilabWorker sw = jvminfo.getWorker();
        if (sw == null) {
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host +
                    " ScilabTask] waiting for deployment");
                outDebug.println("[" + new java.util.Date() + " " + host +
                    " ScilabTask] waiting for deployment");
            }
            waitForRegistration();

            sw = deploy();

            registrationListener.unsubscribeJMXRuntimeEvent();
        }

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

        String funcn = (String) args.get("functionName");
        if (funcn != null) {
            functionName = funcn;
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
        ScilabJVMInfo jvminfo = jvmInfos.get(nodeName);
        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + host + " ScilabTask] Deploying the Worker");
            outDebug.println("[" + new java.util.Date() + " " + host + " ScilabTask] Deploying the Worker");
        }

        final AOScilabWorker worker = (AOScilabWorker) PAActiveObject.newActive(AOScilabWorker.class
                .getName(), new Object[] { scilabConfig }, jvminfo.getNode());

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
        ScilabJVMInfo jvminfo = jvmInfos.get(nodeName);
        AOScilabWorker sw = jvminfo.getWorker();

        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + host + " ScilabTask] Initializing");
            outDebug.println("[" + new java.util.Date() + " " + host + " ScilabTask] Initializing");
        }

        sw.init(inputScript, functionName, functionsDefinition, scriptLines, out_set, debug);

        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + host + " ScilabTask] Executing");
            outDebug.println("[" + new java.util.Date() + " " + host + " ScilabTask] Executing");
        }

        // We execute the task on the worker
        Serializable res = sw.execute(results);
        // We wait for the result
        res = (Serializable) PAFuture.getFutureValue(res);

        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + host + " ScilabTask] Received result");
            outDebug.println("[" + new java.util.Date() + " " + host + " ScilabTask] Received result");
        }

        return res;
    }

    private final Process startProcess() throws Throwable {
        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + host + " ScilabTask] Starting a new JVM");
            outDebug.println("[" + new java.util.Date() + " " + host + " ScilabTask] Starting a new JVM");
        }
        ScilabJVMInfo jvminfo = jvmInfos.get(nodeName);
        // Build java command
        javaCommandBuilder = new DummyJVMProcess();
        javaCommandBuilder.setClassname(StartPARuntime.class.getName());
        int deployid = new SecureRandom().nextInt();
        jvminfo.setDeployID(deployid);

        registrationListener = new RegistrationListener();
        registrationListener.subscribeJMXRuntimeEvent();

        startingProcess = true;

        javaCommandBuilder.setParameters("-d " + deployid + " -c 1 -p " +
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
        env.put("SCI_DISABLE_TK", "1");

        // Classpath specific
        String classpath = addScilabJarsToClassPath(javaCommandBuilder.getClasspath());
        javaCommandBuilder.setClasspath(classpath);

        // We add the Scilab specific env variables
        env.put("SCI", scilabConfig.getScilabSCIDir());
        env.put("SCIDIR", scilabConfig.getScilabSCIDir());

        // javaCommandBuilder.setJavaPath(System.getenv("JAVA_HOME") +
        //     "/bin/java");
        // we set as well the java.library.path property (precaution)
        if (CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue().equals("pamr")) {
            String jvmOptions = "-Djava.library.path=\"" + libPath + "\"" +
                " -Dproactive.communication.protocol=" +
                CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue();
            if (PAMRConfig.PA_NET_ROUTER_ADDRESS.isSet()) {
                jvmOptions += " -Dproactive.net.router.address=" +
                    PAMRConfig.PA_NET_ROUTER_ADDRESS.getValue();
            }
            if (PAMRConfig.PA_NET_ROUTER_PORT.isSet()) {
                jvmOptions += " -Dproactive.net.router.port=" + PAMRConfig.PA_NET_ROUTER_PORT.getValue();
            }
            if (PAMRConfig.PA_PAMR_SOCKET_FACTORY.isSet()) {
                jvmOptions += " -Dproactive.communication.pamr.socketfactory=" +
                    PAMRConfig.PA_PAMR_SOCKET_FACTORY.getValue();
            }
            if (PAMRConfig.PA_PAMRSSH_KEY_DIR.isSet()) {
                jvmOptions += " -Dproactive.communication.pamrssh.key_directory=" +
                    PAMRConfig.PA_PAMRSSH_KEY_DIR.getValue();
            }
            if (PAMRConfig.PA_PAMRSSH_REMOTE_USERNAME.isSet()) {
                jvmOptions += " -Dproactive.communication.pamrssh.username=" +
                    PAMRConfig.PA_PAMRSSH_REMOTE_USERNAME.getValue();
            }
            if (PAMRConfig.PA_PAMRSSH_REMOTE_PORT.isSet()) {
                jvmOptions += " -Dproactive.communication.pamrssh.port=" +
                    PAMRConfig.PA_PAMRSSH_REMOTE_PORT.getValue();
            }

            javaCommandBuilder.setJvmOptions(jvmOptions);
        } else {
            javaCommandBuilder.setJvmOptions("-Djava.library.path=\"" + libPath + "\"" +
                " -Dproactive.rmi.port=" + CentralPAPropertyRepository.PA_RMI_PORT.getValue() +
                " -Dproactive.communication.protocol=" +
                CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue());
        }

        pb.command(javaCommandBuilder.getJavaCommand());

        return pb.start();
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
                System.out
                        .println("[" + new java.util.Date() + " " + host +
                            " ScilabTask] Unable to create a separate java process after " + RETRY_ACQUIRE +
                            " tries");
                outDebug.println("[" + new java.util.Date() + " " + host +
                    " ScilabTaskq] Unable to create a separate java process after " + RETRY_ACQUIRE +
                    " tries");
            }
            throw new IllegalStateException("Unable to create a separate java process after " +
                RETRY_ACQUIRE + " tries");
        }

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

    class RegistrationListener implements NotificationListener {

        private void subscribeJMXRuntimeEvent() {
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host +
                    " ScilabTask] Subscribe JMX Runtime");
                outDebug.println("[" + new java.util.Date() + " " + host +
                    " ScilabTask] Subscribe JMX Runtime");

            }
            ScilabJVMInfo jvminfo = jvmInfos.get(nodeName);
            ProActiveRuntimeImpl part = ProActiveRuntimeImpl.getProActiveRuntime();
            part.addDeployment(jvminfo.getDeployID());
            JMXNotificationManager.getInstance().subscribe(part.getMBean().getObjectName(), this);
            if (debug) {
                System.out.println("[" + new java.util.Date() + " " + host + " ScilabTask] Subscribed");
                outDebug.println("[" + new java.util.Date() + " " + host + " ScilabTask] Subscribed");
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
                            " ScilabTask] Notification received");
                        outDebug.println("[" + new java.util.Date() + " " + host +
                            " ScilabTask] Notification received");
                    }
                    GCMRuntimeRegistrationNotificationData data = (GCMRuntimeRegistrationNotificationData) notification
                            .getUserData();
                    ScilabJVMInfo jvminfo = jvmInfos.get(nodeName);
                    if (data.getDeploymentId() != jvminfo.getDeployID()) {
                        return;
                    }
                    if (debug) {
                        System.out.println("[" + new java.util.Date() + " " + host +
                            " ScilabTask] Notification accepted");
                        outDebug.println("[" + new java.util.Date() + " " + host +
                            " ScilabTask] Notification accepted");
                        outDebug.flush();
                    }

                    ProActiveRuntime childRuntime = data.getChildRuntime();
                    if (debug) {
                        System.out.println("[" + new java.util.Date() + " " + host +
                            " ScilabTask] Creating Node");
                        outDebug.println("[" + new java.util.Date() + " " + host +
                            " ScilabTask] Creating Node");
                    }
                    Node scilabNode = null;
                    try {
                        scilabNode = childRuntime.createLocalNode("Scilab_" + nodeName + "_" + nodeCount,
                                true, null, null, null);
                    } catch (Throwable e) {
                        if (debug) {
                            e.printStackTrace();
                            e.printStackTrace(outDebug);
                            outDebug.flush();
                        }
                        throw new IllegalStateException(e);
                    }
                    nodeCount++;
                    if (debug) {
                        System.out.println("[" + new java.util.Date() + " " + host +
                            " ScilabTask] Node Created : " + scilabNode);
                        outDebug.println("[" + new java.util.Date() + " " + host +
                            " ScilabTask] Node Created :" + scilabNode);
                    }
                    jvminfo.setNode(scilabNode);

                    if (debug) {
                        System.out.println("[" + new java.util.Date() + " " + host +
                            " ScilabTask] waking up main thread");
                        outDebug.println("[" + new java.util.Date() + " " + host +
                            " ScilabTask] waking up main thread");

                    }

                }
            } catch (Throwable e) {
                e.printStackTrace();
                if (debug) {
                    e.printStackTrace(outDebug);
                    outDebug.flush();
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
    private static class DummyJVMProcess extends JVMProcessImpl {

        /**  */
        private static final long serialVersionUID = 21L;

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
