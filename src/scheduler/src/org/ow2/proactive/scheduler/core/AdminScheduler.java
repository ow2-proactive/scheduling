/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core;

import java.security.KeyException;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.policy.Policy;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.exception.AdminSchedulerException;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * <b>Start here</b>, it provides method to create a new ProActive Scheduler and manage it.<br>
 * With this interface, you will be able to create a new scheduler with or without connecting yourself,
 * or create it with your administrator properties.<br>
 * A resources manager may have been launched before creating a new scheduler.
 * This class provides methods to managed jobs as an administrator.
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class AdminScheduler extends UserScheduler implements AdminSchedulerInterface {

    /**  */
    private static final long serialVersionUID = 200;
    /** Logger to be used for all messages related to the scheduler */
    public static final Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CORE);
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.CORE);

    /**
     * Create a new scheduler at the specified URL plugged on the given resource manager.<br>
     * This will provide a connection interface to allow the access to a restricted number of user.<br>
     * It will return an admin scheduler able to managed the scheduler.
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

        //creating admin API and scheduler
        AdminScheduler adminScheduler = new AdminScheduler();
        SchedulerFrontend schedulerFrontend;

        try {
            // creating the scheduler proxy.
            // if this fails then it will not continue.
            logger.info("Creating scheduler frontend...");
            schedulerFrontend = (SchedulerFrontend) PAActiveObject.newActive(SchedulerFrontend.class
                    .getName(), new Object[] { rm, policyFullClassName });

            // setting the proxy to the admin scheduler API
            adminScheduler.schedulerFrontend = schedulerFrontend;
            //ready
            logger.info("Scheduler is now ready to be started !");
        } catch (Exception e) {
            logger_dev.error(e);
            throw new AdminSchedulerException(e.getMessage());
        }
    }

    /**
     * Create a new scheduler on the local host plugged on the given resource manager.<br>
     * This constructor also requires the username//password of the admin to connect.<br><br>
     * This will provide a connection interface to allow the access to a restricted number of user.
     * It will return an admin scheduler able to managed the scheduler.<br><br>
     * <font color="red">WARNING :</font> this method provides a way to connect to the scheduler after its creation,
     * BUT if the scheduler is restarting after failure, this method will create the scheduler
     * but will throw a SchedulerException due to the failure of admin connection.<br>
     * In fact, while the scheduler is restarting after a crash, no one can connect it during the whole restore process.<br><br>
     * The method will block until connection is allowed or error occurred.
     *
     * @param login the admin login.
     * @param password the admin password.
     * @param rm the resource manager to plug on the scheduler.
     * @param policyFullClassName the full policy class name for the scheduler.
     * @return an admin scheduler interface to manage the scheduler.
     * @throws SchedulerException if the scheduler cannot be created.
     * @throws AdminSchedulerException if an admin connection exception occurs.
     * @throws LoginException if a user login/password exception occurs.
     */
    public static AdminSchedulerInterface createScheduler(String login, String password,
            ResourceManagerProxy rm, String policyFullClassName) throws AdminSchedulerException,
            SchedulerException, LoginException {
        createScheduler(rm, policyFullClassName);

        SchedulerAuthenticationInterface auth = SchedulerConnection.waitAndJoin("//localhost/");
        Credentials creds = null;
        try {
            Credentials.createCredentials(login, password, auth.getPublicKey());
        } catch (LoginException e) {
            logger.error("Could not recover public key from Scheduler, check your configuration" + e);
            throw new LoginException("Could not encrypt credentials");
        } catch (KeyException e) {
            throw new LoginException("Could not encrypt credentials");
        }
        return auth.logAsAdmin(creds);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#changePolicy(java.lang.Class)
     */
    public BooleanWrapper changePolicy(Class<? extends Policy> newPolicyFile) throws SchedulerException {
        return schedulerFrontend.changePolicy(newPolicyFile);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#start()
     */
    public BooleanWrapper start() throws SchedulerException {
        return schedulerFrontend.start();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#stop()
     */
    public BooleanWrapper stop() throws SchedulerException {
        return schedulerFrontend.stop();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#pause()
     */
    public BooleanWrapper pause() throws SchedulerException {
        return schedulerFrontend.pause();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#freeze()
     */
    public BooleanWrapper freeze() throws SchedulerException {
        return schedulerFrontend.freeze();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#resume()
     */
    public BooleanWrapper resume() throws SchedulerException {
        return schedulerFrontend.resume();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#shutdown()
     */
    public BooleanWrapper shutdown() throws SchedulerException {
        return schedulerFrontend.shutdown();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#kill()
     */
    public BooleanWrapper kill() throws SchedulerException {
        return schedulerFrontend.kill();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#linkResourceManager(java.lang.String)
     */
    public BooleanWrapper linkResourceManager(String rmURL) throws SchedulerException {
        return schedulerFrontend.linkResourceManager(rmURL);
    }
}
