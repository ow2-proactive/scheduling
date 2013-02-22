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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.webapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.config.xml.ProActiveConfigurationParser;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.ow2.proactive.resourcemanager.common.util.RMCachingProxyUserInterface;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive_grid_cloud_portal.rm.RMSessionMapper;
import org.ow2.proactive_grid_cloud_portal.rm.RMSessionsCleaner;
import org.ow2.proactive_grid_cloud_portal.rm.RMStateCaching;
import org.ow2.proactive_grid_cloud_portal.scheduler.IntWrapperConverter;
import org.ow2.proactive_grid_cloud_portal.scheduler.RestartModeConverter;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerSessionMapper;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerSessionsCleaner;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerStateListener;


public class RestRuntime {

    private SchedulerSessionsCleaner schedulerSessionCleaner;

    private RMSessionsCleaner rmSessionCleaner;

    public void start(ResteasyProviderFactory dispatcher, File configurationFile, File log4jConfig,
            File paConfig) {
        dispatcher.addStringConverter(RestartModeConverter.class);
        dispatcher.addStringConverter(IntWrapperConverter.class);
        dispatcher.registerProvider(JacksonProvider.class);

        try {
            PortalConfiguration.load(configurationFile);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid configuration file", e);
        }

        // configure the loggers
        if (log4jConfig != null) {
            try {
                InputStream in = new FileInputStream(log4jConfig);
                Properties p = new Properties();
                p.load(in);
                in.close();
                PropertyConfigurator.configure(p);
            } catch (Exception e1) {
                System.err
                        .println("Failed to read the portal's log4j file: " + log4jConfig.getAbsolutePath());
            }
        }

        if (paConfig != null && paConfig.exists()) {
            Properties p = new Properties();

            p = ProActiveConfigurationParser.parse(paConfig.getAbsolutePath(), p);

            Iterator<Entry<Object, Object>> i = p.entrySet().iterator();
            while (i.hasNext()) {
                Entry<Object, Object> tmp = i.next();
                ProActiveConfiguration.getInstance().setProperty("" + tmp.getKey(), "" + tmp.getValue(),
                        false);
            }
        }

        System.setProperty("scheduler.database.nodb", "true");

        SchedulerStateListener.getInstance().start();

        RMStateCaching.init();

        // start the scheduler session cleaner
        schedulerSessionCleaner = new SchedulerSessionsCleaner(SchedulerSessionMapper.getInstance());
        Thread scheduler = new Thread(this.schedulerSessionCleaner, "Scheduler Sessions Cleaner Thread");
        scheduler.setDaemon(true);
        scheduler.start();

        // start the rm session cleaner thread
        rmSessionCleaner = new RMSessionsCleaner(RMSessionMapper.getInstance());
        Thread rm = new Thread(this.rmSessionCleaner, "RM Sessions Cleaner Thread");
        rm.setDaemon(true);
        rm.start();
    }

    public void stop() {
        // happily terminate sessions

        String[] sessionids = SchedulerSessionMapper.getInstance().getSessionsMap().keySet()
                .toArray(new String[] {});
        int i = 0;
        for (; i < sessionids.length; i++) {
            Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionids[i])
                    .getScheduler();
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

        sessionids = RMSessionMapper.getInstance().getSessionsMap().keySet().toArray(new String[] {});
        for (; i < sessionids.length; i++) {
            RMCachingProxyUserInterface s = RMSessionMapper.getInstance().getSessionsMap().get(sessionids[i]);
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

        SchedulerStateListener.getInstance().kill();
        RMStateCaching.kill();

        // force the shutdown of the runtime
        ProActiveRuntimeImpl.getProActiveRuntime().cleanJvmFromPA();
    }

}
