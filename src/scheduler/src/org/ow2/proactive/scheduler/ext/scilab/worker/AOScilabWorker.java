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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * An active object which handles the interaction between the ScilabTask and a local Scilab engine
 *
 * @author The ProActive Team
 */
public class AOScilabWorker implements Serializable, MatSciWorker {

    static String nl = System.getProperty("line.separator");

    /**
     * script executed to initialize the task (input parameter)
     */
    private String inputScript = null;

    /**
     * Output variables
     */
    private String[] outputVars = null;

    /**
     * Main script to be executed
     */
    private ArrayList<String> mainscriptLines = new ArrayList<String>();

    /**
     * Configuration of Scilab (paths)
     */
    private ScilabEngineConfig engineConfig;

    private boolean javasciv2 = false;

    private org.scilab.modules.javasci.Scilab engine = null;

    private boolean initialized = false;
    private boolean initErrorOccured = false;
    private Throwable initError = null;

    /**
     * Definition of user-functions
     */
    private String functionsDefinition = null;
    private String functionName = null;

    private String nodeName = null;
    static File tmpDir = new File(System.getProperty("java.io.tmpdir"));

    private File tmpDirNode = null;

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
                    if (javasciv2) {
                        engine.close();
                    }
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
                    if (javasciv2) {
                        engine = new Scilab();
                        if (engine.open()) {

                        } else {
                            throw new IllegalStateException("Scilab engine could not start");
                        }
                    } else {
                        throw new UnsupportedOperationException("javasci v1 not supported");
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

    public void init(String inputScript, String functionName, String functionsDefinition,
            ArrayList<String> scriptLines, String[] outputVars, PASolveScilabGlobalConfig paconfig,
            PASolveScilabTaskConfig taskconfig, ScilabEngineConfig conf) {
        if (!this.engineConfig.equals(conf)) {
            terminate();
        }
        this.engineConfig = conf;
        this.javasciv2 = !MatSciEngineConfigBase.infStrictVersion(engineConfig.getVersion(), "5.3.0");
        this.inputScript = inputScript;
        this.mainscriptLines = scriptLines;
        this.outputVars = outputVars;
        this.paconfig = paconfig;
        this.taskconfig = taskconfig;
        this.functionsDefinition = functionsDefinition;
        this.functionName = functionName;
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
                throw new IllegalStateException("Error, can't write on : " + localSpace);
            }
        }

    }

    private void transferSource() throws Exception {
        if (paconfig.isTransferSource() && tempSubDir != null) {
            if (paconfig.isDebug()) {
                System.out.println("Loading source files");
            }
            if (paconfig.isZipSourceFiles()) {
                // TODO not implemented
            } else {

                String[] sourceNames = taskconfig.getSourceNames();
                URI[] sourceUris = taskconfig.getSourcesFilesURIs();
                if (sourceUris != null) {
                    for (URI sourceuri : sourceUris) {
                        File sourceFile = new File(sourceuri);
                        if (paconfig.isDebug()) {
                            System.out.println("Loading " + sourceFile);
                        }
                        exec("exec('" + sourceFile + "');");
                    }
                }
            }
        }
    }

    private void transferInputVariables() throws Exception {
        if (paconfig.isTransferVariables()) {
            File inMat = new File(taskconfig.getInputVariablesFileURI());
            if (paconfig.isDebug()) {
                System.out.println("Loading Input Variable file " + inMat);
            }
            exec("load('" + inMat + "');");
            if (taskconfig.getComposedInputVariablesFileURI() != null) {
                File compinMat = new File(taskconfig.getComposedInputVariablesFileURI());
                exec("load('" + compinMat + "');in=out;clear out;");
            }
        }
    }

    private ScilabType transferOutputVariable() throws Exception {
        File outputFile = new File(tempSubDir, taskconfig.getOutputVariablesFileName());
        if (paconfig.isDebug()) {
            System.out.println("Saving Output Variable file " + outputFile);
        }
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

        if (functionsDefinition != null) {
            ok = executeFunctionDefinition();
            if (!ok)
                throw new IllegalStateException("Error in function definitions");
        }

        if (inputScript != null) {
            if (paconfig.isDebug()) {
                System.out.println("[AOScilabWorker] Executing inputscript");
            }
            ok = executeScript(inputScript, false);
            if (paconfig.isDebug()) {
                System.out.println("[AOScilabWorker] End of inputscript execution");
            }
        }

        if (paconfig.isDebug()) {
            System.out.println("[AOScilabWorker] Executing mainscript");
        }
        ok = executeScript(prepareScript(mainscriptLines), true);
        if (paconfig.isDebug()) {
            System.out.println("[AOScilabWorker] End of mainscript execution " + (ok ? "ok" : "ko"));
        }

        //if (!ok)
        //    throw new ScilabTaskException();

        return getResults();

    }

    /**
     * Terminates the Scilab engine
     *
     * @return true for synchronous call
     */
    public boolean terminate() {
        if (javasciv2) {
            engine.close();
            engine = null;
        } else {
            throw new UnsupportedOperationException("javasci v1 not supported");
        }
        initialized = false;
        return true;
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
     * Loads in Scilab the user-functions definitions
     *
     * @return success
     * @throws IOException
     */
    protected boolean executeFunctionDefinition() throws IOException {

        File functionFile = new File(tmpDirNode, functionName + ".sci");
        if (functionFile.exists()) {
            functionFile.delete();
        }
        functionFile.createNewFile();
        functionFile.deleteOnExit();

        BufferedWriter out = new BufferedWriter(new FileWriter(functionFile));
        out.write(functionsDefinition.replaceAll("" + ((char) 31), System.getProperty("line.separator")));
        out.close();
        if (paconfig.isDebug()) {
            System.out.println("[AOScilabWorker] Executing function definition : " +
                functionFile.getAbsolutePath());
            exec("exec('" + functionFile.getAbsolutePath() + "')");

        } else {
            exec("exec('" + functionFile.getAbsolutePath() + "');");
        }
        int errorcode = lasterrorcode();

        if ((errorcode != 0) && (errorcode != 2)) {
            writeError();
            return false;
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
        if (paconfig.isDebug()) {
            System.out.println("[AOScilabWorker] Receiving outputs");
        }
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
                if (paconfig.isDebug()) {
                    System.out.println("[AOScilabWorker] Executing multi-line: " + script);
                }
                for (String line : lines) {

                    // The special character ASCII 30 means that we want to execute the line using execstr instead of directly
                    // This is used to get clearer error messages from Scilab
                    if (line.startsWith("" + ((char) 30))) {
                        String modifiedLine = "execstr('" + line.substring(1) + "','errcatch','n');";
                        if (paconfig.isDebug()) {
                            System.out.println("[AOScilabWorker] Executing : " + modifiedLine);
                        }
                        exec(modifiedLine);
                        int errorcode = lasterrorcode();
                        if ((errorcode != 0) && (errorcode != 2)) {
                            writeError();
                            return false;
                        }
                    } else {
                        if (paconfig.isDebug()) {
                            System.out.println("[AOScilabWorker] Executing : " + line);
                        }
                        exec(line);
                        int errorcode = lasterrorcode();
                        if ((errorcode != 0) && (errorcode != 2)) {
                            writeError();
                            return false;
                        }
                    }
                }
            } else {
                if (paconfig.isDebug()) {
                    System.out.println("[AOScilabWorker] Executing single-line: " + script);
                }
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
            if (paconfig.isDebug()) {
                System.out.println("[AOScilabWorker] Executing inputscript: " + script);
            }
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
        if (javasciv2) {
            engine.put(name, var);
        } else {
            throw new UnsupportedOperationException("javasci v1 not supported");
        }
    }

    private ScilabType get(String name) throws Exception {
        if (javasciv2) {
            return engine.get(name);
        } else {
            throw new UnsupportedOperationException("javasci v1 not supported");
        }
    }

    private void exec(String code) {
        if (javasciv2) {

            engine.exec(code);
        } else {
            throw new UnsupportedOperationException("javasci v1 not supported");
        }
    }

    private int lasterrorcode() {
        if (javasciv2) {
            return engine.getLastErrorCode();
        } else {
            throw new UnsupportedOperationException("javasci v1 not supported");
        }
    }

    /**
     * Ouput in scilab the error occured
     */
    private void writeError() {
        if (javasciv2) {
            engine
                    .exec("[str2,n2,line2,func2]=lasterror(%t);printf('!-- error %i\n%s\n at line %i of function %s\n',n2,str2,line2,func2)");
            engine.exec("errclear();");
        } else {
            throw new UnsupportedOperationException("javasci v1 not supported");
        }
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
}
