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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.examples;

import java.io.Serializable;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


/**
 * WaitAndPrint is a task that will wait and print something.<br/>
 * Also use for test.
 *
 * @author The ProActive Team
 *
 */
public class WaitAndPrint extends JavaExecutable {

    /** Sleeping time before displaying. */
    public int sleepTime;
    /** Parameter number. */
    public int number;

    /**
     * @see org.ow2.proactive.scheduler.common.task.executable.Executable#execute(org.ow2.proactive.scheduler.common.task.TaskResult[])
     */
    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        String message = null;

        try {
            System.err.println("Task " + number + " : Test STDERR");
            System.out.println("Task " + number + " : Test STDOUT");

            message = "Task " + number;
            int st = 0;
            while(st < sleepTime){
		Thread.sleep(1000);
		try {
			setProgress((st++)*100/sleepTime);
		} catch (IllegalArgumentException iae){
			setProgress(100);
		}
            }

        } catch (Exception e) {
            message = "crashed";
            e.printStackTrace();
        }

        System.out.println("Terminate task number " + number);

        return ("No." + this.number + " hi from " + message + "\t slept for " + sleepTime + " Seconds");
    }

}
