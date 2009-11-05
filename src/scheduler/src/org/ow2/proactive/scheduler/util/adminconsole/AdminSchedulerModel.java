/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.util.adminconsole;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.ow2.proactive.scheduler.common.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.policy.Policy;
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

    private static int logsNbLines = 20;
    private static String logsDirectory = System.getProperty("pa.scheduler.home") + File.separator + ".logs";

    private static final String schedulerLogFile = "Scheduler.log";
    private static final String schedulerDevLogFile = "SchedulerDev.log";

    private ArrayList<Command> commands;

    /**
     * Get this model. Also specify if the exit command should do something or not
     *
     * @param allowExitCommand true if the exit command is part of the commands, false if exit command does not exist.
     * @return the current model associated to this class.
     */
    public static AdminSchedulerModel getModel(boolean allowExitCommand) {
        if (model == null) {
            model = new AdminSchedulerModel(allowExitCommand);
        }
        return (AdminSchedulerModel) model;
    }

    /**
     * Get a new model. Also specify if the exit command should do something or not
     * WARNING, this method should just be used to re-create an instance of a model, it will disabled previous instance.
     *
     * @param allowExitCommand true if the exit command is part of the commands, false if exit command does not exist.
     * @return a brand new model associated to this class.
     */
    public static AdminSchedulerModel getNewModel(boolean allowExitCommand) {
        model = new AdminSchedulerModel(allowExitCommand);
        return (AdminSchedulerModel) model;
    }

    private static AdminSchedulerModel getModel() {
        return (AdminSchedulerModel) model;
    }

    protected AdminSchedulerModel(boolean allowExitCommand) {
        super(allowExitCommand);
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
        commands.add(new Command("changePolicy(fullName)",
            "Change the current scheduling policy, (argument is the new policy full name)"));
        commands.add(new Command("setLogsDir(logsDir)",
            "Set the directory where the log are located, (default is SCHEDULER_HOME/.logs"));
        commands.add(new Command("viewlogs(nbLines)",
            "View the last nbLines lines of the admin logs file, (default nbLines is 20)"));
        commands.add(new Command("viewDevlogs(nbLines)",
            "View the last nbLines lines of the dev logs file, (default nbLines is 20)"));
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
        return getModel().start_();
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
        return getModel().stop_();
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
        return getModel().pause_();
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
        return getModel().freeze_();
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
        return getModel().resume_();
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
        return getModel().shutdown_();
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
        return getModel().kill_();
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
        return getModel().linkRM_(rmURL);
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

    public static void setLogsDir(String logsDir) {
        if (logsDir == null || "".equals(logsDir)) {
            getModel().error("Given logs directory is null or empty !");
            return;
        }
        File dir = new File(logsDir);
        if (!dir.exists()) {
            getModel().error("Given logs directory does not exist !");
            return;
        }
        if (!dir.isDirectory()) {
            getModel().error("Given logsDir is not a directory !");
            return;
        }
        dir = new File(logsDir + File.separator + schedulerLogFile);
        if (!dir.exists()) {
            getModel().error("Given logs directory does not contains Scheduler logs files !");
            return;
        }
        getModel().print("Logs Directory set to '" + logsDir + "' !");
        logsDirectory = logsDir;
    }

    public static void viewlogs(String nbLines) {
        if (!"".equals(nbLines)) {
            try {
                logsNbLines = Integer.parseInt(nbLines);
            } catch (NumberFormatException nfe) {
                //logsNbLines not set
            }
        }
        getModel().print(readLastNLines(schedulerLogFile));
    }

    public static void viewDevlogs(String nbLines) {
        if (!"".equals(nbLines)) {
            try {
                logsNbLines = Integer.parseInt(nbLines);
            } catch (NumberFormatException nfe) {
                //logsNbLines not set
            }
        }
        getModel().print(readLastNLines(schedulerDevLogFile));
    }

    /**
     * Return the logsNbLines last lines of the given file.
     *
     * @param fileName the file to be displayed
     * @return the N last lines of the given file
     */
    private static String readLastNLines(String fileName) {
        StringBuilder toret = new StringBuilder();
        File f = new File(logsDirectory + File.separator + fileName);
        try {
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            long cursor = raf.length() - 2;
            int nbLines = logsNbLines;
            byte b;
            raf.seek(cursor);
            while (nbLines > 0) {
                if ((b = raf.readByte()) == '\n') {
                    nbLines--;
                }
                cursor--;
                raf.seek(cursor);
                if (nbLines > 0) {
                    toret.insert(0, (char) b);
                }
            }
        } catch (Exception e) {
        }
        return toret.toString();
    }

    public static void changePolicy(String newPolicyFullName) {
        getModel().changePolicy_(newPolicyFullName);
    }

    @SuppressWarnings("unchecked")
    private void changePolicy_(String newPolicyFullName) {
        try {
            Class<? extends Policy> klass = (Class<? extends Policy>) Class.forName(newPolicyFullName);
            ((AdminSchedulerInterface) scheduler).changePolicy(klass);
        } catch (Exception e) {
            handleExceptionDisplay("*ERROR*", e);
        }
    }

    public static AdminSchedulerInterface getAdminScheduler() {
        return getModel().getAdminScheduler_();
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
