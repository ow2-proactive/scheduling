/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.util.adminconsole;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.ow2.proactive.scheduler.common.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.util.userconsole.UserShell;


/**
 * AdminShell will help you to manage the scheduler.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class AdminShell extends UserShell {

    private static final String JS_INIT_FILE = "AdminActions.js";

    private static final String YES = "yes";
    private static final String NO = "no";
    private static final String YES_NO = "(" + YES + "/" + NO + ")";

    private static final String START_CMD = "start()";
    private static final String STOP_CMD = "stop()";
    private static final String PAUSE_CMD = "pause()";
    private static final String FREEZE_CMD = "freeze()";
    private static final String RESUME_CMD = "resume()";
    private static final String SHUTDOWN_CMD = "shutdown()";
    private static final String KILL_CMD = "kill()";
    private static final String LINK_RM_CMD = "linkrm(rmURL)";

    /**
     * Start the Scheduler administrator
     *
     * @param args the arguments to be passed
     */
    public static void main(String[] args) {
        shell = new AdminShell();
        shell.load(args);
    }

    @Override
    protected void connect() throws Exception {
        scheduler = auth.logAsAdmin(user, pwd);
        logger.info("\t-> Admin '" + user + "' successfully connected\n");
    }

    @Override
    protected OptionGroup addCommandLineOptions(Options options) {
        super.addCommandLineOptions(options);

        OptionGroup group = super.addCommandLineOptions(options);

        Option opt = new Option("start", false, control + "Start the Scheduler");
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("stop", false, control + "Stop the Scheduler");
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("pause", false, control +
            "Pause the Scheduler (cause all non-running jobs to be paused)");
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("freeze", false, control +
            "Freeze the Scheduler (cause all non-running tasks to be paused)");
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("resume", false, control + "Resume the Scheduler");
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("shutdown", false, control + "Shutdown the Scheduler");
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("kill", false, control + "Kill the Scheduler");
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("linkrm", true, control + "Reconnect a RM to the scheduler");
        opt.setArgName("rmURL");
        opt.setRequired(false);
        opt.setArgs(1);
        group.addOption(opt);

        options.addOptionGroup(group);

        return group;
    }

    @Override
    protected boolean startCommandLine(CommandLine cmd) {
        if (super.startCommandLine(cmd)) {
            intercativeMode = false;
            if (cmd.hasOption("start")) {
                start();
            } else if (cmd.hasOption("stop")) {
                stop();
            } else if (cmd.hasOption("pause")) {
                pause();
            } else if (cmd.hasOption("freeze")) {
                freeze();
            } else if (cmd.hasOption("resume")) {
                resume();
            } else if (cmd.hasOption("shutdown")) {
                shutdown();
            } else if (cmd.hasOption("kill")) {
                kill();
            } else if (cmd.hasOption("linkrm")) {
                linkRM(cmd.getOptionValue("linkrm"));
            } else {
                intercativeMode = true;
                return intercativeMode;
            }
        }
        return false;
    }

    //***************** COMMAND LISTENER *******************

    public static boolean start() {
        return ((AdminShell) shell).start_();
    }

    private boolean start_() {
        boolean success = false;
        try {
            success = ((AdminSchedulerInterface) scheduler).start().booleanValue();
        } catch (SchedulerException e) {
            error("Start Scheduler is not possible : " + e.getMessage());
            return false;
        }
        if (success) {
            printf("Scheduler started.");
        } else {
            printf("Scheduler cannot be started in its current state.");
        }
        return success;
    }

    public static boolean stop() {
        return ((AdminShell) shell).stop_();
    }

    private boolean stop_() {
        boolean success = false;
        try {
            success = ((AdminSchedulerInterface) scheduler).stop().booleanValue();
        } catch (SchedulerException e) {
            error("Stop Scheduler is not possible : " + e.getMessage());
            return false;
        }
        if (success) {
            printf("Scheduler stopped.");
        } else {
            printf("Scheduler cannot be stopped in its current state.");
        }
        return success;
    }

    public static boolean pause() {
        return ((AdminShell) shell).pause_();
    }

    private boolean pause_() {
        boolean success = false;
        try {
            success = ((AdminSchedulerInterface) scheduler).pause().booleanValue();
        } catch (SchedulerException e) {
            error("Pause Scheduler is not possible : " + e.getMessage());
            return false;
        }
        if (success) {
            printf("Scheduler paused.");
        } else {
            printf("Scheduler cannot be paused in its current state.");
        }
        return success;
    }

    public static boolean freeze() {
        return ((AdminShell) shell).freeze_();
    }

    private boolean freeze_() {
        boolean success = false;
        try {
            success = ((AdminSchedulerInterface) scheduler).freeze().booleanValue();
        } catch (SchedulerException e) {
            error("Freeze Scheduler is not possible : " + e.getMessage());
            return false;
        }
        if (success) {
            printf("Scheduler frozen.");
        } else {
            printf("Scheduler cannot be frozen in its current state.");
        }
        return success;
    }

    public static boolean resume() {
        return ((AdminShell) shell).resume_();
    }

    private boolean resume_() {
        boolean success = false;
        try {
            success = ((AdminSchedulerInterface) scheduler).resume().booleanValue();
        } catch (SchedulerException e) {
            error("Resume Scheduler is not possible : " + e.getMessage());
            return false;
        }
        if (success) {
            printf("Scheduler resumed.");
        } else {
            printf("Scheduler cannot be resumed in its current state.");
        }
        return success;
    }

    public static boolean shutdown() {
        return ((AdminShell) shell).shutdown_();
    }

    private boolean shutdown_() {
        boolean success = false;
        try {
            if (intercativeMode) {
                String s = console.readStatement("Are you sure you want to shutdown the Scheduler ? " +
                    YES_NO + " > ");
                success = s.equalsIgnoreCase(YES);
            }
            if (success || !intercativeMode) {
                try {
                    success = ((AdminSchedulerInterface) scheduler).shutdown().booleanValue();
                } catch (SchedulerException e) {
                    error("Shutdown Scheduler is not possible : " + e.getMessage());
                    return false;
                }
                if (success) {
                    printf("Shutdown sequence initialized, it might take a while to finish all executions, shell will exit.");
                    terminated = true;
                } else {
                    printf("Scheduler cannot be shutdown in its current state.");
                }
            } else {
                printf("Shutdown aborted !");
            }
        } catch (Exception e) {
            error("*ERROR* : " + e.getMessage());
        }
        return success;
    }

    public static boolean kill() {
        return ((AdminShell) shell).kill_();
    }

    private boolean kill_() {
        boolean success = false;
        try {
            if (intercativeMode) {
                String s = console.readStatement("Are you sure you want to kill the Scheduler ? " + YES_NO +
                    " > ");
                success = s.equalsIgnoreCase(YES);
            }
            if (success || !intercativeMode) {
                try {
                    success = ((AdminSchedulerInterface) scheduler).kill().booleanValue();
                } catch (SchedulerException e) {
                    error("Kill Scheduler is not possible : " + e.getMessage());
                    return false;
                }
                if (success) {
                    printf("Sheduler has just been killed, shell will exit.");
                    terminated = true;
                } else {
                    printf("Scheduler cannot be killed in its current state.");
                }
            } else {
                printf("Kill aborted !");
            }
        } catch (Exception e) {
            error("*ERROR* : " + e.getMessage());
        }
        return success;
    }

    public static boolean linkRM(String rmURL) {
        return ((AdminShell) shell).linkRM_(rmURL);
    }

    private boolean linkRM_(String rmURL) {
        boolean success = false;
        try {
            success = ((AdminSchedulerInterface) scheduler).linkResourceManager(rmURL.trim()).booleanValue();
            if (success) {
                printf("The new Resource Manager at " + rmURL + " has been rebind to the scheduler.");
            } else {
                error("Reconnect a Resource Manager is only possible when RM is dead !");
            }
        } catch (Exception e) {
            error("*ERROR* : " + e.getMessage());
        }
        return success;
    }

    //***************** OTHER *******************

    @Override
    protected void initialize() throws IOException {
        super.initialize();
        //read and launch Action.js
        BufferedReader br = new BufferedReader(new InputStreamReader(AdminShell.class
                .getResourceAsStream(JS_INIT_FILE)));
        eval(readFileContent(br));
    }

    //***************** HELP SCREEN *******************

    protected String helpScreen() {
        StringBuilder out = new StringBuilder(super.helpScreen());

        out.append(String.format("\n %1$-18s\t Starts Scheduler\n", START_CMD));
        out.append(String.format(" %1$-18s\t Stops Scheduler\n", STOP_CMD));
        out.append(String.format(
                " %1$-18s\t pauses Scheduler, causes every jobs but running one to be paused\n", PAUSE_CMD));
        out
                .append(String
                        .format(
                                " %1$-18s\t freezes Scheduler, causes all jobs to be paused (every non-running tasks are paused)\n",
                                FREEZE_CMD));
        out
                .append(String.format(" %1$-18s\t resumes Scheduler, causes all jobs to be resumed\n",
                        RESUME_CMD));
        out.append(String.format(" %1$-18s\t Waits for running jobs to finish and shutdown Scheduler\n",
                SHUTDOWN_CMD));
        out.append(String.format(" %1$-18s\t Kill every tasks and jobs and shutdown Scheduler\n", KILL_CMD));
        out
                .append(String
                        .format(
                                " %1$-18s\t Reconnect a Resource Manager (parameter is a string representing the new rmURL)\n",
                                LINK_RM_CMD));

        return out.toString();
    }

}
