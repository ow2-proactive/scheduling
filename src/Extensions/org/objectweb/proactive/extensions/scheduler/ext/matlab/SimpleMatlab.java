/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extensions.scheduler.ext.matlab;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.JavaExecutable;
import org.objectweb.proactive.extensions.scheduler.ext.common.util.IOTools;
import org.objectweb.proactive.extensions.scheduler.ext.common.util.IOTools.LoggingThread;
import org.objectweb.proactive.extensions.scheduler.ext.matlab.exception.MatlabInitException;
import org.objectweb.proactive.extensions.scheduler.ext.matlab.util.MatlabConfiguration;
import org.objectweb.proactive.extensions.scheduler.ext.matlab.util.MatlabFinder;


public class SimpleMatlab extends JavaExecutable {

    /**
     * log4j logger 
     */
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER_MATLAB_EXT);

    /**
     *  This hostname, for debugging purpose
     */
    protected static String host = null;

    /**
     *  the index when the input is the result of a SplitTask
     */
    protected int index = -1;

    /**
     *  the lines of inputScript
     */
    protected String inputScript = null;

    /**
     *  The lines of the Matlab script
     */
    protected ArrayList<String> scriptLines = null;

    /**
     *  The URI to which the spawned JVM(Node) is registered
     */
    protected static String uri = null;

    /**
     *  Thread which collects the JVM's stdout
     */
    private LoggingThread isLogger = null;
    /**
     *  Thread which collects the JVM's stderr
     */
    private LoggingThread esLogger = null;

    /**
     *  tool to build the JavaCommand
     */
    private DummyJVMProcess javaCommandBuilder;

    /**
     *  holds the Matlab environment information on this machine
     */
    protected static MatlabConfiguration matlabConfig = null;

    /**
     *  the Active Object worker located in the spawned JVM
     */
    private static AOSimpleMatlab matlabWorker = null;

    /**
     *  the OS where this JVM is running
     */
    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    /**
     *  The process holding the spanwned JVM
     */
    private static Process process = null;

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
    public SimpleMatlab() {

    }

    /**
     * Convenience constructor
     * @param inputScript script that will be launched and will produce an input to the main script
     * @param mainScript main script to execute
     */
    public SimpleMatlab(String inputScript, String mainScript) {
        this.inputScript = inputScript;
        this.scriptLines = new ArrayList<String>();
        this.scriptLines.add(mainScript);
    }

    @Override
    public Object execute(TaskResult... results) throws Throwable {
        for (TaskResult res : results) {
            if (res.hadException()) {
                throw res.getException();
            }
        }
        if (process == null) {
            // First we try to find MATLAB
            if (logger.isDebugEnabled()) {
                System.out.println("[" + host + " MATLAB TASK] Looking for Matlab...");
            }
            matlabConfig = MatlabFinder.findMatlab();
            if (logger.isDebugEnabled()) {
                System.out.println(matlabConfig);
            }

            // We create a custom URI as the node name
            uri = URIBuilder.buildURI("localhost", "Matlab" + (new Date()).getTime(),
                    Constants.RMI_PROTOCOL_IDENTIFIER, Integer.parseInt(PAProperties.PA_RMI_PORT.getValue()))
                    .toString();
            if (logger.isDebugEnabled()) {
                System.out.println("[" + host + " MATLAB TASK] Starting the Java Process");
            }

            // We spawn a new JVM with the MATLAB library paths
            process = startProcess(uri);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    process.destroy();
                }
            }));

            // We define the loggers which will write on standard output what comes from the java process
            isLogger = new LoggingThread(process.getInputStream(), "[" + host +
                " MATLAB TASK: SUBPROCESS OUT]");
            esLogger = new LoggingThread(process.getErrorStream(), "[" + host +
                " MATLAB TASK: SUBPROCESS ERR]");

            // We start the loggers thread
            Thread t1 = new Thread(isLogger);
            t1.start();

            Thread t2 = new Thread(esLogger);
            t2.start();

        }

        if (logger.isDebugEnabled()) {
            System.out.println("[" + host + " MATLAB TASK] Executing the task");
        }

        // finally we call the internal version of the execute method
        Object res = executeInternal(uri, results);

        return res;
    }

    @Override
    public void init(Map<String, Object> args) throws Exception {
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

        host = java.net.InetAddress.getLocalHost().getHostName();
    }

    /**
     * Deploy an Active Object on the given Node uri
     * @param uri uri of the Node where to deploy the AO
     * @throws Throwable
     */
    protected AOSimpleMatlab deploy(String uri, String workerClassName, Object... params) throws Throwable {
        ProActiveException ex = null;
        AOSimpleMatlab worker = null;
        if (logger.isDebugEnabled()) {
            System.out.println("[" + host + " MATLAB TASK] Deploying the Worker");
        }

        // We create an active object on the given node URI, the JVM corresponding to this node URI is starting,
        // so we retry for 30 seconds until the JVM has started and we can create the Active Object
        for (int i = 0; i < 30; i++) {
            try {
                try {
                    worker = (AOSimpleMatlab) PAActiveObject.newActive(workerClassName, params, uri);
                } catch (ProActiveException e) {
                    ex = e;
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (worker == null) {
            System.err.println("[" + host + " MATLAB TASK] Worker couldn't be deployed.");
            throw ex;
        }

        return worker;
    }

    /**
     * Internal version of the execute method
     * @param uri a URI to which the spawned JVM is registered
     * @param results results from preceding tasks
     * @return result of the task
     * @throws Throwable
     */
    protected Object executeInternal(String uri, TaskResult... results) throws Throwable {
        if (logger.isDebugEnabled()) {
            System.out.println("[" + host + " MATLAB TASK] Deploying Worker (SimpleMatlab)");
        }
        if (matlabWorker == null) {
            matlabWorker = deploy(uri, AOSimpleMatlab.class.getName(), matlabConfig.getMatlabCommandName());

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    matlabWorker.terminate();
                }
            }));
        }
        if (logger.isDebugEnabled()) {
            System.out.println("[" + host + " MATLAB TASK] Executing (SimpleMatlab)");
        }
        matlabWorker.init(inputScript, scriptLines);

        // We execute the task on the worker
        Object res = matlabWorker.execute(index, results);
        // We wait for the result
        res = PAFuture.getFutureValue(res);
        // We make a synchronous call to terminate
        //matlabWorker.terminate();

        return res;
    }

    /**
     * Starts the java process on the given Node uri
     * @param uri
     * @return process
     * @throws Throwable
     */
    private final Process startProcess(String uri) throws Throwable {
        if (logger.isDebugEnabled()) {
            System.out.println("[" + host + " MATLAB TASK] Starting a new JVM");
        }
        // Build java command
        javaCommandBuilder = new DummyJVMProcess();
        // the uri to use to create the node
        javaCommandBuilder.setParameters(uri);

        // We build the process with a separate environment
        ProcessBuilder pb = new ProcessBuilder();

        // Setting Environment variables
        Map<String, String> env = pb.environment();

        // Classpath specific
        String classpath = prependPtolemyLibDirToClassPath(javaCommandBuilder.getClasspath());
        javaCommandBuilder.setClasspath(classpath);

        // we add matlab directories to LD_LIBRARY_PATH
        String libPath = env.get("LD_LIBRARY_PATH");
        libPath = addMatlabToPath(libPath);

        env.put("LD_LIBRARY_PATH", libPath);

        // we add matlab directories to PATH (Windows)
        String path = env.get("PATH");

        if (path == null) {
            path = env.get("Path");
        }

        env.put("PATH", addMatlabToPath(path));

        // we set as well the java.library.path property (precaution)
        javaCommandBuilder.setJvmOptions("-Djava.library.path=\"" + libPath + "\"");

        pb.command(javaCommandBuilder.getJavaCommand());

        return pb.start();
    }

    private String prependPtolemyLibDirToClassPath(String classPath) throws IOException, URISyntaxException,
            MatlabInitException {
        String newcp = classPath;
        newcp = matlabConfig.getPtolemyPath() + os.pathSeparator() + newcp;
        return newcp;
    }

    /**
     * Utility function to add MATLAB directories to the given path-like string
     * @param path path-like string
     * @return an augmented path
     */
    private String addMatlabToPath(String path) {
        String newPath;

        if (path == null) {
            newPath = "";
        } else {
            newPath = path + os.pathSeparator();
        }

        String lastDir = null;
        int lastIndex = matlabConfig.getMatlabLibDirName().lastIndexOf(os.fileSeparator());
        if (lastIndex != -1) {
            lastDir = matlabConfig.getMatlabLibDirName().substring(lastIndex + 1);
        } else {
            lastDir = matlabConfig.getMatlabLibDirName();
        }

        newPath = newPath + (matlabConfig.getMatlabHome() + os.fileSeparator() + "bin");
        newPath = newPath + os.pathSeparator() +
            (matlabConfig.getMatlabHome() + os.fileSeparator() + matlabConfig.getMatlabLibDirName());
        newPath = newPath +
            os.pathSeparator() +
            (matlabConfig.getMatlabHome() + os.fileSeparator() + "sys" + os.fileSeparator() + "os" +
                os.fileSeparator() + lastDir);

        return newPath;
    }

    /**
     * An utility class to build the Java command
     * @author The ProActive Team
     *
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
