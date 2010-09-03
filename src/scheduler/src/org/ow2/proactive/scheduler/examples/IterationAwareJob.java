/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 *              Nice-Sophia Antipolis/ActiveEon
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.examples;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


/**
 * Prints iteration and replication exported properties for testing purposes
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 * 
 */
public class IterationAwareJob extends JavaExecutable {

    private String report = "";

    @Override
    public void init(Map<String, Serializable> args) {
        for (Entry<String, Serializable> entry : args.entrySet()) {
            report += "arg " + entry.getKey() + " " + entry.getValue() + ":";
        }
    }

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        report += "prop it " + System.getProperty("pas.task.iteration") + ":";
        report += "prop dup " + System.getProperty("pas.task.replication") + ":";

        return report;
    }

}
