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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.ext.scilab.client;

import javasci.SciData;
import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.ext.matsci.client.AOMatSciEnvironment;
import org.ow2.proactive.scheduler.ext.matsci.client.MatSciJobPermanentInfo;
import org.ow2.proactive.scheduler.ext.matsci.client.MatSciJobVolatileInfo;
import org.ow2.proactive.scheduler.ext.matsci.client.PASolveException;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabGlobalConfig;
import org.ow2.proactive.scheduler.ext.scilab.worker.ScilabTask;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeSet;


/**
 * This active object handles the interaction between Scilab and the Scheduler it creates Scheduler jobs and receives results
 *
 * @author The ProActive Team
 */
public class AOScilabEnvironment extends AOMatSciEnvironment<SciData, ScilabResultsAndLogs> {

    /**
     * log4j logger
     */
    protected static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.SCILAB);

    public AOScilabEnvironment() {
        super();
    }

    /**
     * Returns all the results in an array or throw a RuntimeException in case of error
     *
     * @return array of SciData tokens
     */
    //    public ArrayList<ScilabResultsAndLogs> waitResultOfJob(String jid) {
    //        MatSciJobVolatileInfo<SciData> jinfo = currentJobs.get(jid);
    //        ArrayList<ScilabResultsAndLogs> answer = new ArrayList<ScilabResultsAndLogs>();
    //        if (jinfo.isDebugCurrentJob()) {
    //            logger.info("[AOScilabEnvironment] Receiving result of job " + jid + " back...");
    //        }
    //
    //        if (schedulerStopped) {
    //            System.err.println("[AOScilabEnvironment] The scheduler has been stopped");
    //           // jinfo.setErrorToThrow(new IllegalStateException("The scheduler has been stopped"));
    //        } else if (jinfo.getStatus() == JobStatus.KILLED || jinfo.getStatus() == JobStatus.CANCELED) {
    //            // Job killed
    //            System.err.println("[AOScilabEnvironment] The job has been killed");
    //            //jinfo.setErrorToThrow(new IllegalStateException("The job has been killed"));
    //        }
    //
    //        try {
    //           // if (jinfo.getErrorToThrow() != null) {
    //
    //                // Error inside job
    //                if (debug) {
    //                    System.err.println("[AOScilabEnvironment] Scheduler Exception");
    //                }
    //                answer.add(new ScilabResultsAndLogs(null, null, jinfo.getErrorToThrow(),
    //                    MatSciTaskStatus.GLOBAL_ERROR));
    //
    //            } else {
    //                // Normal termination
    //                for (int i = 0; i < jinfo.getResults().size(); i++) {
    //                    // answer.add(new ScilabResultsAndLogs(jinfo.getResults().get(i), jinfo.getLogs(i), null), MatSciTaskStatus.OK);
    //                }
    //            }
    //        } finally {
    //            currentJobs.remove(jid);
    //            // updating persistance file
    //            writeCjobsToLog(currentJobs);
    //        }
    //        return answer;
    //    }
    protected ScilabResultsAndLogs waitResultOfTask(String jid, String tname) {
        ScilabResultsAndLogs answer = null;
        MatSciJobVolatileInfo<SciData> jinfo = currentJobs.get(jid);
        if (jinfo.isDebugCurrentJob()) {
            System.out.println("[AOScilabEnvironment] Sending the results of task " + tname + " of job " +
                jid + " back...");
        }

        Throwable t = null;
        if (schedulerStopped) {
            throw new PASolveException("[AOScilabEnvironment] The scheduler has been stopped");
        } else if (jinfo.getStatus() == JobStatus.KILLED || jinfo.getStatus() == JobStatus.CANCELED) {
            // Job killed
            throw new PASolveException("[AOScilabEnvironment] The job has been killed");
        } else if ((t = jinfo.getException(tname)) != null) {
            // Error inside job
            //answer = new ScilabResultsAndLogs(null, null, t, false, true);
        } else {
            // Normal termination
            //answer = jinfo.getResult(tid);
        }

        return answer;
    }

    /**
     * Submit a new bunch of tasks to the scheduler, throws a runtime exception if a job is currently running
     *
     * @param inputScripts input scripts (scripts executed before the main one)
     * @param mainScripts  main scripts
     */
    public ArrayList<ScilabResultsAndLogs> solve(String[] inputScripts, String functionName,
            String functionsDefinition, String mainScripts, String[] inputFiles,
            PASolveScilabGlobalConfig config) throws Throwable {
        if (schedulerStopped) {
            System.err.println("[AOScilabEnvironment] The Scheduler is stopped");
            return new ArrayList<ScilabResultsAndLogs>();
        }

        debug = config.isDebug();

        // We store the script selecting the nodes to use it later at termination.

        if (config.isDebug()) {
            System.out
                    .println("[AOScilabEnvironment] Submitting job of " + inputScripts.length + " tasks...");
        }

        // Creating a task flow job
        TaskFlowJob job = new TaskFlowJob();
        job.setName("Scilab Environment Job " + lastGenJobId++);
        job.setPriority(JobPriority.findPriority(config.getPriority()));
        job.setCancelJobOnError(false);
        job.setDescription("Set of parallel scilab tasks");

        if (config.isTransferSource() || config.isTransferEnv()) {
            job.setInputSpace(config.getInputSpaceURL());

            job.setOutputSpace(config.getOutputSpaceURL());
        }

        // the external log files as the output is forwarded into Scilab directly,
        // in debug mode you might want to read these files though
        if (config.isDebug()) {
            File logFile = new File(System.getProperty("java.io.tmpdir"), "Scilab_job_" + lastGenJobId +
                ".log");
            System.out.println("[AOScilabEnvironment] Log file created at :" + logFile);
            job.setLogFile(logFile.getPath());
        }
        if (config.isDebug()) {
            System.out.println("[AOScilabEnvironment] function name:");
            System.out.println(functionName);
            System.out.println("[AOScilabEnvironment] function definition:");
            System.out.println(functionsDefinition);
            System.out.println("[AOScilabEnvironment] main script:");
            System.out.println(mainScripts);
            System.out.println("[AOScilabEnvironment] input scripts:");
        }

        int nbResults = inputScripts.length;
        int depth = 1;

        TreeSet<String> tnames = new TreeSet<String>(new TaskNameComparator());
        TreeSet<String> finaltnames = new TreeSet<String>(new TaskNameComparator());

        for (int i = 0; i < nbResults; i++) {
            PASolveScilabGlobalConfig clonedConfig = null;
            JavaTask schedulerTask = new JavaTask();

            String tname = "" + i;

            schedulerTask.setName(tname);

            tnames.add(tname);
            finaltnames.add(tname);

            schedulerTask.setPreciousResult(true);
            schedulerTask.addArgument("input", inputScripts[i]);

            if (config.isDebug()) {
                System.out.println(inputScripts[i]);
            }
            schedulerTask.addArgument("functionName", functionName);
            schedulerTask.addArgument("functionsDefinition", functionsDefinition);
            schedulerTask.addArgument("script", mainScripts);

            schedulerTask.addArgument("outputs", "out");

            schedulerTask.addArgument("config", config);

            if (config.isTransferSource()) {
                schedulerTask.addInputFiles(config.getSourceZipFileName(),
                        InputAccessMode.TransferFromInputSpace);
            }
            if (config.isTransferEnv()) {
                schedulerTask.addInputFiles(config.getEnvZipFileName(),
                        InputAccessMode.TransferFromInputSpace);
            }
            if (inputFiles != null) {
                for (String inputFile : inputFiles) {
                    schedulerTask.addInputFiles(inputFile, InputAccessMode.TransferFromInputSpace);
                }
            }

            schedulerTask.setExecutableClassName(ScilabTask.class.getName());

            URL url = new URL(config.getCheckMatSciUrl());
            if (checkScript(url)) {
                SelectionScript sscript = null;
                try {
                    sscript = new SelectionScript(url, new String[] { "versionPref", config.getVersionPref(),
                            "versionRej", config.getVersionRejAsString(), "versionMin",
                            config.getVersionMin(), "versionMax", config.getVersionMax() }, true);
                } catch (InvalidScriptException e1) {
                    throw new RuntimeException(e1);
                }
                schedulerTask.addSelectionScript(sscript);
            }

            try {
                job.addTask(schedulerTask);
            } catch (UserException e) {
                e.printStackTrace();
            }

        }
        MatSciJobPermanentInfo jpinfo = null;
        String jid = null;
        try {
            jid = scheduler.submit(job).value();
            if (config.isDebug()) {
                System.out.println("[AOScilabEnvironment] Job " + jid + " submitted.");
            }
            jpinfo = new MatSciJobPermanentInfo(jid, nbResults, depth, config, tnames, finaltnames);
            currentJobs.put(jid, new MatSciJobVolatileInfo(jpinfo));

        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }

        ArrayList<ScilabResultsAndLogs> answers = new ArrayList<ScilabResultsAndLogs>(nbResults);
        for (String ftname : finaltnames) {
            answers.add(((AOScilabEnvironment) stubOnThis).waitResult(jid, ftname));
        }
        return answers;

        // The last call puts a method in the RequestQueue
        // that won't be executed until all the results are received (see runactivity)
        //return stubOnThis.waitAllResults();
    }

    /**
     * Returns all the results in an array or throw a RuntimeException in case of error
     *
     * @return array of ptolemy tokens
     */
    public ArrayList<ScilabResultsAndLogs> waitAllResults() {
        return null;
        // return waitResultOfJob(waitAllResultsJobID);
    }

    public ScilabResultsAndLogs waitResult(String jid, String tname) {
        return waitResultOfTask(jid, tname);
    }

    public static void main(String[] args) throws ActiveObjectCreationException, NodeException,
            LoginException, SchedulerException {

        AOScilabEnvironment aose = (AOScilabEnvironment) PAActiveObject.newActive(AOScilabEnvironment.class
                .getName(), new Object[] {});
        aose.join("//localhost");
        aose.login("demo", "demo");
        //ArrayList<ResultsAndLogs> ret = aose.solve(new String[] { "in=2" }, "out=in*in;", null, null,
        //        JobPriority.NORMAL, true);
        //System.out.println(ret);
    }

}
