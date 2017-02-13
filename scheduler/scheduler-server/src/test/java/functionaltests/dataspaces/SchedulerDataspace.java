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
package functionaltests.dataspaces;

import java.io.Serializable;
import java.util.Set;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.WrongApplicationIdException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.DataSpaceServiceStarter;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.TaskDataSpaceApplication;


/**
 * SchedulerDataspace depicts the dataspace configuration on server side.
 *
 * @author ActiveEon Team
 * @see TaskProActiveDataspacesIntegrationTest
 */
public class SchedulerDataspace implements EndActive, Serializable {

    private DataSpaceServiceStarter dataSpaceServiceStarter;

    private TaskDataSpaceApplication taskDataSpaceApplication;

    public SchedulerDataspace() {
    }

    public void init(JobId jobId, TaskId taskId, String user) throws Exception {
        dataSpaceServiceStarter = DataSpaceServiceStarter.getDataSpaceServiceStarter();
        dataSpaceServiceStarter.startNamingService();

        taskDataSpaceApplication = new TaskDataSpaceApplication(taskId.toString(),
                                                                dataSpaceServiceStarter.getNamingService());

        taskDataSpaceApplication.startDataSpaceApplication(null, null, null, null, user, jobId);
    }

    public String getNamingServiceUrl() {
        return dataSpaceServiceStarter.getNamingServiceURL();
    }

    @Override
    public void endActivity(Body body) {
        taskDataSpaceApplication.terminateDataSpaceApplication();

        Set<String> registeredApplications = dataSpaceServiceStarter.getNamingService().getRegisteredApplications();

        NamingService namingService = dataSpaceServiceStarter.getNamingService();
        for (String registeredApplication : registeredApplications) {
            try {
                namingService.unregisterApplication(registeredApplication);
            } catch (WrongApplicationIdException e) {
                throw new IllegalStateException(e);
            }
        }

        dataSpaceServiceStarter.terminateNamingService();
        dataSpaceServiceStarter.clearSpaceConfigurations();
    }

    public static void main(String[] args) throws ActiveObjectCreationException, NodeException {
        PASchedulerProperties.SCHEDULER_HOME.updateProperty(args[0]);

        SchedulerDataspace schedulerDataspace = PAActiveObject.newActive(SchedulerDataspace.class, new Object[0]);

        System.out.println(PAActiveObject.getUrl(schedulerDataspace));
    }

}
