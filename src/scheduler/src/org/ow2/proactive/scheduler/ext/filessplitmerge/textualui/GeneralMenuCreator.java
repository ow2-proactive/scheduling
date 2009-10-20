//============================================================================
// Name        : ProActive Files Split-Merge Framework
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

package org.ow2.proactive.scheduler.ext.filessplitmerge.textualui;

import org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands.Command;
import org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands.ExitCmd;
import org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands.ListAllJobsCmd;
import org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands.ListFinishedJobsCmd;
import org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands.ListPendingJobsCmd;
import org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands.ListRunningJobsCmd;
import org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands.StatsCommand;


/**
 * This class provides a menu creator for the Textual UI
 * Override this class to implement your own Menu Creator 
 * 
 * @author esalagea
 *
 */
public class GeneralMenuCreator {

    private TextualMenu mainMenu;

    /**
     * Creates the main textual menu
     * 
     * @return
     */
    public TextualMenu getMainMenu() {

        if (mainMenu == null) {
            mainMenu = new TextualMenu("Main Menu");

            Command stats = new StatsCommand("ds", "Display statistics");
            Command listPendingJobs = new ListPendingJobsCmd("lp", "list pending jobs");
            Command listRunningJobs = new ListRunningJobsCmd("lr", "list runnning jobs");
            Command listFinishedJobs = new ListFinishedJobsCmd("lf", "list finished jobs");
            Command listAllJobsCmd = new ListAllJobsCmd("la", "list all jobs");

            Command exit = new ExitCmd("exit", "Quit the program");

            mainMenu.addCommand(stats);
            mainMenu.addCommand(listAllJobsCmd);
            mainMenu.addCommand(listRunningJobs);
            mainMenu.addCommand(listFinishedJobs);
            mainMenu.addCommand(listPendingJobs);
            mainMenu.addCommand(exit);

        }

        return mainMenu;
    }

}
