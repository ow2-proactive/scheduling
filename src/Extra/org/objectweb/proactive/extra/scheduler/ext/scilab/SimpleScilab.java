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
package org.objectweb.proactive.extra.scheduler.ext.scilab;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.extra.scheduler.common.task.JavaExecutable;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extra.scheduler.ext.scilab.exception.ScilabInitException;
import org.objectweb.proactive.extra.scheduler.util.LinuxShellExecuter;
import org.objectweb.proactive.extra.scheduler.util.Shell;


public class SimpleScilab extends JavaExecutable {

    /**
         *
         */
    private static final long serialVersionUID = 6876359361914422628L;
    final private static String[] DEFAULT_OUT_VARIABLE_SET = { "out" };

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

    // Threads which collect the JVM's stdout and stderr      
    private LoggingThread esLogger = null;
    private LoggingThread isLogger = null;

    // tool to build the JavaCommand
    private DummyJVMProcess javaCommandBuilder;

    // the Home Dir of Scilab on this machine
    private String scilabHome = null;

    // the Active Object worker located in the spawned JVM
    private AOSimpleScilab scilabWorker;

    // the OS where this JVM is running
    private OperatingSystem os = OperatingSystem.getOperatingSystem();

    // The process holding the spawned JVM
    private Process process = null;

