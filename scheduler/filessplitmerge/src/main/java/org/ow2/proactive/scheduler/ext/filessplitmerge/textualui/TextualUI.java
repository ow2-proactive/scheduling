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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.filessplitmerge.textualui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Observable;
import java.util.Observer;

import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.ext.filessplitmerge.JobPostTreatmentManagerHolder;
import org.ow2.proactive.scheduler.ext.filessplitmerge.event.EventType;
import org.ow2.proactive.scheduler.ext.filessplitmerge.event.InternalEvent;
import org.ow2.proactive.scheduler.ext.filessplitmerge.exceptions.NotInitializedException;
import org.ow2.proactive.scheduler.ext.filessplitmerge.logging.GenericLogger;
import org.ow2.proactive.scheduler.ext.filessplitmerge.logging.LoggerManager;
import org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands.CommandResult;


public class TextualUI implements Observer, GenericLogger {

    //	private GoldJobConfiguration goldJobConfig;
    //public static UserSchedulerInterface scheduler; 
    private static boolean displayException = false;
    private static TextualMenu currentMenu;
    public static boolean DISPLAY_MENU_AFTER_EACH_CMD = false;

    public TextualUI() {
        //  startCommandListener();
    }

    public void startCommandListener() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean stopCommunicator = false;

        try {
            currentMenu = MenuCreatorHoloder.getMenuCreator().getMainMenu();
        } catch (NotInitializedException e1) {
            LoggerManager.getLogger().error("Could not start Textual Interface", e1);
        }

        boolean displayMenu = true;
        while (!stopCommunicator) {
            if (displayMenu)
                output(currentMenu.list());

            try {
                String line = reader.readLine();
                if (line.equals("")) {
                    displayMenu = true;
                } else {
                    CommandResult cr = currentMenu.handleCommand(line);
                    if (cr == null)
                        displayMenu = true;
                    else {
                        output(cr.getOutput());
                        output("\n>");
                        if (cr.getTm() != null) {
                            currentMenu = cr.getTm();
                            displayMenu = true;
                        } else {
                            displayMenu = TextualUI.DISPLAY_MENU_AFTER_EACH_CMD;
                        }
                    }//else (if cr==null)	   

                }//else (if line eq "")   

            } catch (Exception e) {
                error("Error !!\n", e);
            }
        }
    }

    /**
     * 
     * @param msg the message to print
     * @param showRedesplayTip if this is false, the message "(Press Enter to redisplay the current menu)" will not be displayed 
     */
    public static void info(String msg, boolean showRedesplayTip) {

        if (showRedesplayTip) {
            msg += " (Press Enter to redisplay the current menu)";
        }
        System.out.println("INFO: " + msg);

    }

    public void info(String msg) {
        TextualUI.info(msg, true);
    }

    public void warning(String msg) {
        System.out.println("WARNING: " + msg + " (Press Enter to redisplay the current menu)");
    }

    public void warning(String message, Exception e) {
        warning(message);
        if (displayException) {
            e.printStackTrace();
            System.out.println();
        }

    }

    private static void output(String message) {
        System.out.print(message);
    }

    public void error(String message) {
        System.err.println("ERROR: " + message + " ");
    }

    public void error(String message, Exception e) {
        error(message);
        if (displayException) {
            e.printStackTrace();
            System.out.println();
        } else {
            error(e.getMessage());
        }
    }

    //@Override
    public void update(Observable o, Object arg) {
        if (!(arg instanceof InternalEvent)) {
            this.info("Unknown notification received: " + arg.toString());
            return;
        }
        InternalEvent ge = (InternalEvent) arg;
        JobState job = ge.getJob();

        final EventType type = ge.getType();

        switch (type) {
            case jobSubmitted:
                //are we intrested in this job:
                try {
                    if (JobPostTreatmentManagerHolder.getPostTreatmentManager().isAwaitedJob(
                            job.getId().value())) {
                        this.info("Job have been submited to the scheduler: " + job.getName() + " (id=" +
                            job.getId() + ")");
                    }
                } catch (NotInitializedException e) {
                    LoggerManager.getLogger().error(e);
                }
                break;
            case jobRunningToFinishedEvent:
                //				Nothing will be written here as the notification will be 
                //				sent by the GoldJobPostTreatmentManager after the merge is done
                //				TextualUI.info("Job finished: "+job.getName());
                //				TextualUI.info("Job Description: "+job.getDescription());
                //				TextualUI.info("Please wait for post treatment");

                break;
            case jobKilledEvent:
                this.info("Job have been killed: " + job.getName());
                break;
            case jobChangePriorityEvent:
                this.info("Priority of job " + job.getName() + " id:" + job.getId() + " have changed to" +
                    job.getPriority());
                break;

            default:
                break;
        }
    }

}
