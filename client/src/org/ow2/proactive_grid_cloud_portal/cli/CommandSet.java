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
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli;

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_INVALID_ARGUMENTS;
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_OTHER;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.cli.Option;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.EvalScriptCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.ExitCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginWithCredentialsCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.OutputCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.PrintSessionCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.SetCaCertsCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.SetCaCertsPassCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.SetInsecureAccessCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.SetPasswordCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.SetSessionCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.SetSessionFileCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.SetSilentCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.SetUrlCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.AddNodeCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.CreateNodeSourceCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.ForceCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.GetNodeInfoCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.GetTopologyCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.ListInfrastructureCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.ListNodeCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.ListNodeSourceCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.ListPolicyCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.LockNodeCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.RemoveNodeCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.RemoveNodeSourceCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.RmHelpCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.RmImodeCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.RmJsHelpCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.RmStatsCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetInfrastructureCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetNodeSourceCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetPolicyCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.UnlockNodeCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.ChangeJobPriorityCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.FreezeCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.GetJobOutputCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.GetJobResultCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.GetJobStateCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.GetTaskOutputCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.GetTaskResultCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.KillCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.KillJobCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.LinkRmCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.ListJobCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.PauseCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.PauseJobCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.PreemptTaskCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.RemoveJobCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.RestartTaskCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.ResumeCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.ResumeJobCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.SchedHelpCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.SchedImodeCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.SchedJsHelpCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.SchedStatsCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.StartCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.StopCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.SubmitJobCommand;

/**
 * Defines the set of commands and their parameters supported by Scheduler and
 * Resource Manager CLIs.
 */
public class CommandSet {

    public static final CommandSet.Entry URL = CommandSetEntryBuilder
            .newInstance().opt("u").longOpt("url")
            .description("URL of REST server").hasArgs(true).numOfArgs(1)
            .argNames("server-url").jsCommand("url(url)")
            .commandClass(SetUrlCommand.class).entry();

    public static final CommandSet.Entry SESSION = CommandSetEntryBuilder
            .newInstance().opt("si").longOpt("session-id")
            .description("the session id of this session").hasArgs(true)
            .numOfArgs(1).argNames("session-id")
            .jsCommand("setSessionId(sessionId)")
            .commandClass(SetSessionCommand.class).entry();

    public static final CommandSet.Entry SESSION_FILE = CommandSetEntryBuilder
            .newInstance().opt("sif").longOpt("session-id-file")
            .description("the session id file for this session").hasArgs(true)
            .numOfArgs(1).argNames("session-file")
            .jsCommand("setSessionIdFile(sessionIdFile)")
            .commandClass(SetSessionFileCommand.class).entry();

    public static final CommandSet.Entry LOGIN = CommandSetEntryBuilder
            .newInstance().opt("l").longOpt("login")
            .description("the login name to connect to REST server")
            .hasArgs(true).numOfArgs(1).argNames("login-name")
            .jsCommand("login(login-name)").commandClass(LoginCommand.class)
            .entry();

    public static final CommandSet.Entry PASSWORD = CommandSetEntryBuilder
            .newInstance().opt("p").longOpt("password")
            .description("the password to connect to REST server")
            .hasArgs(true).numOfArgs(1).argNames("password")
            .commandClass(SetPasswordCommand.class).entry();

    public static final CommandSet.Entry CREDENTIALS = CommandSetEntryBuilder
            .newInstance().opt("c").longOpt("credentials")
            .description("Path to the credential file").hasArgs(true)
            .numOfArgs(1).argNames("cred-path")
            .jsCommand("loginwithcredentials(cred-path)")
            .commandClass(LoginWithCredentialsCommand.class).entry();

    public static final CommandSet.Entry CACERTS = CommandSetEntryBuilder
            .newInstance()
            .opt("ca")
            .longOpt("cacerts")
            .description(
                    "CA certificate store (JKS type) to verify peer against (SSL)")
            .hasArgs(true).numOfArgs(1).argNames("store-path")
            .jsCommand("cacerts(store-path)")
            .commandClass(SetCaCertsCommand.class).entry();

    public static final CommandSet.Entry CACERTS_PASSWORD = CommandSetEntryBuilder
            .newInstance().opt("cap").longOpt("cacertspass")
            .description("Password for CA certificate store").hasArgs(true)
            .numOfArgs(1).argNames("store-password")
            .jsCommand("cacertspass(cacerts-pass)")
            .commandClass(SetCaCertsPassCommand.class).entry();

