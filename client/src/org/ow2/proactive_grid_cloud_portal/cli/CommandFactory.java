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

import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.CACERTS;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.CACERTS_PASSWORD;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.CHANGE_JOB_PRIORITY;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.EVAL_SCRIPT;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.FREEZE_SCHEDULER;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.GET_JOB_OUTPUT;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.GET_JOB_RESULT;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.GET_JOB_STATE;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.GET_STATS;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.GET_TASK_OUTPUT;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.GET_TASK_RESULT;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.HELP;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.INSECURE;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.KILL_JOB;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.KILL_SCHEDULER;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.LINK_RM;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.LIST_JOBS;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.LOGIN;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.LOGIN_WITH_CREDENTIALS;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.PASSWORD;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.PAUSE_JOB;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.PAUSE_SCHEDULER;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.PREEMPT_TASK;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.REMOVE_JOB;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.RESTART_TASK;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.RESUME_JOB;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.RESUME_SCHEDULER;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.START_IMODE;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.START_SCHEDULER;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.STOP_SCHEDULER;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.SUBMIT_JOB_ARCH;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.SUBMIT_JOB_DESC;
import static org.ow2.proactive_grid_cloud_portal.cli.RestCommand.URL;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.ChangeJobPriorityCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.EvalScriptCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.FreezeSchedulerCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.GetJobOutputCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.GetJobResultCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.GetJobStateCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.GetStatsCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.GetTaskOutputCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.GetTaskResultCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.HelpCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.KillJobCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.KillSchedulerCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.LinkResourceManagerCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.ListJobsCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.LoggingCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.LoggingWithCredentialsCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.PauseJobCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.PauseSchedulerCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.PreemptTaskCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.RemoveJobCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.RestartTaskCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.ResumeJobCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.ResumeSchedulerCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.SetCaCertsCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.SetCaCertsPassCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.SetInsecureAccessCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.SetPasswordCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.SetUrlCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.StartIModeCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.StartSchedulerCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.StopSchedulerCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.SubmitJobCommand;

/**
 * {@link CommandFactory} builds an ordered {@link Command} list for a given
 * user argument set.
 */
public class CommandFactory {

    private static Map<String, Class<? extends AbstractCommand>> commands = new HashMap<String, Class<? extends AbstractCommand>>();

    static {
        commands.put(opt(URL), SetUrlCommand.class);
        commands.put(opt(HELP), HelpCommand.class);
        commands.put(opt(INSECURE), SetInsecureAccessCommand.class);
        commands.put(opt(CACERTS), SetCaCertsCommand.class);
        commands.put(opt(CACERTS_PASSWORD), SetCaCertsPassCommand.class);
        commands.put(opt(LOGIN), LoggingCommand.class);
        commands.put(opt(PASSWORD), SetPasswordCommand.class);
        commands.put(opt(LOGIN_WITH_CREDENTIALS),
                LoggingWithCredentialsCommand.class);
        commands.put(opt(EVAL_SCRIPT), EvalScriptCommand.class);
        commands.put(opt(START_SCHEDULER), StartSchedulerCommand.class);
        commands.put(opt(STOP_SCHEDULER), StopSchedulerCommand.class);
        commands.put(opt(PAUSE_SCHEDULER), PauseSchedulerCommand.class);
        commands.put(opt(RESUME_SCHEDULER), ResumeSchedulerCommand.class);
        commands.put(opt(FREEZE_SCHEDULER), FreezeSchedulerCommand.class);
        commands.put(opt(KILL_SCHEDULER), KillSchedulerCommand.class);

        commands.put(opt(GET_STATS), GetStatsCommand.class);
        commands.put(opt(LIST_JOBS), ListJobsCommand.class);
        commands.put(opt(LINK_RM), LinkResourceManagerCommand.class);

        commands.put(opt(SUBMIT_JOB_DESC), SubmitJobCommand.class);
        commands.put(opt(SUBMIT_JOB_ARCH), SubmitJobCommand.class);

        commands.put(opt(GET_JOB_OUTPUT), GetJobOutputCommand.class);
        commands.put(opt(GET_JOB_RESULT), GetJobResultCommand.class);

        commands.put(opt(CHANGE_JOB_PRIORITY), ChangeJobPriorityCommand.class);
        commands.put(opt(GET_JOB_STATE), GetJobStateCommand.class);

        commands.put(opt(PAUSE_JOB), PauseJobCommand.class);
        commands.put(opt(RESUME_JOB), ResumeJobCommand.class);
        commands.put(opt(KILL_JOB), KillJobCommand.class);
        commands.put(opt(REMOVE_JOB), RemoveJobCommand.class);

        commands.put(opt(GET_TASK_OUTPUT), GetTaskOutputCommand.class);
        commands.put(opt(GET_TASK_RESULT), GetTaskResultCommand.class);
        commands.put(opt(PREEMPT_TASK), PreemptTaskCommand.class);
        commands.put(opt(RESTART_TASK), RestartTaskCommand.class);

        commands.put(opt(START_IMODE), StartIModeCommand.class);
    }

