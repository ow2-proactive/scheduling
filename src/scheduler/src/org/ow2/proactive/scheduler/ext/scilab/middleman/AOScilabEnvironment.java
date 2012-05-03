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
package org.ow2.proactive.scheduler.ext.scilab.middleman;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.ParallelEnvironment;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.ext.matsci.client.common.data.*;
import org.ow2.proactive.scheduler.ext.matsci.client.common.exception.PASchedulerException;
import org.ow2.proactive.scheduler.ext.matsci.client.common.exception.PASolveException;
import org.ow2.proactive.scheduler.ext.matsci.common.data.PASolveMatSciGlobalConfig;
import org.ow2.proactive.scheduler.ext.matsci.common.data.PASolveMatSciTaskConfig;
import org.ow2.proactive.scheduler.ext.matsci.middleman.AOMatSciEnvironment;
import org.ow2.proactive.scheduler.ext.scilab.client.common.data.ScilabResultsAndLogs;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabGlobalConfig;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig;
import org.ow2.proactive.scheduler.ext.scilab.common.exception.ScilabTaskException;
import org.ow2.proactive.scheduler.ext.scilab.worker.ScilabExecutable;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.topology.descriptor.ThresholdProximityDescriptor;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.scilab.modules.types.ScilabType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.TreeSet;


/**
 * AOScilabEnvironment This active object handles the interaction between Scilab and the Scheduler it creates Scheduler jobs and receives results
 *
 * @author The ProActive Team
 */
public class AOScilabEnvironment extends AOMatSciEnvironment<ScilabType, ScilabResultsAndLogs> {

