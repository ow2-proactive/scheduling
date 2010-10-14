/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler;

import java.net.URI;
import java.security.KeyException;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.authentication.SchedulerAuthentication;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.InternalSchedulerException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.core.SchedulerFrontend;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.exception.AdminSchedulerException;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


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
    public static final Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.SCHEDULER);
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.SCHEDULER);

    private static ResourceManagerProxy imp = null;
    private static boolean allowNullInit = false;

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
    public static SchedulerAuthenticationInterface startLocal(String rmURL, SchedulerInitializer initializer)
            throws InternalSchedulerException {
        if (imp == null) {
            if (!allowNullInit) {
                if (initializer != null) {
                    //configure application
                    configure(initializer);
                } else {
                    throw new IllegalArgumentException("Initializer cannot be null !");
                }
            }
            if (rmURL == null || rmURL.length() == 0) {
                throw new IllegalArgumentException("RM url is null or empty !");
            }
            try {
                ResourceManagerProxy imp = ResourceManagerProxy.getProxy(new URI(rmURL));
                String policy = initializer.getPolicyFullClassName();
                //start scheduler
                createScheduler(imp, policy);
                return SchedulerConnection.waitAndJoin(null);
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
    public static SchedulerAuthenticationInterface startLocal(String rmURL, String policy) throws Exception {
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
     * @param rm the resource manager to plug on the scheduler.
     * @param policyFullClassName the full policy class name for the scheduler.
     * @throws AdminSchedulerException If an error occurred during creation process
     */
    public static void createScheduler(ResourceManagerProxy rm, String policyFullClassName)
            throws AdminSchedulerException {
        logger.info("Starting new Scheduler");

        //check arguments...
        if (rm == null) {
            String msg = "The Resource Manager must be set !";
            logger_dev.error(msg);
            throw new AdminSchedulerException(msg);
        }

        //check that the RM is an active object
        try {
            PAActiveObject.getActiveObjectNodeUrl(rm);
        } catch (ProActiveRuntimeException e) {
            logger
                    .warn("The Resource Manager is not an active object, this will decrease the scheduler performance.");
        } catch (Exception e) {
            logger_dev.error(e);
            throw new AdminSchedulerException("An error has occured trying to access the Resource Manager " +
                e.getMessage());
        }

        try {
            // creating the scheduler proxy.
            // if this fails then it will not continue.
            logger.info("Creating scheduler frontend...");
            PAActiveObject.newActive(SchedulerFrontend.class.getName(), new Object[] { rm,
                    policyFullClassName });

            //ready
            logger.info("Scheduler is now ready to be started !");
        } catch (Exception e) {
            logger_dev.error(e);
            throw new AdminSchedulerException(e.getMessage());
        }
    }

    /**
     * Create a new scheduler on the local host plugged on the given resource manager.<br>
     * This constructor also requires the username//password of the client to connect.<br><br>
     * This will provide a connection interface to allow the access to a restricted number of user.
     * It will return a client scheduler able to managed the scheduler.<br><br>
     * <font color="red">WARNING :</font> this method provides a way to connect to the scheduler after its creation,
     * BUT if the scheduler is restarting after failure, this method will create the scheduler
     * but will throw a SchedulerException due to the failure of client connection.<br>
     * In fact, while the scheduler is restarting after a crash, no one can connect it during the whole restore process.<br><br>
     * The method will block until connection is allowed or error occurred.
     *
     * @param login the client login.
     * @param password the client password.
     * @param rm the resource manager to plug on the scheduler.
     * @param policyFullClassName the full policy class name for the scheduler.
     * @return a scheduler interface to manage the scheduler.
     * @throws SchedulerException if the scheduler cannot be created.
     * @throws AdminSchedulerException if a client connection exception occurs.
     * @throws LoginException if a user login/password exception occurs.
     */
    public static Scheduler createScheduler(String login, String password, ResourceManagerProxy rm,
            String policyFullClassName) throws AdminSchedulerException, SchedulerException, LoginException {
        createScheduler(rm, policyFullClassName);

        SchedulerAuthenticationInterface auth = SchedulerConnection.waitAndJoin("//localhost/");
        Credentials creds = null;
        try {
            Credentials.createCredentials(new CredData(login, password), auth.getPublicKey());
        } catch (LoginException e) {
            logger.error("Could not recover public key from Scheduler, check your configuration" + e);
            throw new LoginException("Could not encrypt credentials");
        } catch (KeyException e) {
            throw new LoginException("Could not encrypt credentials");
        }
        return auth.login(creds);
    }

}
