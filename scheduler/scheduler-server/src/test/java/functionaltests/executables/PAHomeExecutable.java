/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.executables;

import java.io.File;
import java.io.Serializable;

import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * PAHomeExecutable
 *
 * @author The ProActive Team
 **/
public class PAHomeExecutable extends JavaExecutable {

    private String expectedHome;

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        File expectedFile = new File(expectedHome).getCanonicalFile();
        String prop = System.getProperty("proactive.home");
        if (!expectedFile.equals(new File(prop).getCanonicalFile())) {
            throw new IllegalStateException("Unexpected proactive.home value, expected " + expectedHome +
                " received " + prop);
        }
        prop = System.getProperty(PAResourceManagerProperties.RM_HOME.getKey());
        if (!expectedFile.equals(new File(prop).getCanonicalFile())) {
            throw new IllegalStateException("Unexpected pa.rm.home value, expected " + expectedHome +
                " received " + prop);
        }
        prop = System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey());
        if (!expectedFile.equals(new File(prop).getCanonicalFile())) {
            throw new IllegalStateException("Unexpected pa.scheduler.home value, expected " + expectedHome +
                " received " + prop);
        }
        return true;
    }

}