    public static final CommandSet.Entry INSECURE = CommandSetEntryBuilder
            .newInstance()
            .opt("k")
            .longOpt("insecure")
            .description(
                    "Allow connections to SSL sites without certs verification")
            .commandClass(SetInsecureAccessCommand.class).entry();

    public static final CommandSet.Entry SILENT = CommandSetEntryBuilder
            .newInstance().opt("z").longOpt("silent")
            .description("Runs the command-line client in the silent mode.")
            .jsCommand("silent()").commandClass(SetSilentCommand.class).entry();

    public static final CommandSet.Entry PRINT = CommandSetEntryBuilder
            .newInstance().opt("pr").longOpt("print")
            .description("Print the session id").jsCommand("prints()")
            .commandClass(PrintSessionCommand.class).entry();

    public static final CommandSet.Entry OUTPUT = CommandSetEntryBuilder
            .newInstance()
            .opt("o")
            .longOpt("output-file")
            .description(
                    "Output the result of command execution to specified file")
            .hasArgs(true).numOfArgs(1).commandClass(OutputCommand.class)
            .entry();

    public static final CommandSet.Entry EXIT = CommandSetEntryBuilder
            .newInstance().opt("").longOpt("")
            .description("Exit interactive shell").jsCommand("exit()")
            .commandClass(ExitCommand.class).entry();

    public static final CommandSet.Entry SUBMIT_DESC = CommandSetEntryBuilder
            .newInstance().opt("s").longOpt("submit")
            .description("Submit the specified job-description (XML) file")
            .hasArgs(true).numOfArgs(1).argNames("job-descriptor")
            .jsCommand("submit(job-descriptor)")
            .commandClass(SubmitJobCommand.class).entry();

    public static final CommandSet.Entry SUBMIT_ARCH = CommandSetEntryBuilder
            .newInstance().opt("sa").longOpt("submitarchive")
            .description("Submit the specified job archive (JAR|ZIP) file")
            .hasArgs(true).numOfArgs(1).argNames("job-archive")
            .jsCommand("submitarchive(job-archive)")
            .commandClass(SubmitJobCommand.class).entry();

    public static final CommandSet.Entry SCHED_START = CommandSetEntryBuilder
            .newInstance().opt("start").longOpt("startscheduler")
            .description("Start the scheduler").jsCommand("start()")
            .commandClass(StartCommand.class).entry();

    public static final CommandSet.Entry SCHED_STOP = CommandSetEntryBuilder
            .newInstance().opt("stop").longOpt("stopscheduler")
            .description("Stop the scheduler").jsCommand("stop()")
            .commandClass(StopCommand.class).entry();

    public static final CommandSet.Entry SCHED_PAUSE = CommandSetEntryBuilder
            .newInstance().opt("pause").longOpt("pausescheduler")
            .description("Pause the scheduler (pause all non-running jobs)")
            .jsCommand("pause()").commandClass(PauseCommand.class).entry();

    public static final CommandSet.Entry SCHED_RESUME = CommandSetEntryBuilder
            .newInstance().opt("resume").longOpt("resumescheduler")
            .description("Resume the scheduler").jsCommand("resume()")
            .commandClass(ResumeCommand.class).entry();

    public static final CommandSet.Entry SCHED_FREEZE = CommandSetEntryBuilder
            .newInstance().opt("freeze").longOpt("freezescheduler")
            .description("Freeze the scheduler (pause all non-running jobs)")
            .jsCommand("freeze()").commandClass(FreezeCommand.class).entry();

    public static final CommandSet.Entry SCHED_KILL = CommandSetEntryBuilder
            .newInstance().opt("kill").longOpt("killscheduler")
            .description("Kill the scheduler").jsCommand("kill()")
            .commandClass(KillCommand.class).entry();

    public static final CommandSet.Entry SCHED_STATS = CommandSetEntryBuilder
            .newInstance().opt("stats").longOpt("statistics")
            .description("Retrieve current scheduler statistics")
            .jsCommand("stats()").commandClass(SchedStatsCommand.class).entry();

