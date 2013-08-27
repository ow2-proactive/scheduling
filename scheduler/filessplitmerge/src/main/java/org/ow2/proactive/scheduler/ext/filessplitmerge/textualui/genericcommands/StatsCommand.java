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
