/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
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

        hibernateRmConfiguration = Paths.get(schedulerHome, schedulerConfigurationFolderName, "rm",
                databaseFileName);
        hibernateSchedulerConfiguration = Paths.get(schedulerHome, schedulerConfigurationFolderName,
                "scheduler", databaseFileName);

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
