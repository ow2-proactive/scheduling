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
package org.ow2.proactive.scheduler.ext.scilab;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.common.util.IOTools.LoggingThread;
import org.ow2.proactive.scheduler.ext.scilab.util.ScilabConfiguration;
import org.ow2.proactive.scheduler.ext.scilab.util.ScilabFinder;
import org.ow2.proactive.scheduler.util.SchedulerLoggers;
import org.apache.log4j.Logger;


public class SimpleScilab extends JavaExecutable {

    final private static String[] DEFAULT_OUT_VARIABLE_SET = { "out" };

    protected static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.SCILAB);
    protected static boolean debug = logger.isDebugEnabled();

    /**
     * This hostname, for debugging purpose
     */
    protected String host;

    /**
     * the lines of inputScript
     **/
    protected String inputScript = null;

    /**
     * The lines of the Scilab script
     */
    protected List<String> scriptLines = null;

    /**
     * the array of output variable names
     */
    protected String[] out_set = DEFAULT_OUT_VARIABLE_SET;

    /**
     * The URI to which the spawned JVM(Node) is registered
     */
    protected String uri = null;

    /**
     *  Thread which collects the JVM's stderr      
     */
    private LoggingThread esLogger = null;
    /**
     *  Thread which collects the JVM's stdout      
     */
    private LoggingThread isLogger = null;

    /**
     *  tool to build the JavaCommand
     */
    private DummyJVMProcess javaCommandBuilder;

    /**
     * holds the Scilab environment information
     */
    protected static ScilabConfiguration scilabConfig = null;

    /**
     *  the Active Object worker located in the spawned JVM
     */
    private AOSimpleScilab scilabWorker;

    /**
     *  the OS where this JVM is running
     */
    private OperatingSystem os = OperatingSystem.getOperatingSystem();

    /**
     *  The process holding the spawned JVM
     */
    private Process process = null;

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
    public Object execute(TaskResult... results) throws Throwable {
        for (TaskResult res : results) {
            if (res.hadException()) {
                throw res.getException();
            }
        }

        // First we try to find SCILAB
        if (debug) {
            System.out.println("[" + host + " SCILAB TASK] launching script to find Scilab");
        }
        scilabConfig = ScilabFinder.findScilab();
        if (debug) {
            System.out.println(scilabConfig);
        }

        // We create a custom URI as the node name
        //uri = URIBuilder.buildURI("localhost", "Scilab" + (new Date()).getTime()).toString();
        uri = URIBuilder.buildURI("localhost", "Scilab" + (new Date()).getTime(),
                Constants.RMI_PROTOCOL_IDENTIFIER, Integer.parseInt(PAProperties.PA_RMI_PORT.getValue()))
                .toString();
        if (debug) {
            System.out.println("[" + host + " SCILAB TASK] Starting the Java Process");
        }
        // We spawn a new JVM with the SCILAB library paths
        process = startProcess(uri);
        // We define the loggers which will write on standard output what comes from the java process
        isLogger = new LoggingThread(process.getInputStream(), "[" + host + "]", false);
        esLogger = new LoggingThread(process.getErrorStream(), "[" + host + "]", true);

        // We start the loggers thread
        Thread t1 = new Thread(isLogger, "OUT Scilab");
        t1.setDaemon(true);
        t1.start();

        Thread t2 = new Thread(esLogger, "ERR Scilab");
        t2.setDaemon(true);
        t2.start();

        if (debug) {
            System.out.println("[" + host + " SCILAB TASK] Executing the task");
        }

        // finally we call the internal version of the execute method
        Object res = executeInternal(uri, results);

        // Then we destroy the process and return the results
        process.destroy();
        process = null;

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

        String input = (String) args.get("input");

        if (input != null) {
            inputScript = input;
        }

        String outputs = (String) args.get("outputs");
        if (outputs != null) {
            out_set = outputs.split("[ ,]+");
        }

        host = java.net.InetAddress.getLocalHost().getHostName();
    }

    /**
     * Deploy an Active Object on the given Node uri
     * @param uri uri of the Node where to deploy the AO
     * @param workerClassName name of the worker class
     * @param params parameters of the constructor
     * @throws Throwable
     */
    protected AOSimpleScilab deploy(String uri, String workerClassName, Object... params) throws Throwable {
        Exception ex = null;
        AOSimpleScilab worker = null;
        if (debug) {
            System.out.println("[" + host + " SCILAB TASK] Deploying the Worker");
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
                System.err.println("[" + host + " SCILAB TASK] Worker couldn't be deployed.");
            }
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
        if (debug) {
            System.out.println("[" + host + " SCILAB TASK] Deploying Worker (SimpleScilab)");
        }
        scilabWorker = deploy(uri, AOSimpleScilab.class.getName(), inputScript, scriptLines, out_set);
        if (debug) {
            System.out.println("[" + host + " SCILAB TASK] Executing (SimpleScilab)");
        }

        // We execute the task on the worker
        Object res = scilabWorker.execute(results);
        // We wait for the result
        res = PAFuture.getFutureValue(res);
        // We make a synchronous call to terminate
        scilabWorker.terminate();

        return res;
    }

    private final Process startProcess(String uri) throws Throwable {
        if (debug) {
            System.out.println("[" + host + " SCILAB TASK] Starting a new JVM");
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

        // We add the Scilab specific env variables
        env.put("SCI", scilabConfig.getScilabHome());
        env.put("SCIDIR", scilabConfig.getScilabHome());

        // javaCommandBuilder.setJavaPath(System.getenv("JAVA_HOME") +
        //     "/bin/java");
        // we set as well the java.library.path property (precaution)
        javaCommandBuilder.setJvmOptions("-Djava.library.path=\"" + libPath + "\"" +
            " -Dproactive.rmi.port=" + Integer.parseInt(PAProperties.PA_RMI_PORT.getValue()) +
            " -Dproactive.http.port=" + Integer.parseInt(PAProperties.PA_XMLHTTP_PORT.getValue()));

        pb.command(javaCommandBuilder.getJavaCommand());

        return pb.start();
    }

    /**
     * Utility function to add SCILAB directories to the given path-like string
     * @param path path-like string
     * @return an augmented path
     */
    private String addScilabToPath(String path) {
        String newPath;

        if (path == null) {
            newPath = "";
        } else {
            newPath = path + os.pathSeparator();
        }

        newPath = newPath + (scilabConfig.getScilabHome() + os.fileSeparator() + "bin");

        return newPath;
    }

    /**
     * An utility class to build the Java command
     * @author The ProActive Team
     *
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