    /**
     * ProActive No Arg Constructor
     */
    public SimpleScilab() {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.scheduler.common.task.Executable#execute(org.objectweb.proactive.extra.scheduler.common.task.TaskResult[])
     */
    public Object execute(TaskResult... results) throws Throwable {
        for (TaskResult res : results) {
            if (res.hadException()) {
                throw res.getException();
            }
        }

        // First we try to find SCILAB
        findScilab();

        // We create a custom URI as the node name
        uri = URIBuilder.buildURI("localhost", "Scilab" +
                (new Date()).getTime()).toString();
        System.out.println("[" + host +
            " SCILAB TASK] Starting the Java Process");
        // We spawn a new JVM with the SCILAB library paths
        process = startProcess(uri);
        // We define the loggers which will write on standard output what comes from the java process
        isLogger = new LoggingThread(process.getInputStream(),
                "[" + host + " SCILAB TASK: SUBPROCESS OUT]");
        esLogger = new LoggingThread(process.getErrorStream(),
                "[" + host + " SCILAB TASK: SUBPROCESS ERR]");

        // We start the loggers thread
        Thread t1 = new Thread(isLogger);
        t1.start();

        Thread t2 = new Thread(esLogger);
        t2.start();

        System.out.println("[" + host + " SCILAB TASK] Executing the task");

        // finally we call the internal version of the execute method
        Object res = executeInternal(uri, results);

        // When the task is finished, we first tell the threads to stop logging and exit
        synchronized (isLogger.goon) {
            isLogger.goon = false;
        }

        synchronized (esLogger.goon) {
            esLogger.goon = false;
        }

        // Then we destroy the process and return the results
        process.destroy();
        process = null;

        return res;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.scheduler.common.task.JavaExecutable#init(java.util.Map)
     */
    public void init(Map<String, Object> args) throws Exception {
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
            scriptLines = getContentAsList(is);
        }

        String f = (String) args.get("scriptFile");

        if (f != null) {
            FileInputStream fis = new FileInputStream(f);
            scriptLines = getContentAsList(fis);
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
    protected AOSimpleScilab deploy(String uri, String workerClassName,
        Object... params) throws Throwable {
        ProActiveException ex = null;
        AOSimpleScilab worker = null;
        System.out.println("[" + host + " SCILAB TASK] Deploying the Worker");

        // We create an active object on the given node URI, the JVM corresponding to this node URI is starting,
        // so we retry for 30 seconds until the JVM has started and we can create the Active Object
        for (int i = 0; i < 30; i++) {
            try {
                try {
                    worker = (AOSimpleScilab) ProActiveObject.newActive(workerClassName,
                            params, uri);
                } catch (ProActiveException e) {
                    ex = e;
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (worker == null) {
            System.err.println("[" + host +
                " SCILAB TASK] Worker couldn't be deployed.");
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
    protected Object executeInternal(String uri, TaskResult... results)
        throws Throwable {
        System.out.println("[" + host +
            " SCILAB TASK] Deploying Worker (SimpleScilab)");
        scilabWorker = deploy(uri, AOSimpleScilab.class.getName(), inputScript,
                scriptLines, out_set);
        System.out.println("[" + host +
            " SCILAB TASK] Executing (SimpleScilab)");

        // We execute the task on the worker
        Object res = scilabWorker.execute(results);
        // We wait for the result
        res = ProFuture.getFutureValue(res);
        // We make a synchronous call to terminate
        scilabWorker.terminate();

        return res;
    }

    private final Process startProcess(String uri) throws Throwable {
        System.out.println("[" + host + " SCILAB TASK] Starting a new JVM");
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
        env.put("SCI", scilabHome);
        env.put("SCIDIR", scilabHome);

        // javaCommandBuilder.setJavaPath(System.getenv("JAVA_HOME") +
        //     "/bin/java");
        // we set as well the java.library.path property (precaution)
        javaCommandBuilder.setJvmOptions("-Djava.library.path=" + libPath);

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

        newPath = newPath + (scilabHome + os.fileSeparator() + "bin");

        return newPath;
    }

    /**
     * Utility function to find Scilab
     * @throws IOException
     * @throws InterruptedException
     * @throws ScilabInitException
     */
    private final void findScilab()
        throws IOException, InterruptedException, ScilabInitException {
        System.out.println("[" + host +
            " SCILAB TASK] launching script to find Scilab");

        Process p1 = null;

        if (os.equals(OperatingSystem.unix)) {
            // Under linux we launch an instance of the Shell
            // and then pipe to it the script's content
            InputStream is = SimpleScilab.class.getResourceAsStream(
                    "find_scilab_command.sh");
            p1 = LinuxShellExecuter.executeShellScript(is, Shell.Bash);
        } else if (os.equals(OperatingSystem.windows)) {
            // We can't execute the script on Windows the same way,
            // we need to write the content of the batch file locally and then launch the file
            InputStream is = SimpleScilab.class.getResourceAsStream(
                    "find_scilab_command.bat");

            // Code for writing the content of the stream inside a local file
            List<String> inputLines = getContentAsList(is);
            File batchFile = new File("find_scilab_command.bat");

            if (batchFile.exists()) {
                batchFile.delete();
            }

            batchFile.createNewFile();
            batchFile.deleteOnExit();

            if (batchFile.canWrite()) {
                PrintWriter pw = new PrintWriter(new BufferedWriter(
                            new FileWriter(batchFile)));

                for (String line : inputLines) {
                    pw.println(line);
                    pw.flush();
                }

                pw.close();
            } else {
                throw new ScilabInitException("can't write in : " + batchFile);
            }

            // End of this code

            // finally we launch the batch file
            p1 = Runtime.getRuntime().exec("find_scilab_command.bat");
        } else {
            throw new UnsupportedOperationException("[" + host +
                " SCILAB TASK] Finding Scilab on " + os +
                " is not supported yet");
        }

        List<String> lines = getContentAsList(p1.getInputStream());

        for (String ln : lines) {
            System.out.println(ln);
        }

        // The batch file is supposed to write, if it's successful, two lines :
        // 1st line : the full path to the scilab command
        // 2nd line : the name of the os-dependant arch dir
        if (p1.waitFor() == 0) {
            scilabHome = lines.get(0);
            System.out.println("[" + host + " SCILAB TASK] Found Scilab at : " +
                scilabHome);
        } else {
            StringWriter error_message = new StringWriter();
            PrintWriter pw = new PrintWriter(error_message);
            pw.println("Error during find_scilab script execution:");

            for (String l : lines) {
                pw.println(l);
            }

            throw new ScilabInitException(error_message.toString());
        }
    }

    /**
     * Return the content read through the given text input stream as a list of file
     * @param is
     * @return
     */
    private List<String> getContentAsList(InputStream is) {
        ArrayList<String> lines = new ArrayList<String>();
        BufferedReader d = new BufferedReader(new InputStreamReader(
                    new BufferedInputStream(is)));

        String line = null;

        try {
            line = d.readLine();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        while (line != null) {
            lines.add(line);

            try {
                line = d.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                line = null;
            }
        }

        try {
            d.close();
        } catch (IOException e) {
        }

        return lines;
    }

    /**
     * An utility class to build the Java command
     * @author fviale
     *
     */
    private static class DummyJVMProcess extends JVMProcessImpl {

        /**
                 *
                 */
        private static final long serialVersionUID = -434626212534426067L;

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

    /**
     * An utility class (Thread) to collect the output
     * @author fviale
     *
     */
    private static class LoggingThread implements Runnable {
        private String appendMessage;
        private Boolean goon = true;
        private InputStream streamToLog;

        public LoggingThread(InputStream is, String appendMessage) {
            this.streamToLog = is;
            this.appendMessage = appendMessage;
        }

        public void run() {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                        streamToLog));
            String line = null;
            ;

            try {
                line = br.readLine();
            } catch (IOException e) {
            }

            while ((line != null) && goon) {
                System.out.println(appendMessage + line);
                System.out.flush();

                try {
                    line = br.readLine();
                } catch (IOException e) {
                }
            }

            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