    public static final CommandSet.Entry LINK_RM = CommandSetEntryBuilder
            .newInstance().opt("lrm").longOpt("linkrm")
            .description("Reconnect a resource manager to the scheduler")
            .hasArgs(true).numOfArgs(1).argNames("rm-url")
            .jsCommand("linkrm(rm_url)").commandClass(LinkRmCommand.class)
            .entry();

    public static final CommandSet.Entry JOB_LIST = CommandSetEntryBuilder
            .newInstance()
            .opt("lj")
            .longOpt("listjobs")
            .description("Retrieve a list of all jobs managed by the scheduler")
            .jsCommand("listjobs()").commandClass(ListJobCommand.class).entry();

    public static final CommandSet.Entry JOB_OUTPUT = CommandSetEntryBuilder
            .newInstance().opt("jo").longOpt("joboutput")
            .description("Retrieve the output of specified job").hasArgs(true)
            .numOfArgs(1).argNames("job-id").jsCommand("joboutput(id)")
            .commandClass(GetJobOutputCommand.class).entry();

    public static final CommandSet.Entry JOB_PRIORITY = CommandSetEntryBuilder
            .newInstance().opt("jp").longOpt("jobpriority")
            .description("Change the priority of the specified job")
            .hasArgs(true).numOfArgs(2).argNames("job-id priority")
            .jsCommand("jobpriority(job_id,priority)")
            .commandClass(ChangeJobPriorityCommand.class).entry();

    public static final CommandSet.Entry JOB_RESULT = CommandSetEntryBuilder
            .newInstance().opt("jr").longOpt("jobresult")
            .description("Retrieve the result of specified job").hasArgs(true)
            .numOfArgs(1).argNames("job-id").jsCommand("jobresult(job-id)")
            .commandClass(GetJobResultCommand.class).entry();

    public static final CommandSet.Entry JOB_STATE = CommandSetEntryBuilder
            .newInstance().opt("js").longOpt("jobstate")
            .description("Retrieve the current state of specified job")
            .hasArgs(true).numOfArgs(1).argNames("job-id")
            .jsCommand("jobstate(job-id)")
            .commandClass(GetJobStateCommand.class).entry();

    public static final CommandSet.Entry JOB_PAUSE = CommandSetEntryBuilder
            .newInstance()
            .opt("pj")
            .longOpt("pausejob")
            .description(
                    "Pause the specified job (pause all non-running tasks)")
            .hasArgs(true).numOfArgs(1).argNames("job-id")
            .jsCommand("pausejob(job-id)").commandClass(PauseJobCommand.class)
            .entry();

    public static final CommandSet.Entry JOB_RESUME = CommandSetEntryBuilder
            .newInstance()
            .opt("rj")
            .longOpt("resumejob")
            .description(
                    "Resume the specified job (restart all 'paused' tasks)")
            .hasArgs(true).numOfArgs(1).argNames("job-id")
            .jsCommand("resumejob(job_id)")
            .commandClass(ResumeJobCommand.class).entry();

    public static final CommandSet.Entry JOB_KILL = CommandSetEntryBuilder
            .newInstance().opt("kj").longOpt("killjob")
            .description("Kill the specfied job").hasArgs(true).numOfArgs(1)
            .argNames("job-id").jsCommand("killjob(job-id)")
            .commandClass(KillJobCommand.class).entry();

    public static final CommandSet.Entry JOB_REMOVE = CommandSetEntryBuilder
            .newInstance().opt("rmj").longOpt("removejob")
            .description("Remove the specified job").hasArgs(true).numOfArgs(1)
            .argNames("job-id").jsCommand("removejob(job_id)")
            .commandClass(RemoveJobCommand.class).entry();

    public static final CommandSet.Entry TASK_OUTPUT = CommandSetEntryBuilder
            .newInstance().opt("to").longOpt("taskoutput")
            .description("Retrieve the output of specified task").hasArgs(true)
            .numOfArgs(2).argNames("job-id task-name")
            .jsCommand("taskoutput(job_id,task_name)")
            .commandClass(GetTaskOutputCommand.class).entry();

    public static final CommandSet.Entry TASK_RESULT = CommandSetEntryBuilder
            .newInstance().opt("tr").longOpt("taskresult")
            .description("Retrieve the result of specified task").hasArgs(true)
            .numOfArgs(2).argNames("job-id task-name")
            .jsCommand("taskresult(job-id,task-name)")
            .commandClass(GetTaskResultCommand.class).entry();

