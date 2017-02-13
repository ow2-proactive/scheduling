/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.examples;

import java.io.Serializable;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


/**
 * WaitAndPrint is a task that will wait and print something.
 *
 * @author The ProActive Team
 */
public class WaitAndPrint extends JavaExecutable {

    /** Sleeping time before displaying. */
    public int sleepTime;

    /** Parameter number. */
    public int number;

    /**
     * @see JavaExecutable#execute(TaskResult[])
     */
    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        String message = null;

        try {
            getErr().println("Task " + number + " : Test STDERR");
            getOut().println("Task " + number + " : Test STDOUT");

            message = "Task " + number;
            int st = 0;
            while (st < sleepTime) {
                Thread.sleep(1000);
                try {
                    setProgress((st++) * 100 / sleepTime);
                } catch (IllegalArgumentException iae) {
                    setProgress(100);
                }
            }

        } catch (Exception e) {
            message = "crashed";
            e.printStackTrace(getErr());
        }

        getOut().println("Terminate task number " + number);

        return ("No." + this.number + " hi from " + message + "\t slept for " + sleepTime + " Seconds");
    }

}
