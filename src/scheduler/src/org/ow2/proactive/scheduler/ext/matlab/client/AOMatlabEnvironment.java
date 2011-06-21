/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.ext.matlab.client;

import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabGlobalConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabTaskConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabTaskException;
import org.ow2.proactive.scheduler.ext.matlab.worker.MatlabExecutable;
import org.ow2.proactive.scheduler.ext.matsci.client.*;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;


/**
 * This active object handles the connection between Matlab and the Scheduler directly from the Matlab environment
 *
 * @author The ProActive Team
 */
public class AOMatlabEnvironment extends AOMatSciEnvironment<Boolean, MatlabResultsAndLogs> {

    /**
     * Constructs the environment AO
     */
    public AOMatlabEnvironment() {

    }

    protected MatlabResultsAndLogs waitResultOfTask(String jid, String tname) {
        MatlabResultsAndLogs answer = new MatlabResultsAndLogs();
        MatSciJobVolatileInfo<Boolean> jinfo = currentJobs.get(jid);

        if (jinfo.isDebugCurrentJob()) {

            System.out.println("[AOMatlabEnvironment] Sending the results of task " + tname + " of job " +
                jid + " back...");

        }

        Throwable t = null;
        if (schedulerStopped) {
            answer.setStatus(MatSciTaskStatus.GLOBAL_ERROR);
            answer.setException(new PASolveException("[AOMatlabEnvironment] The scheduler has been stopped"));
        } else if (jinfo.getStatus() == JobStatus.KILLED) {
            // Job killed
            answer.setStatus(MatSciTaskStatus.GLOBAL_ERROR);
            answer.setException(new PASolveException("[AOMatlabEnvironment] The job has been killed"));
        } else if ((t = jinfo.getException(tname)) != null) {
            // Error inside job
            if (t instanceof MatlabTaskException) {
                answer.setStatus(MatSciTaskStatus.MATSCI_ERROR);
                answer.setLogs(jinfo.getLogs(tname));
                //System.err.println(jinfo.getLogs(tid));
            } else {
                answer.setStatus(MatSciTaskStatus.RUNTIME_ERROR);
                answer.setException(new PASolveException(t));
                if (jinfo.getLogs(tname) != null) {
                    answer.setLogs(jinfo.getLogs(tname));
                }
            }
        } else {
            // Normal termination
            answer.setStatus(MatSciTaskStatus.OK);
            //System.out.println(jinfo.getLogs(tid));
            answer.setLogs(jinfo.getLogs(tname));
            answer.setResult(jinfo.getResult(tname));
        }

        jinfo.addServedTask(tname);

        if (jinfo.allServed()) {
            currentJobs.remove(jid);
        }

        return answer;
    }

    public MatlabResultsAndLogs waitResult(String jid, String tname) {
        return waitResultOfTask(jid, tname);
    }

    public ArrayList<MatlabResultsAndLogs> retrieve(MatSciJobPermanentInfo jpinfo) {

        syncRetrieve(jpinfo);
        ArrayList<MatlabResultsAndLogs> answers = new ArrayList<MatlabResultsAndLogs>(jpinfo.getNbres());
        for (String ftname : jpinfo.getFinalTaskNames()) {
            answers.add(((AOMatlabEnvironment) stubOnThis).waitResult(jpinfo.getJobId(), ftname));
        }
        return answers;
    }

