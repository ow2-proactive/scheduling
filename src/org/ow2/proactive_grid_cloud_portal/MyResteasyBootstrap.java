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
package org.ow2.proactive_grid_cloud_portal;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletContextEvent;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.config.xml.ProActiveConfigurationParser;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive_grid_cloud_portal.exceptions.NotConnectedExceptionMapper;


public class MyResteasyBootstrap extends ResteasyBootstrap {

    private Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.PREFIX + ".rest.webapps");
    private SchedulerSessionsCleaner schedulerSessionCleaner;
    private RMSessionsCleaner rmSessionCleaner;

    public void contextInitialized(ServletContextEvent event) {

        super.contextInitialized(event);

        ResteasyProviderFactory dispatcher = ResteasyProviderFactory.getInstance();
        //        RuntimeDelegate.setInstance(dispatcher);
        //        RegisterBuiltin.register(dispatcher);

        //        ResteasyProviderFactory dispatcher = new ResteasyProviderFactory();

        //        ResteasyProviderFactory.getInstance().addContextResolver(provider)
        dispatcher.addStringConverter(RestartModeConverter.class);
        dispatcher.addStringConverter(IntWrapperConverter.class);
        dispatcher.registerProvider(PersistentMapConverter.class);
        dispatcher.registerProvider(NotConnectedExceptionMapper.class);
        dispatcher.registerProvider(JacksonProvider.class);
        dispatcher.addStringConverter(UpdatablePropertiesConverter.class);

        try {
            PortalConfiguration.load(new File(event.getServletContext().getRealPath(
                    "WEB-INF/portal.properties")));
        } catch (Exception e) {
            throw new IllegalStateException("configuration file ('WEB-INF/portal.properties') not found", e);
        }

        // configure the loggers
        PropertyConfigurator.configure(event.getServletContext().getRealPath("WEB-INF/log4j.properties"));

        File paproperties = new File(event.getServletContext().getRealPath(
                "WEB-INF/ProActiveConfiguration.xml"));
        if (paproperties.exists()) {
            Properties p = new Properties();

            p = ProActiveConfigurationParser.parse(paproperties.getAbsolutePath(), p);

            Iterator<Entry<Object, Object>> i = p.entrySet().iterator();
            while (i.hasNext()) {
                Entry<Object, Object> tmp = i.next();
                ProActiveConfiguration.getInstance().setProperty("" + tmp.getKey(), "" + tmp.getValue(),
                        false);
            }
        }
        
        
        SchedulerStateCaching.init();

        // start the session cleaner
        schedulerSessionCleaner = new SchedulerSessionsCleaner(SchedulerSessionMapper.getInstance());
        Thread scheduler = new Thread(this.schedulerSessionCleaner, "Scheduler Sessions Cleaner Thread");
        scheduler.setDaemon(true);
        scheduler.start();
        rmSessionCleaner = new RMSessionsCleaner(RMSessionMapper.getInstance());
        Thread rm = new Thread(this.rmSessionCleaner, "RM Sessions Cleaner Thread");
        rm.setDaemon(true);
        rm.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {

        System.out.println("shutting down ProActive Rest API at " +
            event.getServletContext().getContextPath());

        // happily terminate sessions

        String[] sessionids = SchedulerSessionMapper.getInstance().getSessionsMap().keySet()
                .toArray(new String[] {});
        int i = 0;
        for (; i < sessionids.length; i++) {
            Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionids[i]);
            try {
                s.disconnect();
            } catch (NotConnectedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (PermissionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                SchedulerSessionMapper.getInstance().remove(sessionids[i]);
                System.out.println("Scheduler session id " + sessionids[i] + "terminated");
            }
        }

        sessionids = RMSessionMapper.getInstance().getSessionsMap().keySet()
        .toArray(new String[] {});
        for (; i < sessionids.length; i++) {
            RMCachingProxyInterface s = RMSessionMapper.getInstance().getSessionsMap().get(sessionids[i]);
            try {
                s.disconnect();
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                RMSessionMapper.getInstance().remove(sessionids[i]);
                System.out.println("RM session id " + sessionids[i] + "terminated");
            }
        }
        
        
        schedulerSessionCleaner.stop();
        rmSessionCleaner.stop();

        SchedulerStateCaching.setKill(true);

        // force the shutdown of the runtime
        ProActiveRuntimeImpl.getProActiveRuntime().cleanJvmFromPA();

        super.contextDestroyed(event);
        System.out.println("ProActive Rest API shutdown sequence completed");

    }
}
