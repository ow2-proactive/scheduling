/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli;

public enum RestCommand {

    URL("u", "url", "URL of REST-Scheduler-API", "server-url", 1, false),

    LOGIN("l", "login",
            "the login_name to connect to REST-Scheduler-API server",
            "login_name", 1, false),

    PASSWORD("p", "password",
            "the login_name to connect to REST-Scheduler-API server",
            "password", 1, false),

    LOGIN_WITH_CREDENTIALS("c", "credentials", "Path to the credential file",
            "cred-file-path", 1, false),

    CACERTS("ca", "cacerts",
            "CA certificate store (JKS type) to verify peer against (SSL)",
            "store-path", 1, false),

    CACERTS_PASSWORD("cap", "cacertspass",
            "CA certificate store (JKS type) password (SSL)", "store-pass", 1,
            false),

    INSECURE("k", "insecure",
            "Allow connections to SSL sites without certs verification"),

    SUBMIT_JOB_DESC("s", "submit",
            "Submit the specified job-description (XML) file",
            "job-descriptor", 1, false, "submit(job-descriptor)"),

    SUBMIT_JOB_ARCH("sa", "submitarchive",
            "Submit the specified job archive (JAR|ZIP) file", "job-archive",
            1, false, "submitarchive(job-archive)"),

    START_SCHEDULER("start", "startscheduler", "Start the scheduler", "start()"),

    STOP_SCHEDULER("stop", "stopscheduler", "Stop the scheduler", "stop()"),

    PAUSE_SCHEDULER("pause", "pausescheduler",
            "Pause the scheduler (pause all non-running jobs)", "pause()"),

    RESUME_SCHEDULER("resume", "resumescheduler", "Resume the scheduler",
            "resume()"),

    FREEZE_SCHEDULER("freeze", "freezescheduler",
            "Freeze the scheduler (pause all non-running jobs)", "freeze()"),

    KILL_SCHEDULER("kill", "killscheduler", "Kill the scheduler", "kill()"),

    GET_STATS("stats", "statistics", "Retrieve current scheduler statistics",
            "stats()"),

    LIST_JOBS("lj", "listjobs",
            "Retrieve a list of all jobs managed by the scheduler",
            "listjobs()"),

    LINK_RM("lrm", "linkrm", "Reconnect a resource manager to the scheduler",
            "rm_url", 1, false, "linkrm(rm_url"),

    GET_JOB_OUTPUT("jo", "joboutput", "Retrieve the output of specified job",
            "job_id", 1, false, "joboutput(id)"),

    CHANGE_JOB_PRIORITY("jp", "jobpriority",
            "Change the priority of the specified job", "job_id priority", 2,
            false, "jobpriority(job_id,priority)"),

    GET_JOB_RESULT("jr", "jobresult", "Retrieve the result of specified job",
            "job_id", 1, false, "jobresult(job_id)"),

    GET_JOB_STATE("js", "jobstate",
            "Retrieve the current state of specified job", "job_id", 1, false,
            "jobstate(job_id)"),

    PAUSE_JOB("pj", "pausejob",
            "Pause the specified job (pause all non-running tasks)", "job-id",
            1, false, "pausejob(job_id)"),

    RESUME_JOB("rj", "resumejob",
            "Resume the specified job (restart all 'paused' tasks)", "job-id",
            1, false, "resumejob(job_id)"),

    KILL_JOB("kj", "killjob", "Kill the specfied job", "job-id", 1, false,
            "killjob(job_id)"),

    REMOVE_JOB("rmj", "removejob", "Remove the specified job", "job-id", 1,
            false, "removejob(job_id)"),

    GET_TASK_OUTPUT("to", "taskoutput",
            "Retrieve the output of specified task", "job_id task_name", 2,
            false, "taskoutput(job_id,task_name)"),

    GET_TASK_RESULT("tr", "taskresult",
            "Retrieve the result of specified task ", "job-id task_name", 2,
            false, "taskresult(job_id,task_name)"),

    PREEMPT_TASK(
            "pt",
            "preempttask",
            "Stop the specified task and re-schedules it after the specified delay",
            "job_id task_id [delay]", 3, false,
            "preempttask(job_id,task_name,delay)"),

    RESTART_TASK("rt", "restarttask",
            "Restart the specified task after the specified delay",
            "job_id task_name", 2, false, "restarttask(job_id,task_name)"),

    EVAL_SCRIPT("sf", "script", "Evaluate the specified JavaScript file",
            "script-pathname [param1=value1,param2=value2...]", 2, false,
            "script(script-pathname,param-value)"),

    HELP("h", "help",
            "Prints the usage of REST command-line client for Scheduler",
            "help()"),

    START_IMODE("i", "imode", "Interactive mode of the REST CLI", null);

    private String opt;
    private String longOpt;
    private String description;
    private String argsName;
    private int argsNum;
    private boolean argsRequired;
    private String jsOpt;

    RestCommand(String opt, String longOpt, String description) {
        this(opt, longOpt, description, null);
    }

    RestCommand(String opt, String longOpt, String description, String jsOpt) {
        this(opt, longOpt, description, null, 0, false, jsOpt);
    }

    RestCommand(String opt, String longOpt, String description,
            String argsName, int argsNum, boolean isArgsRequired) {
        this(opt, longOpt, description, argsName, argsNum, isArgsRequired, null);
    }

    RestCommand(String opt, String longOpt, String description,
            String argsName, int argsNum, boolean isArgsRequired, String jsOpt) {
        this.opt = opt;
        this.longOpt = longOpt;
        this.description = description;
        this.argsName = argsName;
        this.argsNum = argsNum;
        this.argsRequired = isArgsRequired;
        this.jsOpt = jsOpt;
    }

    public String getOpt() {
        return opt;
    }

    public void setOpt(String opt) {
        this.opt = opt;
    }

    public String getLongOpt() {
        return longOpt;
    }

    public void setLongOpt(String longOpt) {
        this.longOpt = longOpt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArgsName() {
        return argsName;
    }

    public void setArgsName(String argsName) {
        this.argsName = argsName;
    }

    public int getArgsNum() {
        return argsNum;
    }

    public void setArgsNum(int argsNum) {
        this.argsNum = argsNum;
    }

    public boolean isArgsRequired() {
        return argsRequired;
    }

    public void setArgsRequired(boolean argsRequired) {
        this.argsRequired = argsRequired;
    }

    public String getJsOpt() {
        return jsOpt;
    }

    public void setJsOpt(String jsOpt) {
        this.jsOpt = jsOpt;
    }
}
