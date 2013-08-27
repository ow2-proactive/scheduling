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
