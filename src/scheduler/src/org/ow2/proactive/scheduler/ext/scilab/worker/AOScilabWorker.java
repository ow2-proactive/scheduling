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
package org.ow2.proactive.scheduler.ext.scilab.worker;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.ext.matsci.common.exception.InvalidNumberOfParametersException;
import org.ow2.proactive.scheduler.ext.matsci.common.exception.InvalidParameterException;
import org.ow2.proactive.scheduler.ext.matsci.worker.MatSciWorker;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfigBase;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabGlobalConfig;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig;
import org.ow2.proactive.scheduler.ext.scilab.common.exception.ScilabInitializationException;
import org.ow2.proactive.scheduler.ext.scilab.common.exception.ScilabInitializationHanged;
import org.ow2.proactive.scheduler.ext.scilab.common.exception.ScilabTaskException;
import org.ow2.proactive.scheduler.ext.scilab.worker.util.ScilabEngineConfig;
import org.scilab.modules.javasci.Scilab;
import org.scilab.modules.types.ScilabBoolean;
import org.scilab.modules.types.ScilabType;

import java.io.*;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * An active object which handles the interaction between the ScilabTask and a local Scilab engine
 *
 * @author The ProActive Team
 */
public class AOScilabWorker implements Serializable, MatSciWorker {

    /** The ISO8601 for debug format of the date that precedes the log message */
    private static final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");

    private static String HOSTNAME;

    private static OperatingSystem os;

    private static char fs;

    static {
        try {
            HOSTNAME = java.net.InetAddress.getLocalHost().getHostName();
            os = OperatingSystem.getOperatingSystem();
            fs = os.fileSeparator();
        } catch (UnknownHostException e) {
        }
    }
    static String nl = System.getProperty("line.separator");

    /**
     * script executed to initialize the task (input parameter)
     */
    private String inputScript = null;

    /**
     * Main script to be executed
     */
    private ArrayList<String> mainscriptLines = new ArrayList<String>();

    /**
     * Configuration of Scilab (paths)
     */
    private ScilabEngineConfig engineConfig;

    /**
     * The scilab engine
     */
    private org.scilab.modules.javasci.Scilab engine = null;

    private boolean initialized = false;
    private boolean initErrorOccured = false;
    private Throwable initError = null;

    /**
     * Definition of user-functions
     */
    private String functionName = null;

    /**
     * Name of this ProActive Node
     */
    private String nodeName = null;

    /**
     * System tmp dir
     */
    static File tmpDir = new File(System.getProperty("java.io.tmpdir"));

    /**
     *
     */
    private File tmpDirNode = null;

    /** For debug purpose */
    private PrintWriter outDebugWriter;

    File localSpace;

    File tempSubDir;

    private PASolveScilabGlobalConfig paconfig = null;
    private PASolveScilabTaskConfig taskconfig;

    public AOScilabWorker() {
    }

    /**
     * Constructor for the Simple task
     *
     * @param scilabConfig the configuration for scilab
     */
    public AOScilabWorker(ScilabEngineConfig scilabConfig) throws Exception {
        this.engineConfig = scilabConfig;
    }