    /**
     * log4j logger
     */
    protected static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.SCILAB);

    public AOScilabEnvironment() {
        super();
    }

    /**
     * gets the result and logs of a given task
     * @param jid id of the job
     * @param tname name of the task
     * @return the result object associated with this task
     */
    protected ScilabResultsAndLogs getResultOfTask(String jid, String tname) {
        ScilabResultsAndLogs answer = new ScilabResultsAndLogs();
        answer.setJobId(jid);
        answer.setTaskName(tname);
        MatSciJobVolatileInfo<ScilabType> jinfo = currentJobs.get(jid);

        if (debug) {

            printLog("Sending the results of task " + tname + " of job " + jid + " back...");

        }

        Throwable t = null;
        if (schedulerStopped || schedulerKilled) {
            answer.setStatus(MatSciTaskStatus.GLOBAL_ERROR);
            answer.setException(new PASolveException("The scheduler has been stopped"));
        } else if (jinfo.getStatus() == JobStatus.KILLED) {
            // Job killed
            answer.setStatus(MatSciTaskStatus.GLOBAL_ERROR);
            answer.setException(new PASolveException("The job has been killed"));
        } else if ((t = jinfo.getException(tname)) != null) {
            // Error inside job
            if (t instanceof ScilabTaskException) {
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
            finishedJobs.put(jid, jinfo);
        }

        return answer;
    }

    /** {@inheritDoc} */
    public MatSciJobPermanentInfo solve(PASolveMatSciGlobalConfig gconf, PASolveMatSciTaskConfig[][] tconf)
            throws PASchedulerException, MalformedURLException {

        PASolveScilabGlobalConfig config = (PASolveScilabGlobalConfig) gconf;

        PASolveScilabTaskConfig[][] taskConfigs = (PASolveScilabTaskConfig[][]) tconf;

        if (schedulerStopped || schedulerKilled) {
            throw new PASchedulerException("the Scheduler is stopped");
        }
        ensureConnection();

        // We store the script selecting the nodes to use it later at termination.
        debug = config.isDebug();

        if (debug) {
            printLog("Submitting job of " + taskConfigs.length + " tasks...");
        }
        //Thread t = java.lang.Thread.currentThread();
        //t.setContextClassLoader(this.getClass().getClassLoader());
        //System.out.println(this.getClass().getClassLoader());

        // Creating a task flow job
        TaskFlowJob job = new TaskFlowJob();
        job.setName(gconf.getJobName() + " " + lastGenJobId++);
        job.setPriority(JobPriority.findPriority(config.getPriority()));
        job.setCancelJobOnError(false);
        job.setDescription(gconf.getJobDescription());

        job.setInputSpace(config.getInputSpaceURL());
        job.setOutputSpace(config.getOutputSpaceURL());

        TreeSet<String> tnames = new TreeSet<String>(new TaskNameComparator());
        TreeSet<String> finaltnames = new TreeSet<String>(new TaskNameComparator());
        int nbResults = taskConfigs.length;
        int depth = taskConfigs[0].length;

        String subDir = "";
        String[] subDirNames = config.getTempSubDirNames();
        for (int k = 0; k < subDirNames.length; k++) {
            subDir += subDirNames[k] + "/";
        }

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

                    // Fix for SCHEDULING-1308: With RunAsMe on windows the forked jvm can have a non-writable java.io.tmpdir
                    // With the following js script the forked jvm will inherit the scratchdir property or if undefined the java.io.tmpdir of the node jvm
                    final StringBuilder sb = new StringBuilder(368);
                    sb.append("importClass(java.lang.System);");
                    sb.append("importClass(org.ow2.proactive.scheduler.task.launcher.TaskLauncher);");
                    sb.append("var scratchDir=System.getProperty(TaskLauncher.NODE_DATASPACE_SCRATCHDIR);");
                    sb.append("if (scratchDir == null) {");
                    sb.append("forkEnvironment.addJVMArgument(\"-Djava.io.tmpdir=\"");
                    sb.append("+ System.getProperty(\"java.io.tmpdir\"));");
                    sb.append("} else {");
                    sb.append("forkEnvironment.addJVMArgument(\"-Djava.io.tmpdir=\" + scratchDir);}");

                    final ForkEnvironment f = new ForkEnvironment();
                    try {
                        f.setEnvScript(new SimpleScript(sb.toString(), "js"));
                    } catch (InvalidScriptException e) {
                        throw new PASchedulerException(e);
                    }
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
                schedulerTask.addArgument("functionName", taskConfigs[i][j].getFunctionName());
                schedulerTask.addArgument("outputs", taskConfigs[i][j].getOutputs());
                if (oldTask != null) {
                    schedulerTask.addDependence(oldTask);
                }
                oldTask = schedulerTask;

                if (config.isZipSourceFiles() && taskConfigs[i][j].getSourceZipFileName() != null) {
                    schedulerTask.addInputFiles(subDir + taskConfigs[i][j].getSourceZipFileName(),
                            InputAccessMode.TransferFromInputSpace);
                }

                if (config.isZipSourceFiles() && config.getSourceZipFileName() != null) {
                    schedulerTask.addInputFiles(subDir + config.getSourceZipFileName(),
                            InputAccessMode.TransferFromInputSpace);
                }
                if (taskConfigs[i][j].getSourceFileNames() != null) {
                    for (String fn : taskConfigs[i][j].getSourceFileNames()) {
                        schedulerTask.addInputFiles(subDir + fn, InputAccessMode.TransferFromInputSpace);
                    }
                }

                if (config.isTransferEnv()) {

                    schedulerTask.addInputFiles(subDir + config.getEnvMatFileName(),
                            InputAccessMode.TransferFromInputSpace);

                }

                schedulerTask.addInputFiles(subDir + taskConfigs[i][j].getInputVariablesFileName(),
                        InputAccessMode.TransferFromInputSpace);
                if (taskConfigs[i][j].getComposedInputVariablesFileName() != null) {
                    schedulerTask.addInputFiles(subDir +
                        taskConfigs[i][j].getComposedInputVariablesFileName(),
                            InputAccessMode.TransferFromInputSpace);
                }
                schedulerTask.addOutputFiles(subDir + taskConfigs[i][j].getOutputVariablesFileName(),
                        OutputAccessMode.TransferToOutputSpace);

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

                // Topology
                if (taskConfigs[i][j].getNbNodes() > 1) {
                    switch (taskConfigs[i][j].getTopology()) {
                        case ARBITRARY:
                            schedulerTask.setParallelEnvironment(new ParallelEnvironment(taskConfigs[i][j]
                                    .getNbNodes(), TopologyDescriptor.ARBITRARY));
                            break;
                        case BEST_PROXIMITY:
                            schedulerTask.setParallelEnvironment(new ParallelEnvironment(taskConfigs[i][j]
                                    .getNbNodes(), TopologyDescriptor.BEST_PROXIMITY));
                            break;
                        case SINGLE_HOST:
                            schedulerTask.setParallelEnvironment(new ParallelEnvironment(taskConfigs[i][j]
                                    .getNbNodes(), TopologyDescriptor.SINGLE_HOST));
                            break;
                        case SINGLE_HOST_EXCLUSIVE:
                            schedulerTask.setParallelEnvironment(new ParallelEnvironment(taskConfigs[i][j]
                                    .getNbNodes(), TopologyDescriptor.SINGLE_HOST_EXCLUSIVE));
                            break;
                        case MULTIPLE_HOSTS_EXCLUSIVE:
                            schedulerTask.setParallelEnvironment(new ParallelEnvironment(taskConfigs[i][j]
                                    .getNbNodes(), TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE));
                            break;
                        case DIFFERENT_HOSTS_EXCLUSIVE:
                            schedulerTask.setParallelEnvironment(new ParallelEnvironment(taskConfigs[i][j]
                                    .getNbNodes(), TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE));
                            break;
                        case THRESHHOLD_PROXIMITY:
                            schedulerTask.setParallelEnvironment(new ParallelEnvironment(taskConfigs[i][j]
                                    .getNbNodes(), new ThresholdProximityDescriptor(taskConfigs[i][j]
                                    .getThresholdProximity())));
                            break;
                    }

                }

                schedulerTask.setExecutableClassName(ScilabExecutable.class.getName());

                if (taskConfigs[i][j].getCustomScriptUrl() != null) {
                    URL url = new URL(taskConfigs[i][j].getCustomScriptUrl());

                    SelectionScript sscript = null;

                    String[] params;
                    if (taskConfigs[i][j].getCustomScriptParams() != null &&
                        taskConfigs[i][j].getCustomScriptParams().trim().length() > 0) {

                        params = taskConfigs[i][j].getCustomScriptParams().split("\\s");

                    } else {
                        params = new String[0];
                    }
                    try {

                        sscript = new SelectionScript(url, params, !taskConfigs[i][j].isStaticScript());

                    } catch (InvalidScriptException e1) {
                        throw new PASchedulerException(e1);
                    }
                    schedulerTask.addSelectionScript(sscript);

                    printLog("Task " + i + "_" + j + ":" + " using task custom script (" +
                        (taskConfigs[i][j].isStaticScript() ? "static" : "dynamic") + ") " + url +
                        " with params : " + params);
                }

                if (config.getCustomScriptUrl() != null) {
                    URL url = new URL(config.getCustomScriptUrl());

                    SelectionScript sscript = null;

                    String[] params;
                    if (config.getCustomScriptParams() != null &&
                        config.getCustomScriptParams().trim().length() > 0) {

                        params = config.getCustomScriptParams().split("\\s");

                    } else {
                        params = new String[0];
                    }

                    try {

                        sscript = new SelectionScript(url, params, !config.isCustomScriptStatic());

                    } catch (InvalidScriptException e1) {
                        throw new PASchedulerException(e1);
                    }
                    schedulerTask.addSelectionScript(sscript);
                    printLog("Task " + i + "_" + j + ":" + " using global custom script (" +
                        (taskConfigs[i][j].isStaticScript() ? "static" : "dynamic") + ") " + url +
                        " with params : " + params);
                }

                URL url1 = new URL(config.getCheckMatSciUrl());

                SelectionScript sscript = null;
                try {
                    sscript = new SelectionScript(url1, new String[] { "versionPref",
                            config.getVersionPref(), "versionRej", config.getVersionRejAsString(),
                            "versionMin", config.getVersionMin(), "versionMax", config.getVersionMax() },
                        false);
                } catch (InvalidScriptException e1) {
                    throw new PASchedulerException(e1);
                }
                schedulerTask.addSelectionScript(sscript);

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
            if (debug) {
                printLog("Job " + jid + " submitted.");
            }
            jpinfo = new MatSciJobPermanentInfo(jid, nbResults, depth, config, tnames, finaltnames);
            currentJobs.put(jid, new MatSciJobVolatileInfo(jpinfo));

        } catch (SchedulerException e) {
            throw new PASchedulerException(e);
        }

        return jpinfo;

    }

    public ScilabResultsAndLogs waitResult(String jid, String tname) {
        return getResultOfTask(jid, tname);
    }

}
