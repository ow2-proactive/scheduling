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
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.JavaExecutable;
import org.objectweb.proactive.extensions.scheduler.ext.matlab.exception.MatlabInitException;
import org.objectweb.proactive.extensions.scheduler.util.LinuxShellExecuter;
import org.objectweb.proactive.extensions.scheduler.util.Shell;


public class SimpleMatlab extends JavaExecutable {

    /**
     *
     */

    // This hostname, for debugging purpose
    protected String host;

    // the index when the input is the result of a SplitTask
    protected int index = -1;

    // the lines of inputScript
    protected String inputScript = null;

    // the name of the Matlab command on this machine
    protected static String matlabCommandName = null;

    // The lines of the Matlab script
    protected ArrayList<String> scriptLines = null;

    // The URI to which the spawned JVM(Node) is registered
    protected static String uri = null;
    private LoggingThread esLogger = null;

    // Threads which collect the JVM's stdout and stderr  
    private LoggingThread isLogger = null;

    // tool to build the JavaCommand
    private DummyJVMProcess javaCommandBuilder;

    // the Home Dir of Matlab on this machine
    private static String matlabHome = null;

    // the name of the arch dir to find native libraries (can be win32, glnx86, ...)
    private static String matlabLibDirName = null;

    // the Active Object worker located in the spawned JVM
    private static AOSimpleMatlab matlabWorker;

    // the OS where this JVM is running
    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    // The process holding the spanwned JVM
    private static Process process = null;
    private static String matlabVersion;

