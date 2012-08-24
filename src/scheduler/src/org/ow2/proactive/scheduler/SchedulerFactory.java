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
package org.ow2.proactive.scheduler;

import java.io.File;
import java.net.URI;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.scheduler.authentication.SchedulerAuthentication;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.InternalSchedulerException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.core.SchedulerFrontend;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.exception.AdminSchedulerException;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.utils.FileUtils;
import org.ow2.proactive.utils.appenders.FileAppender;


/**
 * <b>Start here</b>, it provides method to create a new ProActive Scheduler and manage it.<br>
 * With this Class, you will be able to create a new scheduler with or without connecting yourself,
 * or create it with your administrator properties.<br>
 * A resources manager may have been launched before creating a new scheduler.
 *
 * Object which performs the Scheduler (RM)creation,
 * and provides Scheduler's front-end active objects.<BR>
 * {@link SchedulerAuthentication}.<BR>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
@PublicAPI
public class SchedulerFactory {

    /** Logger to be used for all messages related to the scheduler */
    public static final Logger logger = Logger.getLogger(SchedulerFactory.class);

    private static boolean allowNullInit = false;
    private static boolean schedulerStarted = false;

    /**
     * Try to join Resource Manager at given URI.
     *
     * @param uriRM the resource manager URL
     * @return a Resource Manager authentication interface if success.
     * @throws RMException if no RM could be joined at this URI.
     */
    public static RMAuthentication tryJoinRM(URI uriRM) throws RMException {
        return RMConnection.join(uriRM.toString());
    }

    /** Try to join Resource Manager at given URI with a timeout.
     * A timeout of zero means an infinite timeout. The connection will then
     * block until connection is established or an error occurs.
     * 
     * @param uriRM the resource manager URL
     * @param timeout timeout to establish connection (in ms)
     * @return a Resource Manager authentication interface if success.
     * @throws RMException if no RM could be joined at this URI.
     */
    public static RMAuthentication waitAndJoinRM(URI uriRM, long timeout) throws RMException {
        return RMConnection.waitAndJoin(uriRM.toString(), timeout);
    }

    /**
     * Try to join Resource Manager at given URI.
     * Waits undefinitely until connectino with RM is established or an error
     * occurs.
     * 
     * @param uriRM the resource manager URL
     * @return a Resource Manager authentication interface if success.
     * @throws RMException if no RM could be joined at this URI.
     */
    public static RMAuthentication waitAndJoinRM(URI uriRM) throws RMException {
        return RMConnection.waitAndJoin(uriRM.toString());
    }

    /**
     * Creates and starts a Scheduler on the local host using the given initializer to configure it.
     * Only one Scheduler can be started by JVM.
     *
     * @param rmURL the URL of a started Resource Manager
     * @param initializer Use to configure the Scheduler before starting it.
     * 		This parameter cannot be null, if you want to start the Scheduler using JVM properties
     * 		use the {@link #} to start the Scheduler without configuration
     *
     * @return a Scheduler authentication that allow you to administer it or get its connection URL.
     *
     * @throws ActiveObjectCreationException If Scheduler cannot be created
     */
    public static synchronized SchedulerAuthenticationInterface startLocal(URI rmURL,
            SchedulerInitializer initializer) throws InternalSchedulerException {
        if (!schedulerStarted) {
            if (!allowNullInit) {
                if (initializer != null) {
                    //configure application
                    configure(initializer);
                } else {
                    throw new IllegalArgumentException("Initializer cannot be null !");
                }
            }
            if (rmURL == null) {
                throw new IllegalArgumentException("RM url is null !");
            }
            try {
                tryJoinRM(rmURL);
                String policy = initializer.getPolicyFullClassName();
                //start scheduler
                createScheduler(rmURL, policy);
                SchedulerAuthenticationInterface sai = SchedulerConnection.waitAndJoin(null);
                schedulerStarted = true;
                return sai;
            } catch (Exception e) {
                throw new InternalSchedulerException(e);
            }
        } else {
            throw new InternalSchedulerException("Scheduler already localy running");
        }
    }

    /**
     * Configure the VM to be ready to start the new Scheduler.
     *
     * @param initializer the initializer used to configured the VM.
     */
    private static void configure(SchedulerInitializer initializer) {
        //security manager
        if (System.getProperty("java.security.manager") == null) {
            System.setProperty("java.security.manager", "");
        }
        //check policy
        String s = initializer.getPolicyFullClassName();
        if (s == null) {
            throw new IllegalArgumentException("Scheduler policy is not set, cannot start Scheduler !");
        }
        //scheduler properties
        s = initializer.getSchedulerPropertiesConfiguration();
        if (s == null) {
            throw new IllegalArgumentException(
                "Scheduler properties file is not set, cannot start Scheduler !");
        }
        System.setProperty(PASchedulerProperties.PA_SCHEDULER_PROPERTIES_FILEPATH, s);
        //pa conf
        s = initializer.getProActiveConfiguration();
        if (s != null) {
            System.setProperty(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName(), s);
        }
        //Scheduler home
        s = initializer.getSchedulerHomePath();
        if (s != null) {
            System.setProperty(PASchedulerProperties.SCHEDULER_HOME.getKey(), s);
        }
    }

    private static void configureLog4j() {
        // Log4j configuration for jobs/tasks (if enabled)
        if (PASchedulerProperties.SCHEDULER_JOB_LOGS_LOCATION.isSet()) {
            String logsLocation = PASchedulerProperties
                    .getAbsolutePath(PASchedulerProperties.SCHEDULER_JOB_LOGS_LOCATION.getValueAsString());

            boolean cleanStart = PASchedulerProperties.SCHEDULER_DB_HIBERNATE_DROPDB.getValueAsBoolean();
            if (cleanStart) {
                // removing selection logs directory
                logger.info("Removing logs " + logsLocation);
                FileUtils.removeDir(new File(logsLocation));
            }

            Logger jobLogger = Logger.getLogger(JobLogger.class);
            FileAppender appender = new FileAppender();
            appender.setFilesLocation(logsLocation);
            jobLogger.addAppender(appender);

            Logger taskLogger = Logger.getLogger(TaskLogger.class);
            appender = new FileAppender();
            appender.setFilesLocation(logsLocation);
            taskLogger.addAppender(appender);
        }
    }

    /**
     * Creates and starts a Scheduler on the local host.
     * This call considered that the JVM is correctly configured for starting Scheduler.
     * The "pa.scheduler.home" and required JVM properties MUST be set.
     *
     * @param rmURL the URL of a started Resource Manager
     * @param policy the full class name of the Scheduling policy to use.
     *
     * @return a Scheduler authentication that allow you to administer the Scheduler or get its connection URL.
     *
     * @throws ActiveObjectCreationException If Scheduler cannot be created
     */
    public static SchedulerAuthenticationInterface startLocal(URI rmURL, String policy) throws Exception {
        SchedulerInitializer init = new SchedulerInitializer();
        init.setPolicyFullClassName(policy);
        allowNullInit = true;
        SchedulerAuthenticationInterface sai = startLocal(rmURL, init);
        allowNullInit = false;
        return sai;
    }

    /**
     * Create a new scheduler on the local host plugged on the given resource manager.<br>
     * This will provide a connection interface to allow the access to a restricted number of user.<br>
     * Use {@link SchedulerConnection} class to join the Scheduler.
     *
     * @param rmURL the resource manager URL on which the scheduler will connect
     * @param policyFullClassName the full policy class name for the scheduler.
     * @throws AdminSchedulerException If an error occurred during creation process
     */
    public static void createScheduler(URI rmURL, String policyFullClassName) throws AdminSchedulerException {
        logger.info("Starting new Scheduler");

        //check arguments...
        if (rmURL == null) {
            String msg = "The Resource Manager URL must not be null";
            logger.error(msg);
            throw new AdminSchedulerException(msg);
        }

        try {
            // creating the scheduler
            // if this fails then it will not continue.
            logger.info("Creating scheduler frontend...");
            PAActiveObject.newActive(SchedulerFrontend.class.getName(), new Object[] { rmURL,
                    policyFullClassName });

            //ready
            logger.info("Scheduler is now ready to be started !");
            configureLog4j();
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new AdminSchedulerException(e.getMessage());
        }
    }

    /**
     * Create a new scheduler on the local host plugged on the given resource manager.<br>
     * This constructor also requires the credentials of the client to connect.<br><br>
     * It will return a client scheduler able to managed the scheduler.<br><br>
     * <font color="red">WARNING :</font> this method provides a way to connect to the scheduler after its creation,
     * BUT if the scheduler is restarting after failure, this method will create the scheduler
     * but will throw a SchedulerException due to the failure of client connection.<br>
     * In fact, while the scheduler is restarting after a crash, no one can connect it during the whole restore process.<br><br>
     * In any other case, the method will block until connection is allowed or error occurred.
     *
     * @param rmURL the resource manager URL on which the scheduler will connect
     * @param policyFullClassName the full policy class name for the scheduler.
     * @return a scheduler interface to manage the scheduler.
     * @throws SchedulerException if the scheduler cannot be created.
     * @throws AdminSchedulerException if a client connection exception occurs.
     * @throws LoginException if a user login/password exception occurs.
     */
    public static Scheduler createScheduler(Credentials creds, URI rmURL, String policyFullClassName)
            throws AdminSchedulerException, SchedulerException, LoginException {
        createScheduler(rmURL, policyFullClassName);
        SchedulerAuthenticationInterface auth = SchedulerConnection.waitAndJoin(null);
        return auth.login(creds);
    }

}
