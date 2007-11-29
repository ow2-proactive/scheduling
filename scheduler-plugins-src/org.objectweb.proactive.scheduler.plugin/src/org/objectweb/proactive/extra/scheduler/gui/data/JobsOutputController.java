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
package org.objectweb.proactive.extra.scheduler.gui.data;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.objectweb.proactive.extra.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.task.Log4JTaskLogs;
import org.objectweb.proactive.extra.scheduler.gui.Activator;
import org.objectweb.proactive.extra.scheduler.gui.views.JobOutput;


/**
 * Create, show and remove jobs output
 *
 * @author FRADJ Johann
 * @version 1.0, Jul 12, 2007
 * @since ProActive 3.2
 */
public class JobsOutputController {

    /** title of the job output view */
    public static final String PREFIX_JOB_OUTPUT_TITLE = "Job #";

    // The shared instance
    private static JobsOutputController instance = null;
    private Map<JobId, JobOutputAppender> appenders = null;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    private JobsOutputController() {
        appenders = new HashMap<JobId, JobOutputAppender>();
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static JobsOutputController getInstance() {
        if (instance == null) {
            instance = new JobsOutputController();
        }
        return instance;
    }

    public static void clearInstance() {
        if (instance != null) {
            instance.removeAllJobOutput();
            instance = null;
        }
    }

    /**
     * This method tries to show the output of a job (identified by the given
     * jobId). This method can't show the output if it has never been created.
     * In order to create an output use
     * {@link JobsOutputController#createJobOutput(JobId) createJobOutput}.
     *
     * @param jobId the jobId
     * @return true only if the output was created
     * @see JobsOutputController#createJobOutput(JobId)
     */
    public boolean showJobOutput(JobId jobId) {
        JobOutputAppender joa = appenders.get(jobId);
        if (joa == null) {
            return false;
        }
        ConsolePlugin.getDefault().getConsoleManager()
                     .showConsoleView(joa.getJobOutput());
        return true;
    }

    /**
     * Create an output for a job identified by the given jobId
     *
     * @param jobId the jobId
     * @throws SchedulerException
     */
    public void createJobOutput(JobId jobId) {
        try {
            if (!showJobOutput(jobId)) {
                SchedulerProxy.getInstance()
                              .listenLog(jobId, Activator.getHostname(),
                    Activator.getListenPortNumber());
                JobOutputAppender joa = new JobOutputAppender(new JobOutput(PREFIX_JOB_OUTPUT_TITLE +
                            jobId));
                joa.setLayout(Log4JTaskLogs.DEFAULT_LOG_LAYOUT);
                Logger log = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX +
                        jobId);
                log.setAdditivity(false);
                log.setLevel(Level.ALL);
                log.removeAllAppenders();
                log.addAppender(joa);
                appenders.put(jobId, joa);
                showJobOutput(jobId);
            }
        } catch (Exception e) {
            //TODO a virer c t pour les tests....
            e.printStackTrace();
        }
    }

    /**
     * To remove an output for a job identified by the given jobId
     *
     * @param jobId the jobId
     */
    public void removeJobOutput(JobId jobId) {
        JobOutputAppender joa = appenders.get(jobId);
        if (joa != null) {
            ConsolePlugin.getDefault().getConsoleManager()
                         .removeConsoles(new IConsole[] { joa.getJobOutput() });
        }
        appenders.remove(jobId);
    }

    /**
     * Remove all output ! This method clear the console.
     */
    public void removeAllJobOutput() {
        for (JobOutputAppender joa : appenders.values())
            ConsolePlugin.getDefault().getConsoleManager()
                         .removeConsoles(new IConsole[] { joa.getJobOutput() });
    }
}