    // ProActive No Arg Constructor    
    public SimpleMatlab() {
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
            findMatlab();

            // We create a custom URI as the node name
            uri = URIBuilder.buildURI("localhost", "Matlab" + (new Date()).getTime(),
                    Constants.RMI_PROTOCOL_IDENTIFIER, Integer.parseInt(PAProperties.PA_RMI_PORT.getValue()))
                    .toString();
            System.out.println("[" + host + " MATLAB TASK] Starting the Java Process");
            // We spawn a new JVM with the MATLAB library paths

            process = startProcess(uri);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    process.destroy();
                }
            }));

        }
        // We define the loggers which will write on standard output what comes from the java process
        isLogger = new LoggingThread(process.getInputStream(), "[" + host + " MATLAB TASK: SUBPROCESS OUT]");
        esLogger = new LoggingThread(process.getErrorStream(), "[" + host + " MATLAB TASK: SUBPROCESS ERR]");

        // We start the loggers thread
        Thread t1 = new Thread(isLogger);
        t1.start();

        Thread t2 = new Thread(esLogger);
        t2.start();

        System.out.println("[" + host + " MATLAB TASK] Executing the task");

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
        //process.destroy();
        //process = null;

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
            scriptLines = getContentAsList(is);
        }

        Object f = args.get("scriptFile");

        if (f != null) {
            FileInputStream fis = new FileInputStream((String) f);
            scriptLines = getContentAsList(fis);
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
        System.out.println("[" + host + " MATLAB TASK] Deploying the Worker");

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
        System.out.println("[" + host + " MATLAB TASK] Deploying Worker (SimpleMatlab)");
        if (matlabWorker == null) {
            matlabWorker = deploy(uri, AOSimpleMatlab.class.getName(), matlabCommandName);

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    matlabWorker.terminate();
                }
            }));
        }
        System.out.println("[" + host + " MATLAB TASK] Executing (SimpleMatlab)");
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
     * @return
     * @throws Throwable
     */
    private final Process startProcess(String uri) throws Throwable {
        System.out.println("[" + host + " MATLAB TASK] Starting a new JVM");
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

    /**
     * Finds where the ptolemy library is installed for this specific architecture
     * @return a path to ptolemy libraries
     * @throws IOException
     * @throws URISyntaxException
     * @throws MatlabInitException
     */
    private File findPtolemyLibDir() throws IOException, URISyntaxException, MatlabInitException {
        JarURLConnection conn = (JarURLConnection) this.getClass().getResource("/ptolemy/matlab")
                .openConnection();
        URL jarFileURL = conn.getJarFileURL();

        File jarFile = new File(jarFileURL.toURI());
        File libDirFile = jarFile.getParentFile();
        URI ptolemyLibDirURI = libDirFile.toURI().resolve(
                matlabVersion + os.fileSeparator() + matlabLibDirName + os.fileSeparator());
        File answer = new File(ptolemyLibDirURI);

        if (!answer.exists() || !answer.canRead()) {
            throw new MatlabInitException("Can't find ptolemy native library at " + answer +
                ". The native library is generated from scripts in PROACTIVE/scripts/unix/matlab. Refer to README file.");
        } else {
            File libraryFile = new File(ptolemyLibDirURI.resolve(System.mapLibraryName("ptmatlab")));
            if (!libraryFile.exists() || !libraryFile.canRead()) {
                throw new MatlabInitException("Can't find ptolemy native library at " + libraryFile +
                    ". The native library is generated from scripts in PROACTIVE/scripts/unix/matlab. Refer to README file.");
            }
        }
        return answer;
    }

    private String prependPtolemyLibDirToClassPath(String classPath) throws IOException, URISyntaxException,
            MatlabInitException {
        String newcp = classPath;
        File ptolemyLibDir = findPtolemyLibDir();
        newcp = ptolemyLibDir.getAbsolutePath() + os.pathSeparator() + newcp;
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
        int lastIndex = matlabLibDirName.lastIndexOf(os.fileSeparator());
        if (lastIndex != -1) {
            lastDir = matlabLibDirName.substring(lastIndex + 1);
        } else {
            lastDir = matlabLibDirName;
        }

        newPath = newPath + (matlabHome + os.fileSeparator() + "bin");
        newPath = newPath + os.pathSeparator() + (matlabHome + os.fileSeparator() + matlabLibDirName);
        newPath = newPath +
            os.pathSeparator() +
            (matlabHome + os.fileSeparator() + "sys" + os.fileSeparator() + "os" + os.fileSeparator() + lastDir);

        return newPath;
    }

    /**
     * Utility function to find Matlab
     * @throws IOException
     * @throws InterruptedException
     * @throws MatlabInitException
     */
    private final void findMatlab() throws IOException, InterruptedException, MatlabInitException {
        System.out.println("[" + host + " MATLAB TASK] launching script to find Matlab");

        Process p1 = null;

        if (os.equals(OperatingSystem.unix)) {
            // Under linux we launch an instance of the Shell
            // and then pipe to it the script's content
            InputStream is = SimpleMatlab.class.getResourceAsStream("find_matlab_command.sh");
            p1 = LinuxShellExecuter.executeShellScript(is, Shell.Bash);
        } else if (os.equals(OperatingSystem.windows)) {
            // We can't execute the script on Windows the same way,
            // we need to write the content of the batch file locally and then launch the file
            InputStream is = SimpleMatlab.class.getResourceAsStream("find_matlab_command.bat");

            // Code for writing the content of the stream inside a local file
            List<String> inputLines = getContentAsList(is);
            File batchFile = new File("find_matlab_command.bat");

            if (batchFile.exists()) {
                batchFile.delete();
            }

            batchFile.createNewFile();
            batchFile.deleteOnExit();

            if (batchFile.canWrite()) {
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(batchFile)));

                for (String line : inputLines) {
                    pw.println(line);
                    pw.flush();
                }

                pw.close();
            } else {
                throw new MatlabInitException("can't write in : " + batchFile);
            }

            // End of this code

            // finally we launch the batch file
            p1 = Runtime.getRuntime().exec("find_matlab_command.bat");
        } else {
            throw new UnsupportedOperationException("[" + host + " MATLAB TASK] Finding Matlab on " + os +
                " is not supported yet");
        }

        ArrayList<String> lines = getContentAsList(p1.getInputStream());

        for (String ln : lines) {
            System.out.println(ln);
        }

        // The batch file is supposed to write, if it's successful, two lines :
        // 1st line : the full path to the matlab command
        // 2nd line : the name of the os-dependant arch dir
        if (p1.waitFor() == 0) {
            String full_command = lines.get(0);
            System.out.println("[" + host + " MATLAB TASK] Found Matlab at : " + full_command);

            File file = new File(full_command);
            matlabCommandName = file.getName();
            matlabHome = file.getParentFile().getParentFile().getAbsolutePath();
            matlabLibDirName = lines.get(1);
            System.out.println("MATLAB LIB DIR:" + matlabLibDirName);
            matlabVersion = lines.get(2);
            System.out.println("MATLAB VERSION:" + matlabVersion);
        } else {
            StringWriter error_message = new StringWriter();
            PrintWriter pw = new PrintWriter(error_message);
            pw.println("Error during find_matlab script execution:");

            for (String l : lines) {
                pw.println(l);
            }

            throw new MatlabInitException(error_message.toString());
        }
    }

    /**
     * Return the content read through the given text input stream as a list of file
     * @param is input stream to read
     * @return content as list of strings
     */
    private ArrayList<String> getContentAsList(InputStream is) {
        ArrayList<String> lines = new ArrayList<String>();
        BufferedReader d = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));

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
            BufferedReader br = new BufferedReader(new InputStreamReader(streamToLog));
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
