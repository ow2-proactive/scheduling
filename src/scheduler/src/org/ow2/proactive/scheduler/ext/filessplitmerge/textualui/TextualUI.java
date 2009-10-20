//============================================================================
// Name        : ProActive Files Split-Merge Framework
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

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