    private void initializeEngine() throws Exception {
        if (!initialized) {
            try {
                if (paconfig.isDebug()) {
                    System.out.println("Scilab Initialization...");
                    System.out.println("PATH=" + System.getenv("PATH"));
                    System.out.println("LD_LIBRARY_PATH=" + System.getenv("LD_LIBRARY_PATH"));
                    System.out.println("java.library.path=" + System.getProperty("java.library.path"));
                }
                System.out.println("Starting a new Scilab engine:");
                System.out.println(engineConfig);
                scilabStarter();

                if (paconfig.isDebug()) {
                    System.out.println("Initialization Complete!");
                }
            } catch (UnsatisfiedLinkError e) {
                StringWriter error_message = new StringWriter();
                PrintWriter pw = new PrintWriter(error_message);
                pw.println("Can't find the Scilab libraries in host " + java.net.InetAddress.getLocalHost());
                pw.println("PATH=" + System.getenv("PATH"));
                pw.println("LD_LIBRARY_PATH=" + System.getenv("LD_LIBRARY_PATH"));
                pw.println("java.library.path=" + System.getProperty("java.library.path"));

                ScilabInitializationException ne = new ScilabInitializationException(error_message.toString());
                ne.initCause(e);
                throw ne;
            } catch (NoClassDefFoundError e) {
                StringWriter error_message = new StringWriter();
                PrintWriter pw = new PrintWriter(error_message);
                pw.println("Classpath Error in " + java.net.InetAddress.getLocalHost());
                pw.println("java.class.path=" + System.getProperty("java.class.path"));

                ScilabInitializationException ne = new ScilabInitializationException(error_message.toString());
                ne.initCause(e);
                throw ne;
            } catch (ScilabInitializationException e) {
                throw e;
            } catch (Throwable e) {

                StringWriter error_message = new StringWriter();
                PrintWriter pw = new PrintWriter(error_message);
                pw.println("Error initializing Scilab in " + java.net.InetAddress.getLocalHost());
                pw.println("PATH=" + System.getenv("PATH"));
                pw.println("LD_LIBRARY_PATH=" + System.getenv("LD_LIBRARY_PATH"));
                pw.println("java.library.path=" + System.getProperty("java.library.path"));
                pw.println("java.class.path=" + System.getProperty("java.class.path"));

                IllegalStateException ne = new IllegalStateException(error_message.toString());
                ne.initCause(e);
                throw ne;
            }

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {

                    engine.close();

                }
            }));

            nodeName = PAActiveObject.getNode().getVMInformation().getName().replace('-', '_');
            tmpDirNode = new File(tmpDir, nodeName);
            if (!tmpDirNode.exists() || !tmpDirNode.isDirectory()) {
                tmpDirNode.mkdir();
            }

            initialized = true;
        }

    }

    private void scilabStarter() throws Throwable {

        Runnable runner = new Runnable() {
            public void run() {
                try {

                    engine = new Scilab();
                    if (engine.open()) {

                    } else {
                        throw new IllegalStateException("Scilab engine could not start");
                    }
                    initialized = true;
                } catch (Throwable t) {
                    initError = t;
                    initErrorOccured = true;
                }
            }
        };

        Thread starter = new Thread(runner);
        starter.start();

        int nbwait = 0;
        while (!initialized && !initErrorOccured && nbwait < 200) {
            try {
                Thread.sleep(50);
                nbwait++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (initErrorOccured)
            throw initError;
        if (!initialized)
            throw new ScilabInitializationHanged(
                "Couldn't initialize the Scilab engine, this is due to a known bug in Scilab initialization");
    }

    public void init(String inputScript, String functionName, ArrayList<String> scriptLines,
            PASolveScilabGlobalConfig paconfig, PASolveScilabTaskConfig taskconfig, ScilabEngineConfig conf)
            throws Exception {
        if (!this.engineConfig.equals(conf)) {
            terminate();
        }
        this.engineConfig = conf;
        this.inputScript = inputScript;
        this.mainscriptLines = scriptLines;
        this.paconfig = paconfig;
        this.taskconfig = taskconfig;
        this.functionName = functionName;

        // Create a log file if debug is enabled
        this.createLogFileOnDebug();
    }

    private void testEngineInitOrRestart() throws Exception {
        initializeEngine();
        try {
            ScilabBoolean test = new ScilabBoolean(true);
            put("test", test);
            ScilabType answer = get("test");
            if ((answer == null) || !(answer instanceof ScilabBoolean)) {
                restart();
            }
            if (answer.getHeight() != 1 || answer.getWidth() != 1) {
                restart();
            }
            boolean[][] data = ((ScilabBoolean) answer).getData();
            if (data[0][0] != true) {
                restart();
            }
            exec("clear test");
        } catch (Exception e) {
            restart();
        }
        // ok
    }

    private void restart() throws Exception {

        try {
            terminate();
        } catch (Exception e) {

        }
        initializeEngine();
    }

    private void initDS() throws Exception {
        // Changing dir to local space
        URI ls = paconfig.getLocalSpace();
        if (ls != null) {
            localSpace = new File(ls);
            if (localSpace.exists() && localSpace.canRead() && localSpace.canWrite()) {
                exec("cd('" + localSpace + "');");
                tempSubDir = new File(localSpace, paconfig.getTempSubDirName());

            } else {
                System.err.println("Error, can't write on : " + localSpace);
                printLog("Error, can't write on : " + localSpace);
                throw new IllegalStateException("Error, can't write on : " + localSpace);
            }
        }

    }

    private void transferSource() throws Exception {
        if (paconfig.isTransferSource() && tempSubDir != null) {
            if (paconfig.isDebug()) {
                System.out.println("Loading source files from " + tempSubDir);
            }

            File[] files = tempSubDir.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    if (pathname.getName().matches(".*\\.sci")) {
                        return true;
                    }
                    return false;
                }
            });
            if (files != null && files.length > 0) {
                exec("try;getd('" + tempSubDir + "');catch; end;");
            }
            if (taskconfig.getFunctionVarFiles() != null) {
                for (String fileName : taskconfig.getFunctionVarFiles()) {
                    exec("load('" + this.tempSubDir + fs + fileName + "');");
                }
            }

        }
    }

    private void transferInputVariables() throws Exception {
        if (paconfig.isTransferVariables()) {
            File inMat = new File(taskconfig.getInputVariablesFileURI());
            printLog("Loading Input Variable file " + inMat);
            exec("load('" + inMat + "');");
            if (taskconfig.getComposedInputVariablesFileURI() != null) {
                File compinMat = new File(taskconfig.getComposedInputVariablesFileURI());
                exec("load('" + compinMat + "');in=out;clear out;");
            }
        }
    }

    private ScilabType transferOutputVariable() throws Exception {
        File outputFile = new File(tempSubDir, taskconfig.getOutputVariablesFileName());
        printLog("Saving Output Variable file " + outputFile);
        exec("save('" + outputFile + "',out);");

        if (!outputFile.exists()) {
            throw new ScilabTaskException();
        }
        ScilabType out = new ScilabBoolean(true);
        return out;
    }

    public Serializable execute(int index, TaskResult... results) throws Throwable {

        testEngineInitOrRestart();

        prepare();
        boolean ok = true;
        if (!paconfig.isTransferVariables()) {
            HashMap<String, List<ScilabType>> newEnv = new HashMap<String, List<ScilabType>>();

            if (results != null) {
                if (results.length > 1) {
                    throw new InvalidNumberOfParametersException(results.length);
                }
                if (results.length == 1) {
                    TaskResult res = results[0];
                    if (!(res.value() instanceof ScilabType)) {
                        throw new InvalidParameterException(res.value().getClass());
                    }

                    ScilabType in = (ScilabType) res.value();
                    put("in", in);
                }

            }
        }

        // Initialization, clearing up old variables :
        prepare();

        initDS();

        transferSource();

        transferInputVariables();

        //if (paconfig.isDebug()) {
        //    exec("who");
        //}

        if (inputScript != null) {
            printLog("Executing inputscript");
            ok = executeScript(inputScript, false);
            if (paconfig.isDebug()) {
                System.out.println("End of inputscript execution");
            }
        }

        printLog("Executing mainscript");

        ok = executeScript(prepareScript(mainscriptLines), true);
        printLog("End of mainscript execution " + (ok ? "ok" : "ko"));

        //if (!ok)
        //    throw new ScilabTaskException();

        return getResults();

    }

    /**
     * Terminates the Scilab engine
     *
     * @return true for synchronous call
     */
    public BooleanWrapper terminate() {

        engine.close();
        engine = null;

        initialized = false;
        return new BooleanWrapper(true);
    }

    private void prepare() {
        exec("errclear();clear;");
        if (paconfig.isDebug()) {
            // To be improved
            exec("mode(3);lines(0);funcprot(0);");
        } else {
            exec("lines(0);funcprot(0);");
        }
    }

    public boolean cleanup() {
        if (engine != null) {
            exec("errclear();clear;");
            exec("cd('" + tmpDir + "');");
        }
        return true;
    }

    /**
     * Retrieves the output variables
     *
     * @return list of Scilab data
     */
    protected ScilabType getResults() throws Exception {
        ScilabType out;
        printLog("Receiving outputs");

        if (paconfig.isTransferVariables()) {
            out = transferOutputVariable();
        } else {
            try {
                out = get("out");
            } catch (Exception e) {
                throw new ScilabTaskException();
            }
        }

        return out;

    }

    /**
     * Executes both input and main scripts on the engine
     *
     * @throws Throwable
     */
    protected boolean executeScript(String script, boolean eval) throws Throwable {

        if (eval) {

            if (script.indexOf(31) >= 0) {
                String[] lines = script.split("" + ((char) 31));
                printLog("Executing multi-line: " + script);

                for (String line : lines) {

                    // The special character ASCII 30 means that we want to execute the line using execstr instead of directly
                    // This is used to get clearer error messages from Scilab
                    if (line.startsWith("" + ((char) 30))) {
                        String modifiedLine = "execstr('" + line.substring(1) + "','errcatch','n');";
                        printLog("Executing : " + modifiedLine);

                        exec(modifiedLine);
                        int errorcode = lasterrorcode();
                        if ((errorcode != 0) && (errorcode != 2)) {
                            writeError();
                            return false;
                        }
                    } else {
                        printLog("Executing : " + line);

                        exec(line);
                        int errorcode = lasterrorcode();
                        if ((errorcode != 0) && (errorcode != 2)) {
                            writeError();
                            return false;
                        }
                    }
                }
            } else {
                printLog("Executing single-line: " + script);

                exec(script);
                int errorcode = lasterrorcode();
                if ((errorcode != 0) && (errorcode != 2)) {
                    writeError();
                    return false;
                }
            }

        } else {
            File temp;
            BufferedWriter out;
            printLog("Executing inputscript: " + script);

            temp = new File(tmpDirNode, "inpuscript.sce");
            if (temp.exists()) {
                temp.delete();
            }
            temp.createNewFile();
            temp.deleteOnExit();
            out = new BufferedWriter(new FileWriter(temp));
            out.write(script);
            out.close();
            if (paconfig.isDebug()) {
                exec("exec('" + temp.getAbsolutePath() + "',3);");
            } else {
                exec("exec('" + temp.getAbsolutePath() + "',0);");
            }
            int errorcode = lasterrorcode();
            if ((errorcode != 0) && (errorcode != 2)) {
                exec("disp(lasterror())");
                exec("errclear();");
                return false;
            }
        }
        return true;
    }

    private void put(String name, ScilabType var) throws Exception {

        engine.put(name, var);

    }

    private ScilabType get(String name) throws Exception {

        return engine.get(name);

    }

    private void exec(String code) {

        engine.exec(code);

    }

    private int lasterrorcode() {

        return engine.getLastErrorCode();

    }

    /**
     * Ouput in scilab the error occured
     */
    private void writeError() {

        engine
                .exec("[str2,n2,line2,func2]=lasterror(%t);printf('!-- error %i\n%s\n at line %i of function %s\n',n2,str2,line2,func2)");
        engine.exec("errclear();");

    }

    /**
     * Appends all the script's lines as a single string
     *
     * @return single line script
     */
    private String prepareScript(List<String> scriptLines) {
        String script = "";

        for (String line : scriptLines) {
            script += line;
            script += nl;
        }

        return script;
    }

    private void printLog(final String message) {
        if (!this.paconfig.isDebug()) {
            return;
        }
        final Date d = new Date();
        final String log = "[" + ISO8601FORMAT.format(d) + " " + HOSTNAME + " " + this.getClass() + "] " +
            message;
        System.out.println(log);
        System.out.flush();
        if (this.outDebugWriter != null) {
            this.outDebugWriter.println(log);
            this.outDebugWriter.flush();
        }
    }

    /** Creates a log file in the java.io.tmpdir if debug is enabled */
    private void createLogFileOnDebug() throws Exception {
        if (!this.paconfig.isDebug()) {
            return;
        }
        String nodeName = MatSciEngineConfigBase.getNodeName();

        String tmpPath = System.getProperty("java.io.tmpdir");
        // system temp dir (not using java.io.tmpdir since in runasme it can be
        // inaccesible and the scratchdir property can inherited from parent jvm)
        //        if (this.paconfig.isRunAsMe()) {
        //            tmpPath = System.getProperty("node.dataspace.scratchdir");
        //        }

        // log file writer used for debugging
        File tmpDirFile = new File(tmpPath);
        File nodeTmpDir = new File(tmpDirFile, nodeName);
        if (!nodeTmpDir.exists()) {
            nodeTmpDir.mkdirs();
        }
        File logFile = new File(tmpPath, this.getClass() + "_" + nodeName + ".log");
        if (!logFile.exists()) {
            logFile.createNewFile();
        }

        try {
            FileWriter outFile = new FileWriter(logFile);
            PrintWriter out = new PrintWriter(outFile);

            outDebugWriter = out;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
