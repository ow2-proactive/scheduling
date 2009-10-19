//============================================================================
// Name        : ProActive Embarrassingly Parallel Framework 
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

package org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands;

import java.util.List;


public class StatsCommand extends Command {

    public StatsCommand(String cmd, String description) {
        super(cmd, description);
        // TODO Auto-generated constructor stub
    }

    @Override
    public CommandResult execute(List params) {
        //		 UserSchedulerInterface scheduler = TextualUI.scheduler; 
        //         String out = "";
        //		 try {
        //	            HashMap<String, Object> stat = scheduler.getStats().getProperties();
        //
        //	            for (Entry<String, Object> e : stat.entrySet()) {
        //	                out += (e.getKey() + " : " + e.getValue() + "\n");
        //	            }
        //	            
        //	        } catch (SchedulerException e) {
        //	            e.printStackTrace();
        //	        }
        //	
        String out = "This command is not implemented.";
        CommandResult cr = new CommandResult(out);
        return cr;

    }

    @Override
    public CommandResult execute() {
        return execute(null);
    }

}