    private CommandFactory() {
    }

    private static String opt(RestCommand command) {
        return command.getOpt();
    }

    /**
     * Returns an ordered {@link Command} list for specified user arguments.
     * 
     * @param cli
     *            the command-line arguments
     * @return an ordered {@link Command} list.
     */
    public static List<Command> getCommandList(CommandLine cli) {
        LinkedList<Command> list = new LinkedList<Command>();
        Map<String, Command> map = getCommands(cli);
        if (map.containsKey(opt(HELP))) {
            list.add(map.remove(opt(HELP)));
            return list;
        }

        if (map.containsKey(opt(URL))) {
            list.addFirst(map.remove(opt(URL)));
        }

        if (map.containsKey(opt(INSECURE))) {
            list.add(map.remove(opt(INSECURE)));
        } else if (map.containsKey(opt(CACERTS))) {
            list.add(map.remove(opt(CACERTS)));
            if (map.containsKey(opt(CACERTS_PASSWORD))) {
                list.add(map.remove(opt(CACERTS_PASSWORD)));
            }
        }

        if (map.containsKey(opt(PASSWORD))) {
            list.add(map.remove(opt(PASSWORD)));
        }

        if (map.containsKey(opt(LOGIN))) {
            list.add(map.remove(opt(LOGIN)));

        } else if (map.containsKey(opt(LOGIN_WITH_CREDENTIALS))) {
            list.add(map.remove(opt(LOGIN_WITH_CREDENTIALS)));
        }

        list.addAll(map.values());

        return list;
    }

    private static Map<String, Command> getCommands(CommandLine cli) {
        Map<String, Command> cliCommands = new HashMap<String, Command>();
        for (Option o : cli.getOptions()) {
            cliCommands.put(o.getOpt(), getCommand(o));
        }
        return cliCommands;
    }

    private static Command getCommand(Option opt) {
        try {
            Class<?> commandClass = commands.get(opt.getOpt());
            Constructor<?>[] ctors = commandClass.getConstructors();
            if (ctors.length > 0) {
                Constructor<?> selected = null;
                int numOfCtorParams = (opt.getValues() == null) ? 0 : opt
                        .getValues().length;
                for (Constructor<?> ctor : ctors) {
                    // naive way of selecting the most suitable ctor
                    if (numOfCtorParams == ctor.getParameterTypes().length) {
                        selected = ctor;
                        break;
                    }
                }
                if (selected != null) {
                    return getInstance(selected, opt.getValues());
                }
            }

            throw new IllegalArgumentException(
                    String.format(
                            "%s %s %s%n%s %s",
                            "No suitable command found for",
                            opt.getOpt(),
                            opt.getValuesList(),
                            "Check whether you have specified all required arguments for",
                            opt.getOpt()));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Command getInstance(Constructor<?> ctor, String[] args)
            throws Exception {
        Object[] ctorArgs = new Object[ctor.getParameterTypes().length];
        if (args != null) {
            System.arraycopy(args, 0, ctorArgs, 0, args.length);
        }
        return (Command) ctor.newInstance(ctorArgs);
    }

}