    public static final CommandSet.Entry TASK_PREEMPT = CommandSetEntryBuilder
            .newInstance()
            .opt("pt")
            .longOpt("preempttask")
            .description(
                    "Stop the specified task and re-schedules it after the specified delay")
            .hasArgs(true).argNames("job-id task-id [delay]")
            .jsCommand("preempttask(job-id,task-name,delay)")
            .commandClass(PreemptTaskCommand.class).entry();

    public static final CommandSet.Entry TASK_RESTART = CommandSetEntryBuilder
            .newInstance()
            .opt("rt")
            .longOpt("restarttask")
            .description("Restart the specified task after the specified delay")
            .hasArgs(true).numOfArgs(2).argNames("job-id task-name")
            .jsCommand("restarttask(job-id,task-name)")
            .commandClass(RestartTaskCommand.class).entry();

    public static final CommandSet.Entry EVAL = CommandSetEntryBuilder
            .newInstance().opt("sf").longOpt("script")
            .description("Evaluate the specified JavaScript file")
            .hasArgs(true)
            .argNames("script-path [param-1=value-1 param-2=value-2 ...]")
            .jsCommand("script(script-pathname,param1=value1,...)")
            .commandClass(EvalScriptCommand.class).entry();

    public static final CommandSet.Entry SCHED_HELP = CommandSetEntryBuilder
            .newInstance()
            .opt("h")
            .longOpt("help")
            .description(
                    "Prints the usage of REST command-line client for Scheduler")
            .commandClass(SchedHelpCommand.class).entry();

    public static final CommandSet.Entry SCHED_IMODE = CommandSetEntryBuilder
            .newInstance().opt("i").longOpt("imode")
            .description("Interactive mode of the REST CLI")
            .commandClass(SchedImodeCommand.class).entry();

    public static final CommandSet.Entry NODE_ADD = CommandSetEntryBuilder
            .newInstance().opt("a").longOpt("addnodes")
            .description("Add the specified nodes by their URLs").hasArgs(true)
            .argNames("node-url").jsCommand("addnode(node-url[,node-source])")
            .commandClass(AddNodeCommand.class).entry();

    public static final CommandSet.Entry NS_CREATE = CommandSetEntryBuilder
            .newInstance().opt("cn").longOpt("createns")
            .description("Create new node source").hasArgs(true)
            .argNames("node-source").jsCommand("createns(node-source)")
            .commandClass(CreateNodeSourceCommand.class).entry();

    public static final CommandSet.Entry NODE_REMOVE = CommandSetEntryBuilder
            .newInstance().opt("d").longOpt("removenodes")
            .description("Remove the specified node").hasArgs(true)
            .numOfArgs(1).argNames("node-url")
            .jsCommand("removenode(node-url)")
            .commandClass(RemoveNodeCommand.class).entry();

    public static final CommandSet.Entry FORCE = CommandSetEntryBuilder
            .newInstance()
            .opt("f")
            .longOpt("force")
            .description(
                    "Do not wait for buys nodes to be freed before removal")
            .commandClass(ForceCommand.class).entry();

    public static final CommandSet.Entry INFRASTRUCTURE = CommandSetEntryBuilder
            .newInstance().opt("in").longOpt("infrastructure")
            .description("Specify an infrastructure for the node source")
            .hasArgs(true).argNames("param1 param2 ...")
            .commandClass(SetInfrastructureCommand.class).entry();

    public static final CommandSet.Entry POLICY = CommandSetEntryBuilder
            .newInstance().opt("po").longOpt("policy")
            .description("Specify a policy for the node source").hasArgs(true)
            .argNames("param1 param2 ...").commandClass(SetPolicyCommand.class)
            .entry();

    public static final CommandSet.Entry NODE_LIST = CommandSetEntryBuilder
            .newInstance().opt("ln").longOpt("listnodes")
            .description("List nodes handled by the Resoruce Manager")
            .jsCommand("listnodes([node-source])")
            .commandClass(ListNodeCommand.class).entry();

    public static final CommandSet.Entry NS_LIST = CommandSetEntryBuilder
            .newInstance().opt("lns").longOpt("listns")
            .description("List node-sources handled by the Resource Manager")
            .jsCommand("listns()").commandClass(ListNodeSourceCommand.class)
            .entry();

