/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.ow2.proactive.scheduler.core;

import java.io.File;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerConnection;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.exception.AdminSchedulerException;
import org.ow2.proactive.scheduler.policy.PolicyInterface;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;


/**
 * <b>Start here</b>, it provides method to create a new ProActive Scheduler and manage it.<br>
 * With this interface, you will be able to create a new scheduler with or without connecting yourself,
 * or create it with your administrator properties, group-file and login-File in order to give the scheduler
 * the right to only accept predefined user.<br>
 * A resources manager may have been launched before creating a new scheduler.
 * This class provides methods to managed jobs as an administrator.
 *
 *
 * @author The ProActive Team
 * @version 3.9, Jun 28, 2007
 * @since ProActive 3.9
 */
@PublicAPI
public class AdminScheduler extends UserScheduler implements AdminSchedulerInterface {

    /** Logger to be used for all messages related to the scheduler */
    public static final Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);
    /** Login file name */
    private static final String LOGIN_FILENAME = PASchedulerProperties.SCHEDULER_LOGIN_FILENAME
            .getValueAsString();
    private static final String GROUP_FILENAME = PASchedulerProperties.SCHEDULER_GROUP_FILENAME
            .getValueAsString();

    /**
     * Create a new scheduler at the specified URL plugged on the given resource manager.<br>
     * This will provide a connection interface to allow the access to a restricted number of user.<br>
     * It will return an admin scheduler able to managed the scheduler.
     *
     * @param configFile the file that contains the description of the database.
     * @param authPath the path where to find the authentication files.<br>
     * File names have to be :<ul>
     * <li>"login.cfg" the file where are stored the allowed login//password.</li>
     * <li>"group.cfg" the file where to check the membership of a user.</li></ul>
     * @param rm the resource manager to plug on the scheduler.
     * @param policyFullClassName the full policy class name for the scheduler.
     */
    public static void createScheduler(String configFile, String authPath, ResourceManagerProxy rm,
            String policyFullClassName) throws AdminSchedulerException {
        logger.info("********************* STARTING NEW SCHEDULER *******************");

        //check arguments...
        if (rm == null) {
            throw new AdminSchedulerException("The Entity manager must be set !");
        }
        //get authentication file paths
        authPath = authPath.endsWith(File.separator) ? authPath : authPath + File.separator;
        String loginFile = authPath + LOGIN_FILENAME;
        String groupFile = authPath + GROUP_FILENAME;

        if (new File(loginFile).exists() && new File(groupFile).exists()) {
            logger.info("Using Login file at : " + loginFile);
            logger.info("Using Group file at : " + groupFile);
        } else {
            throw new AdminSchedulerException(
                "The authentication path does not exist or does not contain the group and login files !");
        }

        //check that the scheduler is an active object
        try {
            PAActiveObject.getActiveObjectNodeUrl(rm);
        } catch (ProActiveRuntimeException e) {
            logger
                    .warn("The infrastructure manager is not an active object, this will decrease the scheduler performance.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new AdminSchedulerException("An error has occured trying to access the entity manager " +
                e.getMessage());
        }

        //creating admin API and scheduler
        AdminScheduler adminScheduler = new AdminScheduler();
        SchedulerFrontend schedulerFrontend;
        SchedulerAuthentication schedulerAuth;

        try {
            // creating the scheduler proxy.
            // if this fails then it will not continue.
            logger.info("Creating scheduler frontend...");
            schedulerFrontend = (SchedulerFrontend) PAActiveObject.newActive(SchedulerFrontend.class
                    .getName(), new Object[] { configFile, rm, policyFullClassName });
            // creating the scheduler authentication interface.
            // if this fails then it will not continue.
            logger.info("Creating scheduler authentication interface...");
            schedulerAuth = (SchedulerAuthentication) PAActiveObject.newActive(SchedulerAuthentication.class
                    .getName(), new Object[] { loginFile, groupFile, schedulerFrontend });

            logger.info("Registering scheduler...");

            String schedulerUrl = "//localhost/" + SchedulerConnection.SCHEDULER_DEFAULT_NAME;
            PAActiveObject.register(schedulerAuth, schedulerUrl);
            // setting the proxy to the admin scheduler API
            adminScheduler.schedulerFrontend = schedulerFrontend;
            // run forest run !!
            logger.info("Scheduler Created on " + schedulerUrl);
            logger.info("Scheduler is now ready to be started !");
        } catch (Exception e) {
            e.printStackTrace();
            throw new AdminSchedulerException(e.getMessage());
        }
    }

    /**
     * Create a new scheduler at the specified URL plugged on the given resource manager.<br>
     * This constructor also requires the username//password of the admin to connect.<br>
     * This will provide a connection interface to allow the access to a restricted number of user.
     * It will return an admin scheduler able to managed the scheduler.<br>
     * <font color="red">WARNING :</font> this method provides a way to connect to the scheduler after its creation,
     * BUT if the scheduler is restarting after failure, this method will create the scheduler
     * but will throw a SchedulerException due to the failure of admin connection.<br>
     * In fact, while the scheduler is restarting after a crash, no one can connect it during the whole restore process.
     *
     * @param configFile the file that contains the description of the database.
     * @param authPath the path where to find the authentication files.<br>
     * File names have to be :<ul>
     * <li>"login.cfg" the file where are stored the allowed login//password.</li>
     * <li>"group.cfg" the file where to check the membership of a user.</li></ul>
     * @param login the admin login.
     * @param password the admin password.
     * @param rm the resource manager to plug on the scheduler.
     * @param policyFullClassName the full policy class name for the scheduler.
     * @return an admin scheduler interface to manage the scheduler.
     * @throws SchedulerException if the scheduler cannot be created.
     * @throws AdminSchedulerException if an admin connection exception occurs.
     * @throws LoginException if a user login/password exception occurs.
     */
    public static AdminSchedulerInterface createScheduler(String configFile, String authPath, String login,
            String password, ResourceManagerProxy rm, String policyFullClassName)
            throws AdminSchedulerException, SchedulerException, LoginException {
        createScheduler(configFile, authPath, rm, policyFullClassName);

        SchedulerAuthenticationInterface auth = SchedulerConnection.join(null);

        return auth.logAsAdmin(login, password);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#changePolicy(java.lang.Class)
     */
    public BooleanWrapper changePolicy(Class<? extends PolicyInterface> newPolicyFile)
            throws SchedulerException {
        return schedulerFrontend.changePolicy(newPolicyFile);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#start()
     */
    public BooleanWrapper start() throws SchedulerException {
        return schedulerFrontend.start();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#stop()
     */
    public BooleanWrapper stop() throws SchedulerException {
        return schedulerFrontend.stop();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#pause()
     */
    public BooleanWrapper pause() throws SchedulerException {
        return schedulerFrontend.pause();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#freeze()
     */
    public BooleanWrapper freeze() throws SchedulerException {
        return schedulerFrontend.freeze();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#resume()
     */
    public BooleanWrapper resume() throws SchedulerException {
        return schedulerFrontend.resume();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#shutdown()
     */
    public BooleanWrapper shutdown() throws SchedulerException {
        return schedulerFrontend.shutdown();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#kill()
     */
    public BooleanWrapper kill() throws SchedulerException {
        return schedulerFrontend.kill();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#linkResourceManager(org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy)
     */
    public BooleanWrapper linkResourceManager(String rmURL) throws SchedulerException {
        return schedulerFrontend.linkResourceManager(rmURL);
    }
}
