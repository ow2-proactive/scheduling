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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.login.LoginException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.config.xml.ProActiveConfigurationParser;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.common.util.RMCachingProxyUserInterface;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.ExceptionToJson;
import org.ow2.proactive_grid_cloud_portal.rm.RMSessionMapper;
import org.ow2.proactive_grid_cloud_portal.rm.RMSessionsCleaner;
import org.ow2.proactive_grid_cloud_portal.rm.RMStateCaching;
import org.ow2.proactive_grid_cloud_portal.scheduler.IntWrapperConverter;
import org.ow2.proactive_grid_cloud_portal.scheduler.RestartModeConverter;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerSessionMapper;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerSessionsCleaner;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerStateListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobCreationRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SubmissionClosedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.resteasy.spi.ResteasyProviderFactory;


public class RestRuntime {

    private static final Logger LOGGER = ProActiveLogger.getLogger(RestRuntime.class);

    private SchedulerSessionsCleaner schedulerSessionCleaner;
    private RMSessionsCleaner rmSessionCleaner;

    private static Map<Class, Integer> EXCEPTION_MAPPINGS = new HashMap<Class, Integer>();
    static {
        EXCEPTION_MAPPINGS.put(IOException.class, HttpURLConnection.HTTP_NOT_FOUND);
        EXCEPTION_MAPPINGS.put(JobAlreadyFinishedException.class, HttpURLConnection.HTTP_NOT_FOUND);
        EXCEPTION_MAPPINGS.put(JobCreationRestException.class, HttpURLConnection.HTTP_NOT_FOUND);
        EXCEPTION_MAPPINGS.put(KeyException.class, HttpURLConnection.HTTP_NOT_FOUND);
        EXCEPTION_MAPPINGS.put(LoginException.class, HttpURLConnection.HTTP_NOT_FOUND);
        EXCEPTION_MAPPINGS.put(NotConnectedRestException.class, HttpURLConnection.HTTP_UNAUTHORIZED);
        EXCEPTION_MAPPINGS.put(NotConnectedException.class, HttpURLConnection.HTTP_UNAUTHORIZED);
        EXCEPTION_MAPPINGS.put(PermissionRestException.class, HttpURLConnection.HTTP_FORBIDDEN);
        EXCEPTION_MAPPINGS.put(SchedulerRestException.class, HttpURLConnection.HTTP_NOT_FOUND);
        EXCEPTION_MAPPINGS.put(NotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        EXCEPTION_MAPPINGS.put(SubmissionClosedRestException.class, HttpURLConnection.HTTP_NOT_FOUND);
        EXCEPTION_MAPPINGS.put(UnknownJobRestException.class, HttpURLConnection.HTTP_NOT_FOUND);
        EXCEPTION_MAPPINGS.put(UnknownTaskException.class, HttpURLConnection.HTTP_NOT_FOUND);
        EXCEPTION_MAPPINGS.put(PermissionException.class, HttpURLConnection.HTTP_FORBIDDEN);
        EXCEPTION_MAPPINGS.put(ProActiveRuntimeException.class, HttpURLConnection.HTTP_NOT_FOUND);
        EXCEPTION_MAPPINGS.put(RuntimeException.class, HttpURLConnection.HTTP_INTERNAL_ERROR);
        EXCEPTION_MAPPINGS.put(Throwable.class, HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    public void start(ResteasyProviderFactory dispatcher, File configurationFile, File log4jConfig,
            File paConfig) {

        addExceptionMappers(dispatcher);

        dispatcher.addStringConverter(RestartModeConverter.class);
        dispatcher.addStringConverter(IntWrapperConverter.class);
        dispatcher.registerProvider(JacksonProvider.class);

        loadProperties(configurationFile);

        // configure the loggers
        if (log4jConfig != null) {
            try {
                InputStream in = new FileInputStream(log4jConfig);
                Properties p = new Properties();
                p.load(in);
                in.close();
                PropertyConfigurator.configure(p);
            } catch (Exception e1) {
                LOGGER.error("Failed to read the portal's log4j file: " + log4jConfig.getAbsolutePath(), e1);
            }
        }

        if (paConfig != null && paConfig.exists()) {
            Properties p = new Properties();

            p = ProActiveConfigurationParser.parse(paConfig.getAbsolutePath(), p);

            for (Entry<Object, Object> tmp : p.entrySet()) {
                ProActiveConfiguration.getInstance().setProperty(tmp.getKey().toString(),
                        tmp.getValue().toString(), false);
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

    private void loadProperties(File configurationFile) {
        FileInputStream propertyFile = null;
        try {
            propertyFile = new FileInputStream(configurationFile);
            PortalConfiguration.load(propertyFile);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid configuration file", e);
        } finally {
            if (propertyFile != null) {
                try {
                    propertyFile.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close portal configuration file", e);
                }
            }
        }
    }

    void addExceptionMappers(ResteasyProviderFactory dispatcher) {
        for (final Entry<Class, Integer> exceptionMapping : EXCEPTION_MAPPINGS.entrySet()) {
            dispatcher.addExceptionMapper(new ExceptionMapper<Throwable>() {
                @Override
                public Response toResponse(Throwable throwable) {
                    ExceptionToJson js = new ExceptionToJson();
                    js.setErrorMessage(throwable.getMessage());
                    js.setHttpErrorCode(exceptionMapping.getValue());
                    js.setStackTrace(ProActiveLogger.getStackTraceAsString(throwable));
                    js.setException(throwable);
                    js.setExceptionClass(throwable.getClass().getName());
                    return Response.status(exceptionMapping.getValue())
                      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                      .entity(js).build();
                }
            }, (Type) exceptionMapping.getKey());
        }
    }

    public void stop() {
        // happily terminate sessions
        terminateSchedulerSessions();
        terminateRmSessions();

        schedulerSessionCleaner.stop();
        rmSessionCleaner.stop();

        SchedulerStateListener.getInstance().kill();
        RMStateCaching.kill();

        // force the shutdown of the runtime
        ProActiveRuntimeImpl.getProActiveRuntime().cleanJvmFromPA();
    }

    private void terminateRmSessions() {
        Set<String> schedulerSessionIds = RMSessionMapper.getInstance().getSessionsMap().keySet();
        List<String> sessionids = new ArrayList<String>(schedulerSessionIds);
        for (String sessionid : sessionids) {
            RMCachingProxyUserInterface s = RMSessionMapper.getInstance().getSessionsMap().get(sessionid);
            try {
                s.disconnect();
            } catch (Throwable e) {
                LOGGER.warn(e);
            } finally {
                RMSessionMapper.getInstance().remove(sessionid);
                LOGGER.debug("RM session id " + sessionid + "terminated");
            }
        }
    }

    private void terminateSchedulerSessions() {
        Set<String> schedulerSessionIds = SchedulerSessionMapper.getInstance().getSessionsMap().keySet();
        List<String> sessionids = new ArrayList<String>(schedulerSessionIds);
        for (String sessionid : sessionids) {
            Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionid).getScheduler();
            try {
                s.disconnect();
            } catch (Throwable e) {
                LOGGER.warn(e);
            } finally {
                SchedulerSessionMapper.getInstance().remove(sessionid);
                LOGGER.debug("Scheduler session id " + sessionid + "terminated");
            }
        }
    }

}