    public static final CommandSet.Entry NODE_LOCK = CommandSetEntryBuilder
            .newInstance().opt("lon").longOpt("locknodes")
            .description("Lock specified nodes").hasArgs(true)
            .argNames("node1-url node2-url ...")
            .jsCommand("locknodes([node1-url,node2-url,...])")
            .commandClass(LockNodeCommand.class).entry();

    public static final CommandSet.Entry NODE_INFO = CommandSetEntryBuilder
            .newInstance().opt("ni").longOpt("nodeinfo")
            .description("Retrieve info of specified node").hasArgs(true)
            .numOfArgs(1).argNames("node-url")
            .commandClass(GetNodeInfoCommand.class)
            .jsCommand("nodeinfo(node-url)").entry();

    public static final CommandSet.Entry NS = CommandSetEntryBuilder
            .newInstance().opt("ns").longOpt("nodesource")
            .description("Specify node source name").hasArgs(true)
            .argNames("node-source").commandClass(SetNodeSourceCommand.class)
            .entry();

    public static final CommandSet.Entry NS_REMOVE = CommandSetEntryBuilder
            .newInstance().opt("r").longOpt("removens")
            .description("Remove specified node source").hasArgs(true)
            .argNames("node-source").jsCommand("removens(node-source)")
            .commandClass(RemoveNodeSourceCommand.class).entry();

    public static final CommandSet.Entry TOPOLOGY = CommandSetEntryBuilder
            .newInstance().opt("t").longOpt("topology")
            .description("Retrieve node topology").jsCommand("topology()")
            .commandClass(GetTopologyCommand.class).entry();

    public static final CommandSet.Entry NODE_UNLOCK = CommandSetEntryBuilder
            .newInstance().opt("ulon").longOpt("unlocknodes")
            .description("Unlock specified nodes").hasArgs(true)
            .argNames("node1-url node2-url ...")
            .jsCommand("unlocknodes([node1-url,node2-url,...])")
            .commandClass(UnlockNodeCommand.class).entry();

    public static final CommandSet.Entry INFRASTRUCTURE_LIST = CommandSetEntryBuilder
            .newInstance().opt("li").longOpt("listinfra")
            .description("List supported infrastructure types")
            .jsCommand("listinfrastructures()")
            .commandClass(ListInfrastructureCommand.class).entry;

    public static final CommandSet.Entry POLICY_LIST = CommandSetEntryBuilder
            .newInstance().opt("lp").longOpt("listpolicy")
            .description("List supported policy types")
            .jsCommand("listpolicies()").commandClass(ListPolicyCommand.class).entry;

    public static final CommandSet.Entry RM_IMODE = CommandSetEntryBuilder
            .newInstance().opt("i").longOpt("interactive")
            .description("Interactive mode of REST CLI")
            .commandClass(RmImodeCommand.class).entry();

    public static final CommandSet.Entry RM_HELP = CommandSetEntryBuilder
            .newInstance()
            .opt("h")
            .longOpt("help")
            .description(
                    "Prints the usage of REST command-line client for Resource Manager")
            .commandClass(RmHelpCommand.class).entry();

    public static final CommandSet.Entry RM_STATS = CommandSetEntryBuilder
            .newInstance().opt("stats").longOpt("statistics")
            .description("Retrieve current resource manager statistics")
            .jsCommand("stats()").commandClass(RmStatsCommand.class).entry();

    public static final CommandSet.Entry SCHED_JS_HELP = CommandSetEntryBuilder
            .newInstance().opt("").longOpt("").description("Interactive help")
            .jsCommand("help()").commandClass(SchedJsHelpCommand.class).entry();

    public static final CommandSet.Entry RM_JS_HELP = CommandSetEntryBuilder
            .newInstance().opt("").longOpt("").description("Interactive help")
            .jsCommand("help()").commandClass(RmJsHelpCommand.class).entry();

    /**
     * CommandSet.Entry objects which are common to both Scheduler and Resource
     * Manager CLIs
     */
    public static final CommandSet.Entry[] COMMON_COMMANDS = new CommandSet.Entry[] {
            URL, SESSION, SESSION_FILE, LOGIN, PASSWORD, CREDENTIALS, INSECURE,
            CACERTS, CACERTS_PASSWORD, EVAL, SILENT, PRINT, OUTPUT };

