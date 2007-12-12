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
package org.objectweb.proactive.extensions.scheduler.ext.scilab;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javasci.SciData;
import javasci.Scilab;

import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.ext.matlab.exception.InvalidParameterException;


public class AOSimpleScilab implements Serializable {

    /**
         *
         */
    static String nl = System.getProperty("line.separator");
    private String inputScript = null;
    private String[] outputVars = null;
    private ArrayList<String> scriptLines = new ArrayList<String>();

    public AOSimpleScilab() {
    }

    /**
     * Constructor for the Simple task
     * @param matlabCommandName the name of the Scilab command
     * @param inputScript  a pre-scilab script that will be launched before the main one (e.g. to set input params)
     * @param scriptLines a list of lines which represent the main script
     */
    public AOSimpleScilab(String inputScript, ArrayList<String> scriptLines,
        String[] outputVars) {
        this.inputScript = inputScript;
        this.scriptLines = scriptLines;
        this.outputVars = outputVars;

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    Scilab.Finish();
                }
            }));
    }

    public Object execute(TaskResult... results) throws Throwable {
        try {
            System.out.println("Scilab Initialization...");
            Scilab.init();
            System.out.println("Initialization Complete!");
        } catch (UnsatisfiedLinkError e) {
            StringWriter error_message = new StringWriter();
            PrintWriter pw = new PrintWriter(error_message);
            pw.println("Can't find the Scilab libraries in host " +
                java.net.InetAddress.getLocalHost());
            pw.println("PATH=" + System.getenv("PATH"));
            pw.println("LD_LIBRARY_PATH=" + System.getenv("LD_LIBRARY_PATH"));
            pw.println("java.library.path=" +
                System.getProperty("java.library.path"));

            UnsatisfiedLinkError ne = new UnsatisfiedLinkError(error_message.toString());
            ne.initCause(e);
            throw ne;
        }

        HashMap<String, List<SciData>> newEnv = new HashMap<String, List<SciData>>();

        for (TaskResult res : results) {
            if (!(res.value() instanceof List)) {
                throw new InvalidParameterException(res.value().getClass());
            }

            for (SciData in : (List<SciData>) res.value()) {
                if (newEnv.containsKey(in.getName())) {
                    List<SciData> ldata = newEnv.get(in.getName());
                    ldata.add(in);
                } else {
                    ArrayList<SciData> ldata = new ArrayList<SciData>();
                    ldata.add(in);
                    newEnv.put(in.getName(), ldata);
                }

                //Scilab.sendData(in);
            }
        }

        for (Map.Entry<String, List<SciData>> entry : newEnv.entrySet()) {
            List<SciData> ldata = entry.getValue();
            int i = 1;
            for (SciData in : ldata) {
                in.setName(in.getName() + i);
                i++;
                Scilab.sendData(in);
            }
        }
        executeScript();

        System.out.println("Receiving outputs");
        ArrayList<SciData> out = new ArrayList<SciData>();
        int i = 0;
        for (String var : outputVars) {
            System.out.println("Receiving output :" + var);
            out.add(Scilab.receiveDataByName(var));
        }

        return out;
    }

    /**
     * Terminates the Scilab engine
     * @return true for synchronous call
     */
    public boolean terminate() {
        Scilab.Finish();

        return true;
    }

    /**
     * Executes both input and main scripts on the engine
     * @throws Throwable
     */
    protected final void executeScript() throws Throwable {
        if (inputScript != null) {
            System.out.println("Feeding input");
            Scilab.Exec(inputScript);
        }

        String execScript = prepareScript();
        System.out.println("Executing Script");
        File temp;
        BufferedWriter out;
        temp = File.createTempFile("scilab", ".sce");
        temp.deleteOnExit();
        out = new BufferedWriter(new FileWriter(temp));
        out.write(execScript);
        out.close();

        Scilab.Exec("exec(''" + temp.getAbsolutePath() + "'');");
        System.out.println("Script Finished");
    }

    /**
     * Appends all the script's lines as a single string
     * @return
     */
    private String prepareScript() {
        String script = "";

        for (String line : scriptLines) {
            script += line;
            script += nl;
        }

        return script;
    }
}
