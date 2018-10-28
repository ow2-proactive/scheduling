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
package functionaltests.utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.tests.ProActiveTest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;


/**
 * Locally launches Resource Manager. 
 *
 */
public class RMStarterForFunctionalTest {

    private static final String IAM_LOGIN_METHOD = "IAMLoginMethod";

    /**
     * Start a Resource Manager.
     * <p/>
     * Must be called with one parameter: path to a RM Properties file.
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("RMTStarter must be started with one parameter: path to a RM Properties file");
        }

        startIAMIfNeeded();

        String RMPropPath = args[0];
        PAResourceManagerProperties.updateProperties(RMPropPath);

        //Starting a local RM
        RMFactory.setOsJavaProperty();
        RMFactory.startLocal();

        // waiting the initialization
        RMConnection.waitAndJoin(null);

        System.out.println("Resource Manager successfully created !");
    }

    /**
     * Start IAM microservice if it is required for authentication
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws ConfigurationException
     */
    public static void startIAMIfNeeded() throws IOException, InterruptedException, ExecutionException,
            ConfigurationException, GeneralSecurityException {

        //Check if PA is configured to use IAM microservice for authentication
        if (PAResourceManagerProperties.RM_LOGIN_METHOD.getValueAsString().equals(IAM_LOGIN_METHOD)) {

            String proactiveHome = CentralPAPropertyRepository.PA_HOME.getValue();
            String bootMicroservicesPath = PASchedulerProperties.getAbsolutePath(PASchedulerProperties.SCHEDULER_BOOT_MICROSERVICES_PATH.getValueAsString());
            String bootConfigurationPath = PASchedulerProperties.getAbsolutePath(PASchedulerProperties.SCHEDULER_BOOT_CONFIGURATION_PATH.getValueAsString());

            IAMTHelper.startIAM(proactiveHome, bootMicroservicesPath, bootConfigurationPath);
        }
    }
}
