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
package functionaltests.workflow;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


/**
 * Tests the correctness of dataspace workflow jobs
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class JobWorkflowDataspace extends JavaExecutable {

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {

        DataSpacesFileObject dsf = getLocalFile(System.getProperty("pas.task.iteration") + "_" +
            System.getProperty("pas.task.replication") + ".in");

        InputStream in = dsf.getContent().getInputStream();

        int i = -1;
        String line = "";
        while ((i = in.read()) != -1) {
            line += (char) i;
        }

        line = line.toUpperCase();

        getLocalFile(
                System.getProperty("pas.task.iteration") + "_" + System.getProperty("pas.task.replication") +
                    ".out").createFile();
        DataSpacesFileObject dsfOut = getLocalFile(System.getProperty("pas.task.iteration") + "_" +
            System.getProperty("pas.task.replication") + ".out");
        OutputStream out = dsfOut.getContent().getOutputStream();
        out.write(line.getBytes());
        out.close();

        return null;
    }
}