    /**
     * Submit a new bunch of tasks to the scheduler, throws a runtime exception if a job is currently running
     *
     * @param config      global job config
     * @param taskConfigs config of individual tasks
     */
    public Pair<MatSciJobPermanentInfo, ArrayList<MatlabResultsAndLogs>> solve(
            PASolveMatlabGlobalConfig config, PASolveMatlabTaskConfig[][] taskConfigs) throws Throwable {

        if (schedulerStopped) {
            throw new RuntimeException("[AOMatlabEnvironment] the Scheduler is stopped");
        }
        // We store the script selecting the nodes to use it later at termination.
        debug = config.isDebug();

        if (config.isDebug()) {
            System.out.println("[AOMatlabEnvironment] Submitting job of " + taskConfigs.length + " tasks...");
        }
        //Thread t = java.lang.Thread.currentThread();
        //t.setContextClassLoader(this.getClass().getClassLoader());
        //System.out.println(this.getClass().getClassLoader());

        // Creating a task flow job
        TaskFlowJob job = new TaskFlowJob();
        job.setName("Matlab Environment Job " + lastGenJobId++);
        job.setPriority(JobPriority.findPriority(config.getPriority()));
        job.setCancelJobOnError(false);
        job.setDescription("Set of parallel matlab tasks");

        //if (config.isTransferSource() || config.isTransferEnv() || config.isTransferVariables()) {
        job.setInputSpace(config.getInputSpaceURL());

        job.setOutputSpace(config.getOutputSpaceURL());
        //}
        TreeSet<String> tnames = new TreeSet<String>(new TaskNameComparator());
        TreeSet<String> finaltnames = new TreeSet<String>(new TaskNameComparator());
        int nbResults = taskConfigs.length;
        int depth = taskConfigs[0].length;
        for (int i = 0; i < nbResults; i++) {
            JavaTask oldTask = null;
            for (int j = 0; j < depth; j++) {

                JavaTask schedulerTask = new JavaTask();

                if (config.isFork()) {
                    schedulerTask.setForkEnvironment(new ForkEnvironment());
                }

                // Being fixed in the scheduler trunk
                if (config.isRunAsMe()) {
                    schedulerTask.setRunAsMe(true);

                    // TODO: Fix for Windows since RunAsMe on windows cannot access java.io.tmpdir
                    ForkEnvironment f = new ForkEnvironment();
                    //f.addJVMArgument("-Dnode.dataspace.scratchdir=c:\\Temp\\tata");
                    schedulerTask.setForkEnvironment(f);
                }

                String tname = "" + i + "_" + j;

                tnames.add(tname);

                if (j == depth - 1) {
                    finaltnames.add(tname);
                }

                schedulerTask.setName(tname);
                if (j == depth - 1) {
                    schedulerTask.setPreciousResult(true);
                }
                schedulerTask.addArgument("input", taskConfigs[i][j].getInputScript());
                schedulerTask.addArgument("script", taskConfigs[i][j].getMainScript());
                if (oldTask != null) {
                    schedulerTask.addDependence(oldTask);
                }
                oldTask = schedulerTask;

                if (config.isZipSourceFiles() && taskConfigs[i][j].getSourceZipFileName() != null) {
                    schedulerTask.addInputFiles(config.getTempSubDirName() + "/" +
                        taskConfigs[i][j].getSourceZipFileName(), InputAccessMode.TransferFromInputSpace);
                }
                if (!config.isZipSourceFiles() && taskConfigs[i][j].getSourceNames() != null) {
                    for (String name : taskConfigs[i][j].getSourceNames()) {
                        schedulerTask.addInputFiles(config.getTempSubDirName() + "/" + name,
                                InputAccessMode.TransferFromInputSpace);
                    }
                }
                if (config.isZipSourceFiles() && config.getSourceZipFileName() != null) {
                    schedulerTask.addInputFiles(config.getTempSubDirName() + "/" +
                        config.getSourceZipFileName(), InputAccessMode.TransferFromInputSpace);
                }

                if (config.isTransferEnv()) {

                    schedulerTask.addInputFiles(
                            config.getTempSubDirName() + "/" + config.getEnvMatFileName(),
                            InputAccessMode.TransferFromInputSpace);

                }

                schedulerTask.addInputFiles(config.getTempSubDirName() + "/" +
                    taskConfigs[i][j].getInputVariablesFileName(), InputAccessMode.TransferFromInputSpace);
                if (taskConfigs[i][j].getComposedInputVariablesFileName() != null) {
                    schedulerTask.addInputFiles(config.getTempSubDirName() + "/" +
                        taskConfigs[i][j].getComposedInputVariablesFileName(),
                            InputAccessMode.TransferFromInputSpace);
                }
                schedulerTask.addOutputFiles(config.getTempSubDirName() + "/" +
                    taskConfigs[i][j].getOutputVariablesFileName(), OutputAccessMode.TransferToOutputSpace);

                String[] inputFiles = taskConfigs[i][j].getInputFiles();
                if (inputFiles != null && inputFiles.length > 0) {
                    for (String inputFile : inputFiles) {
                        schedulerTask.addInputFiles(inputFile, InputAccessMode.TransferFromInputSpace);
                    }
                }

                String[] outputFiles = taskConfigs[i][j].getOutputFiles();
                if (outputFiles != null && outputFiles.length > 0) {

                    for (String outputFile : outputFiles) {
                        schedulerTask.addOutputFiles(outputFile, OutputAccessMode.TransferToOutputSpace);
                    }
                }

                if (taskConfigs[i][j].getDescription() != null) {
                    schedulerTask.setDescription(taskConfigs[i][j].getDescription());
                } else {
                    schedulerTask.setDescription(taskConfigs[i][j].getMainScript());
                }

                schedulerTask.setExecutableClassName(MatlabExecutable.class.getName());

                if (taskConfigs[i][j].getCustomScriptUrl() != null) {
                    URL url = new URL(taskConfigs[i][j].getCustomScriptUrl());
                    if (checkScript(url)) {
                        SelectionScript sscript = null;
                        try {

                            sscript = new SelectionScript(url, new String[0]);

                        } catch (InvalidScriptException e1) {
                            throw new RuntimeException(e1);
                        }
                        schedulerTask.addSelectionScript(sscript);
                    }
                }

                if (config.getCustomScriptUrl() != null) {
                    URL url = new URL(config.getCustomScriptUrl());
                    if (checkScript(url)) {
                        SelectionScript sscript = null;
                        try {

                            sscript = new SelectionScript(url, new String[0]);

                        } catch (InvalidScriptException e1) {
                            throw new RuntimeException(e1);
                        }
                        schedulerTask.addSelectionScript(sscript);
                    }
                }

                URL url1 = new URL(config.getCheckMatSciUrl());
                if (checkScript(url1)) {
                    SelectionScript sscript = null;
                    try {
                        //System.out.println(config.getVersionPref());
                        sscript = new SelectionScript(url1, new String[] { "versionPref",
                                config.getVersionPref(), "versionRej", config.getVersionRejAsString(),
                                "versionMin", config.getVersionMin(), "versionMax", config.getVersionMax() });
                    } catch (InvalidScriptException e1) {
                        throw new RuntimeException(e1);
                    }
                    schedulerTask.addSelectionScript(sscript);
                }

                URL url3 = new URL(config.getCheckLicenceScriptUrl());
                if (checkScript(url3)) {
                    SelectionScript sscript = null;
                    String[] scriptParams;
                    ArrayList<String> paramsList = new ArrayList<String>();
                    paramsList.add(config.getLogin());
                    if (taskConfigs[i][j].getCheckLicenceScriptParams() != null) {
                        paramsList.addAll(Arrays.asList(taskConfigs[i][j].getCheckLicenceScriptParams()));
                    } else {
                        paramsList.addAll(Arrays.asList(config.getScriptParams()));
                    }
                    scriptParams = paramsList.toArray(new String[paramsList.size()]);
                    try {
                        sscript = new SelectionScript(url3, scriptParams);
                    } catch (InvalidScriptException e1) {
                        throw new RuntimeException(e1);
                    }

                    schedulerTask.addSelectionScript(sscript);
                }

                schedulerTask.addArgument("global_config", config);
                schedulerTask.addArgument("task_config", taskConfigs[i][j]);

                try {
                    job.addTask(schedulerTask);
                } catch (UserException e) {
                    e.printStackTrace();
                }
            }

        }
        MatSciJobPermanentInfo jpinfo = null;
        String jid = null;
        try {
            jid = scheduler.submit(job).value();
            if (config.isDebug()) {
                System.out.println("[AOMatlabEnvironment] Job " + jid + " submitted.");
            }
            jpinfo = new MatSciJobPermanentInfo(jid, nbResults, depth, config, tnames, finaltnames);
            currentJobs.put(jid, new MatSciJobVolatileInfo(jpinfo));

        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }

        ArrayList<MatlabResultsAndLogs> answers = new ArrayList<MatlabResultsAndLogs>(nbResults);
        for (String ftname : finaltnames) {
            answers.add(((AOMatlabEnvironment) stubOnThis).waitResult(jid, ftname));
        }

        // The last call puts a method in the RequestQueue 
        // that won't be executed until all the results are received (see runactivity)
        // return stubOnThis.waitAllResults();
        return new Pair<MatSciJobPermanentInfo, ArrayList<MatlabResultsAndLogs>>(
            (MatSciJobPermanentInfo) jpinfo.clone(), answers);
    }

}
