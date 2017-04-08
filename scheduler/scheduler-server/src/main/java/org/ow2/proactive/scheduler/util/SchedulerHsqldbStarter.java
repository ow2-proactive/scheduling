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
package org.ow2.proactive.scheduler.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;

import com.google.common.annotations.VisibleForTesting;


/**
 * Manage the startup of an HSQLDB server configured for ProActive Workflows and Scheduling.
 *
 * @author ActiveEon Team
 * @see HsqldbServer
 */
public class SchedulerHsqldbStarter {

    private final String schedulerHome = PASchedulerProperties.SCHEDULER_HOME.getValueAsString();

    private final HsqldbServer hsqldbServer;

    private Path hibernateRmConfiguration;

    private Path hibernateSchedulerConfiguration;

    public SchedulerHsqldbStarter() throws IOException {
        this.hsqldbServer = createHsqldbServer();
        configureCatalogs(hsqldbServer, schedulerHome);
    }

    @VisibleForTesting
    SchedulerHsqldbStarter(HsqldbServer hsqldbServer) throws IOException {
        this.hsqldbServer = hsqldbServer;
        configureCatalogs(hsqldbServer, schedulerHome);
    }

    private HsqldbServer createHsqldbServer() throws IOException {
        return new HsqldbServer(Paths.get(schedulerHome, "config", "hsqldb-server.properties"));
    }

    @VisibleForTesting
    void configureCatalogs(HsqldbServer server, String schedulerHome) throws IOException {
        String databaseFileName = "database.properties";
        String schedulerConfigurationFolderName = "config";

        Path schedulerDbPath = Paths.get(schedulerHome, "data", "db");

        hibernateRmConfiguration = Paths.get(schedulerHome, schedulerConfigurationFolderName, "rm", databaseFileName);
        hibernateSchedulerConfiguration = Paths.get(schedulerHome,
                                                    schedulerConfigurationFolderName,
                                                    "scheduler",
                                                    databaseFileName);

        server.addCatalog(schedulerDbPath, hibernateRmConfiguration);
        server.addCatalog(schedulerDbPath, hibernateSchedulerConfiguration);
    }

    public boolean isServerModeRequired() throws IOException {
        return HsqldbServer.isServerModeRequired(hibernateRmConfiguration, hibernateSchedulerConfiguration);
    }

    public void startIfNeeded() throws IOException {
        if (isServerModeRequired()) {
            start();
        }
    }

    @VisibleForTesting
    void start() {
        hsqldbServer.startAsync();
        hsqldbServer.awaitRunning();
    }

    public void stop() {
        if (isRunning()) {
            stopImmediately();
        }
    }

    @VisibleForTesting
    boolean isRunning() {
        return hsqldbServer.isRunning();
    }

    @VisibleForTesting
    void stopImmediately() {
        hsqldbServer.stopAsync();
        hsqldbServer.awaitTerminated();
    }

}
