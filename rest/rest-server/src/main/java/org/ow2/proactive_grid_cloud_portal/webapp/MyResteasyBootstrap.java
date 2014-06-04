/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.webapp;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.resteasy.spi.ResteasyProviderFactory;


public class MyResteasyBootstrap extends ResteasyBootstrap {

    private static final Logger LOGGER = ProActiveLogger.getLogger(MyResteasyBootstrap.class);

    private RestRuntime restRuntime;

    public void contextInitialized(ServletContextEvent event) {
        super.contextInitialized(event);

        ResteasyProviderFactory dispatcher = ResteasyProviderFactory.getInstance();
        
        dispatcher.registerProvider(OctetStreamWriter.class, false);
        dispatcher.registerProvider(PlainTextReader.class, false);

        restRuntime = new RestRuntime();

        restRuntime.start(dispatcher, findConfigurationFile(event.getServletContext(), File.separator +
            "config" + File.separator + "rest" + File.separator + "settings.ini"), findConfigurationFile(
                event.getServletContext(), "log4j.properties"), findConfigurationFile(event
                .getServletContext(), File.separator + "config" + File.separator + "proactive" +
            File.separator + "ProActiveConfiguration.xml"));
    }

    private File findConfigurationFile(ServletContext servletContext, String configurationFileName) {
        File configurationFile = findConfigurationFileInWebInf(servletContext, configurationFileName);
        if (configurationFile == null || !configurationFile.exists()) {
            return new File(PASchedulerProperties.SCHEDULER_HOME.getValueAsString() + configurationFileName);
        }
        return configurationFile;
    }

    private File findConfigurationFileInWebInf(ServletContext servletContext, String configurationFileName) {
        String pathToConfigurationFile = servletContext.getRealPath("WEB-INF" + configurationFileName);
        if (pathToConfigurationFile == null) {
            return null;
        }
        return new File(pathToConfigurationFile);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        LOGGER.info("Shutting down ProActive Rest API at " + event.getServletContext().getContextPath());

        restRuntime.stop();

        super.contextDestroyed(event);
        LOGGER.info("ProActive Rest API shutdown sequence completed");

    }
}
