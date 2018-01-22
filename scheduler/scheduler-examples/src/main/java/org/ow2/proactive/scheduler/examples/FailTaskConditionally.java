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
 * Throw RuntimeException if replicationId argument is 1, otherwise sleep for 30
 * seconds and exit normally.
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 3.1
 */
public class FailTaskConditionally extends JavaExecutable {

    public static final String EXCEPTION_MESSAGE = "Faulty task exception";

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        getOut().println("it=" + getVariables());

        if (getReplicationIndex() == 1) {
            try {
                getOut().println("I will throw a runtime exception in 3 sec");
                Thread.sleep(3000);
            } finally {
                throw new RuntimeException(EXCEPTION_MESSAGE);
            }

        } else {
            getOut().println("I will sleep for 60 seconds");
            Thread.sleep(60000);
            return "Nothing";
        }
    }

}
