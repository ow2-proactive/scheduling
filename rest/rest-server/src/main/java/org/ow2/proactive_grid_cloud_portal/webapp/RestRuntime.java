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
package org.ow2.proactive_grid_cloud_portal.webapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.config.xml.ProActiveConfigurationParser;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive_grid_cloud_portal.common.SessionsCleaner;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStore;
import org.ow2.proactive_grid_cloud_portal.rm.RMStateCaching;
import org.ow2.proactive_grid_cloud_portal.scheduler.IntWrapperConverter;
import org.ow2.proactive_grid_cloud_portal.scheduler.RestartModeConverter;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerStateListener;


public class RestRuntime {

    private static final Logger LOGGER = ProActiveLogger.getLogger(RestRuntime.class);

    private SessionsCleaner sessionCleaner;

    private boolean needToKillProActiveRuntime;

    public void start(ResteasyProviderFactory dispatcher, File log4jConfig, File paConfig) {

        needToKillProActiveRuntime = !PALifeCycle.IsProActiveStarted();

        addExceptionMappers(dispatcher);

        dispatcher.registerProvider(RestartModeConverter.class);
        dispatcher.registerProvider(IntWrapperConverter.class);
        dispatcher.registerProvider(JacksonProvider.class);

        configureLogger(log4jConfig);

        if (paConfig != null && paConfig.exists()) {
            Properties p = new Properties();

            p = ProActiveConfigurationParser.parse(paConfig.getAbsolutePath(), p);

            for (Entry<Object, Object> tmp : p.entrySet()) {
                ProActiveConfiguration.getInstance().setProperty(tmp.getKey().toString(),
                                                                 tmp.getValue().toString(),
                                                                 false);
            }
        }

        System.setProperty("scheduler.database.nodb", "true");

        SchedulerStateListener.getInstance().start();

        RMStateCaching.init();

        // start the scheduler session cleaner
        sessionCleaner = new SessionsCleaner(SharedSessionStore.getInstance());
        Thread sessionCleanerThread = new Thread(this.sessionCleaner, "Sessions Cleaner Thread");
        sessionCleanerThread.setDaemon(true);
        sessionCleanerThread.start();
    }

    private void configureLogger(File log4jConfig) {
        if (loggerNotConfigured()) {
            if (log4jConfig != null) {
                try (InputStream in = new FileInputStream(log4jConfig)) {
                    Properties p = new Properties();
                    p.load(in);
                    System.setProperty("log4j.configuration", log4jConfig.getAbsolutePath()); // avoid reset by ProActiveLogger
                    PropertyConfigurator.configure(p);
                } catch (Exception e1) {
                    LOGGER.warn("Failed to read the portal's log4j file: " + log4jConfig.getAbsolutePath(), e1);
                }
            }
        }
    }

    private boolean loggerNotConfigured() {
        return System.getProperty(CentralPAPropertyRepository.LOG4J.getName()) == null;
    }

    void addExceptionMappers(ResteasyProviderFactory dispatcher) {
        dispatcher.registerProvider(ExceptionMappers.IOExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.JobAlreadyFinishedExceptionExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.JobCreationRestExceptionExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.KeyExceptionExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.LoginExceptionExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.NotConnectedRestExceptionExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.NotConnectedExceptionExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.PermissionRestExceptionExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.SchedulerRestExceptionExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.NotFoundExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.SubmissionClosedRestExceptionExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.UnknownJobRestExceptionExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.UnknownTaskExceptionExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.PermissionExceptionExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.ProActiveRuntimeExceptionExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.RuntimeExceptionExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.IllegalArgumentExceptionMapper.class);
        dispatcher.registerProvider(ExceptionMappers.ThrowableExceptionMapper.class);
    }

    public void stop() {
        // happily terminate sessions
        SharedSessionStore.getInstance().terminateAll();

        sessionCleaner.stop();

        SchedulerStateListener.getInstance().kill();

        if (needToKillProActiveRuntime) {
            // force the shutdown of the runtime
            ProActiveRuntimeImpl.getProActiveRuntime().cleanJvmFromPA();
        }
    }

}
