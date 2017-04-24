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
package org.ow2.proactive.wrapper;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.ow2.proactive.scheduler.util.WarWrapper;


@WebListener
public class WrapperContextListener implements ServletContextListener {

    private WarWrapper ws = WrapperSingleton.getInstance();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        BootstrapLogger.getLogger().info("ProActive is starting up!");

        try {

            ws.launchProactiveServer();

        } catch (Exception e) {
            BootstrapLogger.getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        try {

            ws.shutdownProactive();

        } catch (Exception e) {
            BootstrapLogger.getLogger().error(e.getMessage(), e);
        }

        BootstrapLogger.getLogger().info("ProActive is stopped!");

    }
}
