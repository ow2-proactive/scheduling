/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core;

/**
 * RecoveringThread is used to display percentage of job recovered at startup.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class RecoveringThread extends Thread implements RecoverCallback {

    /**  */
    private static final long serialVersionUID = 200;
    private int jobsToRecover = 1;
    private int jobsRecovered = 0;

    @Override
    public void run() {
        display("Found " + jobsToRecover + " jobs to retrieve, please wait...     ", false);
        while (!isInterrupted() && jobsRecovered < jobsToRecover) {
            try {
                display(threeChar(100 * jobsRecovered / jobsToRecover), true);
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        display("100%\n", true);
    }

    public void init(int nb) {
        this.jobsToRecover = nb;
        if (nb > 0) {
            this.start();
        }
    }

    public void jobRecovered() {
        this.jobsRecovered++;
    }

    private String threeChar(int i) {
        if (i < 10) {
            return "  " + i + "%";
        }
        if (i < 100) {
            return " " + i + "%";
        }
        return i + "%";
    }

    private void display(String msg, boolean backspaces) {
        if (backspaces) {
            //remove 4 chars on sysout
            System.out.print("\010\010\010\010");
        }
        System.out.print(msg);
    }
}
