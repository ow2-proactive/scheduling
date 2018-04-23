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
package functionaltests.nodesource;

import static functionaltests.utils.RMTHelper.log;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.CronPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;


/**
 * Test of the CronPolicy. Acquires the nodes of a LocalInfrastructure
 * immediately, then wait between one and two minutes before removing them,
 * and then wait one more minute to reacquire the nodes.
 *
 */
public class TestLocalInfrastructureCronPolicy extends RMFunctionalTest {

    private static final int NODES_NUMBER = 2;

    private static final String CRON_WITHOUT_MINUTES = " * * * *";

    private static final String EMPTY_NODE_SOURCE_NAME = "emptyNodeSource";

    private static final String NODE_SOURCE_NAME = "cronNodeSource";

    private static final int CRON_MARGIN_AFTER_FIRST_ACQUIRE = 2;

    private static final int CRON_MARGIN_AFTER_FIRST_RELEASE = 1;

    private byte[] credentials;

    private ResourceManager resourceManager;

    @Before
    public void setup() throws Exception {
        this.credentials = getCredentialsBytes();
        this.resourceManager = this.rmHelper.getResourceManager();
    }

    @Test
    public void action() throws Exception {

        log("Create an remove an empty local node source with cron policy");
        createEmptyNodeSource(EMPTY_NODE_SOURCE_NAME);
        removeNodeSource(EMPTY_NODE_SOURCE_NAME);

        log("Create a local node source with " + NODES_NUMBER + " nodes with cron policy");
        createNodeSource(NODE_SOURCE_NAME);

        log("Waiting for the cron policy to remove the nodes, in " + CRON_MARGIN_AFTER_FIRST_ACQUIRE +
            " minutes maximum");
        for (int i = 0; i < NODES_NUMBER; i++) {
            this.rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        log("Waiting for the cron policy to add the nodes again, in " + CRON_MARGIN_AFTER_FIRST_RELEASE +
            " minute maximum");
        for (int i = 0; i < NODES_NUMBER; i++) {
            this.rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }
    }

    private byte[] getCredentialsBytes() throws IOException {
        String credentialsPath = PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString());
        return FileToBytesConverter.convertFileToByteArray(new File(credentialsPath));
    }

    private void createEmptyNodeSource(String nodeSourceName) throws Exception {

        this.resourceManager.defineNodeSource(nodeSourceName,
                                              LocalInfrastructure.class.getName(),
                                              new Object[] { this.credentials, 0, RMTHelper.DEFAULT_NODES_TIMEOUT, "" },
                                              CronPolicy.class.getName(),
                                              getPolicyParams(),
                                              NODES_NOT_RECOVERABLE);
        this.resourceManager.deployNodeSource(nodeSourceName);
        this.rmHelper.waitForNodeSourceCreation(nodeSourceName);
    }

    private void createNodeSource(String nodeSourceName) throws Exception {

        this.resourceManager.defineNodeSource(nodeSourceName,
                                              LocalInfrastructure.class.getName(),
                                              new Object[] { this.credentials, NODES_NUMBER,
                                                             RMTHelper.DEFAULT_NODES_TIMEOUT, "" },
                                              CronPolicy.class.getName(),
                                              getPolicyParams(),
                                              NODES_NOT_RECOVERABLE);
        this.resourceManager.deployNodeSource(nodeSourceName);

        RMTHelper.waitForNodeSourceCreation(nodeSourceName, NODES_NUMBER, this.rmHelper.getMonitorsHandler());
    }

    private void removeNodeSource(String sourceName) throws Exception {
        this.rmHelper.getResourceManager().removeNodeSource(sourceName, true);
        this.rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, sourceName);
    }

    private Object[] getPolicyParams() {
        int cronReleaseMinute = LocalDateTime.now().getMinute() + CRON_MARGIN_AFTER_FIRST_ACQUIRE;
        int cronAcquireMinute = cronReleaseMinute + CRON_MARGIN_AFTER_FIRST_RELEASE;
        return new Object[] { "ME", "ALL", cronAcquireMinute + CRON_WITHOUT_MINUTES,
                              cronReleaseMinute + CRON_WITHOUT_MINUTES, "true", "true" };
    }

}
