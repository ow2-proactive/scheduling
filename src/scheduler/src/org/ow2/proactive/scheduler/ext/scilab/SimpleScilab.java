/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.scheduler.ext.scilab;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.common.util.IOTools.LoggingThread;
import org.ow2.proactive.scheduler.ext.scilab.util.ScilabConfiguration;
import org.ow2.proactive.scheduler.ext.scilab.util.ScilabFinder;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.*;


public class SimpleScilab extends JavaExecutable {

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
     * The URI to which the spawned JVM(Node) is registered
     */
    protected static String uri = null;

    /**
     * Thread which collects the JVM's stderr
     */
    protected static Map<String, LoggingThread> esLogger = new HashMap<String, LoggingThread>();
    /**
     * Thread which collects the JVM's stdout
     */
    protected static Map<String, LoggingThread> isLogger = new HashMap<String, LoggingThread>();

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
    protected static Map<String, AOSimpleScilab> scilabWorker = new HashMap<String, AOSimpleScilab>();

    /**
     * the OS where this JVM is running
     */
    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    /**
     * The process holding the spawned JVM
     */
    protected static Map<String, Process> processes = new HashMap<String, Process>();

    /**
     * ProActive No Arg Constructor
     */
    public SimpleScilab() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.proactive.scheduler.common.task.Executable#execute(org.ow2.proactive.scheduler.common.task.TaskResult[])
     */
    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        for (TaskResult res : results) {
            if (res.hadException()) {
                throw res.getException();
            }
        }
        nodeName = PAActiveObject.getNode().getNodeInformation().getName();
        if (processes.get(nodeName) == null) {

            // First we try to find SCILAB
            if (debug) {
                System.out.println("[" + host + " SimpleScilab] launching script to find Scilab");
            }
            scilabConfig = ScilabFinder.findScilab(debug);
            if (debug) {
                System.out.println(scilabConfig);
            }

            // We create a custom URI as the node name
            //uri = URIBuilder.buildURI("localhost", "Scilab" + (new Date()).getTime()).toString();
            uri = URIBuilder.buildURI(ProActiveInet.getInstance().getHostname(),
                    "Scilab" + (new Date()).getTime(), Constants.RMI_PROTOCOL_IDENTIFIER,
                    Integer.parseInt(PAProperties.PA_RMI_PORT.getValue())).toString();
            if (debug) {
                System.out.println("[" + host + " SimpleScilab] Starting the Java Process");
            }
            // We spawn a new JVM with the SCILAB library paths
            Process p = startProcess(uri);
            processes.put(nodeName, p);

            // We add a shutdownhook to terminate children processes
            if (!shutdownhookSet) {
                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    public void run() {
                        for (Process p : processes.values()) {
                            p.destroy();
                        }
                    }
                }));
                shutdownhookSet = true;
            }

            LoggingThread lt1 = new LoggingThread(p.getInputStream(), "[" + host + " OUT]", false);
            LoggingThread lt2 = new LoggingThread(p.getErrorStream(), "[" + host + " ERR]", true);
            IOTools.RedirectionThread rt1 = new IOTools.RedirectionThread(System.in, p.getOutputStream());

            // We define the loggers which will write on standard output what comes from the java process
            isLogger.put(nodeName, lt1);
            esLogger.put(nodeName, lt2);

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
            System.out.println("[" + host + " SimpleScilab] Executing the task");
        }

        // finally we call the internal version of the execute method
        Serializable res = executeInternal(uri, results);

        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.proactive.scheduler.common.task.JavaExecutable#init(java.util.Map)
     */
    @Override
    public void init(Map<String, String> args) throws Exception {
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

        String d = args.get("debug");
        if (d != null) {
            debug = Boolean.parseBoolean(d);
        }

        String input = args.get("input");

        if (input != null) {
            inputScript = input;
        }

        String functionsDef = args.get("functionsDefinition");
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
     * Deploy an Active Object on the given Node uri
     *
     * @param uri             uri of the Node where to deploy the AO
     * @param workerClassName name of the worker class
     * @param params          parameters of the constructor
     * @throws Throwable
     */
    protected AOSimpleScilab deploy(String uri, String workerClassName, Object... params) throws Throwable {
        Exception ex = null;
        AOSimpleScilab worker = null;
        if (debug) {
            System.out.println("[" + host + " SimpleScilab] Deploying the Worker");
        }

        // We create an active object on the given node URI, the JVM corresponding to this node URI is starting,
        // so we retry for 30 seconds until the JVM has started and we can create the Active Object
        for (int i = 0; i < 50; i++) {
            try {
                try {
                    worker = (AOSimpleScilab) PAActiveObject.newActive(workerClassName, params, uri);
                    if (worker != null) {
                        break;
                    }
                } catch (Exception e) {
                    ex = e;
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (worker == null) {
            if (debug) {
                System.err.println("[" + host + " SimpleScilab] Worker couldn't be deployed.");
            }
            throw ex;
        }

        return worker;
    }

    /**
     * Internal version of the execute method
          *
          * @param uri     a URI to which the spawned JVM is registered
          * @param results results from preceding tasks
          * @return result of the task
          * @throws Throwable
          */
    protected Serializable executeInternal(String uri, TaskResult... results) throws Throwable {
        AOSimpleScilab sw = scilabWorker.get(nodeName);
        if (sw == null) {
            sw = deploy(uri, AOSimpleScilab.class.getName(), scilabConfig);
            scilabWorker.put(nodeName, sw);
        }
        if (debug) {
            System.out.println("[" + host + " SimpleScilab] Executing");
        }
        try {
            sw.init(inputScript, functionsDefinition, scriptLines, out_set, debug);
        } catch (Exception e) {
            // in case the active object died
            if (debug) {
                System.out.println("[" + host + " SimpleScilab] Re-deploying Worker");
            }
            sw = deploy(uri, AOSimpleScilab.class.getName(), scilabConfig);
            scilabWorker.put(nodeName, sw);
            sw.init(inputScript, functionsDefinition, scriptLines, out_set, debug);
        }

        // We execute the task on the worker
        Serializable res = sw.execute(results);
        // We wait for the result
        res = (Serializable) PAFuture.getFutureValue(res);
        // We make a synchronous call to terminate
        //scilabWorker.terminate();

        return res;
    }

    private final Process startProcess(String uri) throws Throwable {
        if (debug) {
            System.out.println("[" + host + " SimpleScilab] Starting a new JVM");
        }
        // Build java command
        javaCommandBuilder = new DummyJVMProcess();
        // the uri to use to create the node
        javaCommandBuilder.setParameters(uri);

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

        // Classpath specific
        String classpath = addScilabJarsToClassPath(javaCommandBuilder.getClasspath());
        javaCommandBuilder.setClasspath(classpath);

        // We add the Scilab specific env variables
        env.put("SCI", scilabConfig.getScilabSCIDir());
        env.put("SCIDIR", scilabConfig.getScilabSCIDir());

        // javaCommandBuilder.setJavaPath(System.getenv("JAVA_HOME") +
        //     "/bin/java");
        // we set as well the java.library.path property (precaution)
        // "-Djava.library.path=\"" + libPath + "\"" +
        javaCommandBuilder.setJvmOptions(" -Dproactive.rmi.port=" +
            Integer.parseInt(PAProperties.PA_RMI_PORT.getValue()) + " -Dproactive.http.port=" +
            Integer.parseInt(PAProperties.PA_XMLHTTP_PORT.getValue()));

        pb.command(javaCommandBuilder.getJavaCommand());

        return pb.start();
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

        return newPath;
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