    /** CommandSet.Entry objects which are specific to Scheduler CLI */
    public static final CommandSet.Entry[] SCHED_ONLY = new CommandSet.Entry[] {
            SCHED_START, SCHED_STOP, SCHED_PAUSE, SCHED_RESUME, SCHED_FREEZE,
            SCHED_KILL, LINK_RM, SCHED_STATS, JOB_LIST, SUBMIT_DESC,
            SUBMIT_ARCH, JOB_STATE, JOB_OUTPUT, JOB_RESULT, JOB_PRIORITY,
            JOB_PAUSE, JOB_RESUME, JOB_KILL, JOB_REMOVE, TASK_RESTART,
            TASK_PREEMPT, TASK_OUTPUT, TASK_RESULT, SCHED_IMODE, SCHED_HELP };

    /** CommandSet.Entry objects which are specific to Resource Manager CLI */
    public static final CommandSet.Entry[] RM_ONLY = new CommandSet.Entry[] {
            NODE_ADD, NODE_LIST, NODE_INFO, NODE_LOCK, NODE_UNLOCK,
            NODE_REMOVE, NS_CREATE, NS_LIST, NS_REMOVE, NS, INFRASTRUCTURE,
            INFRASTRUCTURE_LIST, POLICY, POLICY_LIST, TOPOLOGY, FORCE,
            RM_STATS, RM_IMODE, RM_HELP };

    private CommandSet() {
    }

    /**
     * Description of a specific command.
     */
    public static class Entry implements Comparable<Entry> {

        /**
         * Name of this option.
         */
        private String opt;
        /**
         * Long name of this option.
         */
        private String longOpt;
        /**
         * Description of this option.
         */
        private String description;
        /**
         * Interactive shell command description of this option.
         */
        private String jsCommand = null;
        /**
         * Indicates whether this option has one or more arguments.
         */
        private boolean hasArgs = false;
        /**
         * Argument names of this option.
         */
        private String argNames;
        /**
         * Number of arguments of this option.
         */
        private int numOfArgs = -1;
        /**
         * Name of the Command class responsible for processing this option.
         */
        private Class<?> commandClass;

        private Entry() {
        }

        /**
         * Returns the name of this option.
         * 
         * @return the name of this option.
         */
        public String opt() {
            return opt;
        }

        /**
         * Sets the name of this option.
         * 
         * @param opt
         */
        private void setOpt(String opt) {
            this.opt = opt;
        }

        /**
         * Returns the long name of this option.
         * 
         * @return the long name of this option.
         */
        public String longOpt() {
            return longOpt;
        }

        /**
         * Sets the long name of this option.
         * 
         * @param longOpt
         *            the long name of this option.
         */
        private void setLongOpt(String longOpt) {
            this.longOpt = longOpt;
        }

        /**
         * Returns the description of this option.
         * 
         * @return the description of this option.
         */
        public String description() {
            return description;
        }

        /**
         * Sets the description of this option.
         * 
         * @param description
         *            the description of this option.
         */
        private void setDescription(String description) {
            this.description = description;
        }

        /**
         * Returns the description of interactive shell command of this option.
         * 
         * @return the interactive shell command description.
         */
        public String jsCommand() {
            return jsCommand;
        }

        /**
         * Sets the description of interactive shell command of this option.
         * 
         * @param jsCommand
         *            the interactive shell command description.
         */
        private void setJsCommand(String jsCommand) {
            this.jsCommand = jsCommand;
        }

        /**
         * Returns <tt>ture</tt> if this option has one or more arguments.
         * 
         * @return <tt>true</tt> if this option has one or more arguments.
         */
        public boolean hasArgs() {
            return hasArgs;
        }

        /**
         * Sets whether this option has one or more arguments.
         * 
         * @param hasArgs
         *            whether this option has one or more arguments.
         */
        private void setHasArgs(boolean hasArgs) {
            this.hasArgs = hasArgs;
        }

        /**
         * Returns the argument names of this option.
         * 
         * @return the argument names.
         */
        public String argNames() {
            return argNames;
        }

        /**
         * Sets the argument names of this option.
         * 
         * @param argNames
         *            the argument names.
         */
        public void setArgNames(String argNames) {
            this.argNames = argNames;
        }

        /**
         * Returns the number of arguments of this option. If not specified,
         * returns <tt>-1</tt>.
         * 
         * @return the number of arguments of this option.
         */
        public int numOfArgs() {
            return numOfArgs;
        }

