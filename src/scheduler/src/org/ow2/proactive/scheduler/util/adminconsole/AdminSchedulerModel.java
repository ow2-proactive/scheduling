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
 * $PROACTIVE_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.util.adminconsole;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.ow2.proactive.scheduler.common.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.util.userconsole.UserSchedulerModel;
import org.ow2.proactive.utils.console.Command;


/**
 * AdminSchedulerModel is the class that drives the Scheduler console in the admin view.
 * To use this class, get the model, connect a Scheduler and a console, and just start this model.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class AdminSchedulerModel extends UserSchedulerModel {

    private static final String JS_INIT_FILE = "AdminActions.js";
    private static final String YES = "yes";
    private static final String NO = "no";
    private static final String YES_NO = "(" + YES + "/" + NO + ")";

    private ArrayList<Command> commands;

    public static AdminSchedulerModel getModel() {
        if (model == null) {
            model = new AdminSchedulerModel();
        }
        return (AdminSchedulerModel) model;
    }

    protected AdminSchedulerModel() {
        super();
        commands = new ArrayList<Command>();
        commands.add(new Command("start()", "Start Scheduler"));
        commands.add(new Command("stop()", "Stop Scheduler"));
        commands
                .add(new Command("pause()", "Pause Scheduler, causes every jobs but running one to be paused"));
        commands.add(new Command("freeze()",
            "Freeze Scheduler, causes all jobs to be paused (every non-running tasks are paused)"));
        commands.add(new Command("resume()", "Resume Scheduler, causes all jobs to be resumed"));
        commands.add(new Command("shutdown()", "Wait for running jobs to finish and shutdown Scheduler"));
        commands.add(new Command("kill()", "Kill every tasks and jobs and shutdown Scheduler"));
        commands.add(new Command("linkrm(rmURL)",
            "Reconnect a Resource Manager (parameter is a string representing the new rmURL)"));
    }

    /**
     * @see org.ow2.proactive.scheduler.common.util.userconsole.UserSchedulerModel#initialize()
     */
    @Override
    protected void initialize() throws IOException {
        super.initialize();
        //read and launch Action.js
        BufferedReader br = new BufferedReader(new InputStreamReader(AdminController.class
                .getResourceAsStream(JS_INIT_FILE)));
        eval(readFileContent(br));
    }

    //***************** COMMAND LISTENER *******************

    public static boolean start() {
        return ((AdminSchedulerModel) model).start_();
    }

    private boolean start_() {
        boolean success = false;
        try {
            success = ((AdminSchedulerInterface) scheduler).start().booleanValue();
        } catch (Exception e) {
            handleExceptionDisplay("Start Scheduler is not possible", e);
            return false;
        }
        if (success) {
            print("Scheduler started.");
        } else {
            print("Scheduler cannot be started in its current state.");
        }
        return success;
    }

    public static boolean stop() {
        return ((AdminSchedulerModel) model).stop_();
    }

    private boolean stop_() {
        boolean success = false;
        try {
            success = ((AdminSchedulerInterface) scheduler).stop().booleanValue();
        } catch (Exception e) {
            handleExceptionDisplay("Stop Scheduler is not possible", e);
            return false;
        }
        if (success) {
            print("Scheduler stopped.");
        } else {
            print("Scheduler cannot be stopped in its current state.");
        }
        return success;
    }

    public static boolean pause() {
        return ((AdminSchedulerModel) model).pause_();
    }

    private boolean pause_() {
        boolean success = false;
        try {
            success = ((AdminSchedulerInterface) scheduler).pause().booleanValue();
        } catch (Exception e) {
            handleExceptionDisplay("Pause Scheduler is not possible", e);
            return false;
        }
        if (success) {
            print("Scheduler paused.");
        } else {
            print("Scheduler cannot be paused in its current state.");
        }
        return success;
    }

    public static boolean freeze() {
        return ((AdminSchedulerModel) model).freeze_();
    }

    private boolean freeze_() {
        boolean success = false;
        try {
            success = ((AdminSchedulerInterface) scheduler).freeze().booleanValue();
        } catch (Exception e) {
            handleExceptionDisplay("Freeze Scheduler is not possible", e);
            return false;
        }
        if (success) {
            print("Scheduler frozen.");
        } else {
            print("Scheduler cannot be frozen in its current state.");
        }
        return success;
    }

    public static boolean resume() {
        return ((AdminSchedulerModel) model).resume_();
    }

    private boolean resume_() {
        boolean success = false;
        try {
            success = ((AdminSchedulerInterface) scheduler).resume().booleanValue();
        } catch (Exception e) {
            handleExceptionDisplay("Resume Scheduler is not possible", e);
            return false;
        }
        if (success) {
            print("Scheduler resumed.");
        } else {
            print("Scheduler cannot be resumed in its current state.");
        }
        return success;
    }

    public static boolean shutdown() {
        return ((AdminSchedulerModel) model).shutdown_();
    }

    private boolean shutdown_() {
        boolean success = false;
        try {
            if (!displayOnStdStream) {
                String s = console.readStatement("Are you sure you want to shutdown the Scheduler ? " +
                    YES_NO + " > ");
                success = s.equalsIgnoreCase(YES);
            }
            if (success || displayOnStdStream) {
                try {
                    success = ((AdminSchedulerInterface) scheduler).shutdown().booleanValue();
                } catch (SchedulerException e) {
                    error("Shutdown Scheduler is not possible : " + e.getMessage());
                    return false;
                }
                if (success) {
                    print("Shutdown sequence initialized, it might take a while to finish all executions, shell will exit.");
                    terminated = true;
                } else {
                    print("Scheduler cannot be shutdown in its current state.");
                }
            } else {
                print("Shutdown aborted !");
            }
        } catch (Exception e) {
            handleExceptionDisplay("*ERROR*", e);
        }
        return success;
    }

    public static boolean kill() {
        return ((AdminSchedulerModel) model).kill_();
    }

    private boolean kill_() {
        boolean success = false;
        try {
            if (!displayOnStdStream) {
                String s = console.readStatement("Are you sure you want to kill the Scheduler ? " + YES_NO +
                    " > ");
                success = s.equalsIgnoreCase(YES);
            }
            if (success || displayOnStdStream) {
                try {
                    success = ((AdminSchedulerInterface) scheduler).kill().booleanValue();
                } catch (SchedulerException e) {
                    error("Kill Scheduler is not possible : " + e.getMessage());
                    return false;
                }
                if (success) {
                    print("Sheduler has just been killed, shell will exit.");
                    terminated = true;
                } else {
                    print("Scheduler cannot be killed in its current state.");
                }
            } else {
                print("Kill aborted !");
            }
        } catch (Exception e) {
            handleExceptionDisplay("*ERROR*", e);
        }
        return success;
    }

    public static boolean linkRM(String rmURL) {
        return ((AdminSchedulerModel) model).linkRM_(rmURL);
    }

    private boolean linkRM_(String rmURL) {
        boolean success = false;
        try {
            success = ((AdminSchedulerInterface) scheduler).linkResourceManager(rmURL.trim()).booleanValue();
            if (success) {
                print("The new Resource Manager at " + rmURL + " has been rebound to the scheduler.");
            } else {
                error("Reconnect a Resource Manager is only possible when RM is dead !");
            }
        } catch (Exception e) {
            handleExceptionDisplay("*ERROR*", e);
        }
        return success;
    }

    public static AdminSchedulerInterface getAdminScheduler() {
        return ((AdminSchedulerModel) model).getAdminScheduler_();
    }

    private AdminSchedulerInterface getAdminScheduler_() {
        return (AdminSchedulerInterface) scheduler;
    }

    //****************** HELP SCREEN ********************

    /**
     * @see org.ow2.proactive.scheduler.common.util.userconsole.UserSchedulerModel#helpScreen()
     */
    @Override
    protected String helpScreen() {
        StringBuilder out = new StringBuilder(super.helpScreen());

        out.append(newline);

        for (int i = 0; i < commands.size(); i++) {
            out.append(String.format(" %1$-" + cmdHelpMaxCharLength + "s %2$s" + newline, commands.get(i)
                    .getName(), commands.get(i).getDescription()));
        }

        return out.toString();
    }

}