        /**
         * Sets the number of arguments of this option.
         * 
         * @param numOfArgs
         *            the number of arguments of this option.
         */
        private void setNumOfArgs(int numOfArgs) {
            this.numOfArgs = numOfArgs;
        }

        /**
         * Returns an instance of Command class which is responsible for
         * processing this option along with any arguments specified.
         * 
         * @param option
         *            a wrapper instance which contains arguments of this
         *            option.
         * @return
         */
        public Command commandObject(Option option) {
            return CommandSetEntryHelper.newCommandObject(option, commandClass);
        }

        /**
         * Sets the name of the Command type responsible for processing this
         * option.
         * 
         * @param commandClass
         *            the class name of the Command type.
         */
        public void setCommandClass(Class<?> commandClass) {
            this.commandClass = commandClass;
        }

        @Override
        public int compareTo(Entry o) {
            return opt.compareTo(o.opt);
        }
    }

    private static class CommandSetEntryHelper {
        private CommandSetEntryHelper() {
        }

        public static Command newCommandObject(Option opt, Class<?> commandClass) {
            try {
                Constructor<?>[] ctors = commandClass.getConstructors();
                if (ctors.length > 0) {
                    String[] values = opt.getValues();
                    int numOfCtorArgs = (values == null) ? 0 : values.length;

                    for (Constructor<?> ctor : ctors) {
                        // naive way of selecting the most suitable ctor
                        Class<?>[] paramTypes = ctor.getParameterTypes();
                        if (paramTypes.length == 1
                                && (String[].class.equals(paramTypes[0]))) {
                            return (Command) ctor.newInstance((Object) values);

                        } else if (paramTypes.length == numOfCtorArgs) {
                            return newInstance(ctor, values);
                        }
                    }
                }

                throw new CLIException(
                        REASON_INVALID_ARGUMENTS,
                        String.format(
                                "%s %s %s:%n%s %s.",
                                "No suitable command found for",
                                opt.getOpt(),
                                opt.getValuesList(),
                                "Check whether you have specified all required arguments for",
                                opt.getOpt()));

            } catch (CLIException error) {
                throw error;
            } catch (IllegalArgumentException error) {
                throw new CLIException(REASON_INVALID_ARGUMENTS, error);
            } catch (Exception error) {
                throw new CLIException(REASON_OTHER, error);
            }
        }

        private static Command newInstance(Constructor<?> ctor, String[] args)
                throws IllegalArgumentException, InstantiationException,
                IllegalAccessException, InvocationTargetException {
            Object[] ctorArgs = new Object[ctor.getParameterTypes().length];
            if (args != null) {
                System.arraycopy(args, 0, ctorArgs, 0, args.length);
            }
            return (Command) ctor.newInstance(ctorArgs);
        }
    }

    /**
     * Utility class to build CommandSet.Entry objects.
     */
    private static class CommandSetEntryBuilder {

        private CommandSet.Entry entry = null;

        private CommandSetEntryBuilder() {
            entry = new CommandSet.Entry();
        }

        public static CommandSetEntryBuilder newInstance() {
            return new CommandSetEntryBuilder();
        }

        public CommandSetEntryBuilder opt(String opt) {
            entry.setOpt(opt);
            return this;
        }

        public CommandSetEntryBuilder longOpt(String longOpt) {
            entry.setLongOpt(longOpt);
            return this;
        }

        public CommandSetEntryBuilder description(String description) {
            entry.setDescription(description);
            return this;
        }

        public CommandSetEntryBuilder hasArgs(boolean hasArgs) {
            entry.setHasArgs(hasArgs);
            return this;
        }

        public CommandSetEntryBuilder numOfArgs(int numOfArgs) {
            entry.setNumOfArgs(numOfArgs);
            return this;
        }

        public CommandSetEntryBuilder argNames(String argNames) {
            entry.setArgNames(argNames);
            return this;
        }

        public CommandSetEntryBuilder jsCommand(String jsCommand) {
            entry.setJsCommand(jsCommand);
            return this;
        }

        public CommandSet.Entry entry() {
            return entry;
        }

        public CommandSetEntryBuilder commandClass(Class<?> commandClass) {
            entry.setCommandClass(commandClass);
            return this;
        }
    }

}
