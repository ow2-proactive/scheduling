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
package org.objectweb.proactive.extra.scheduler.core;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extra.scheduler.common.scheduler.AdminSchedulerInterface;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extra.scheduler.exception.AdminSchedulerException;
import org.objectweb.proactive.extra.scheduler.resourcemanager.InfrastructureManagerProxy;


/**
 * Scheduler Admin interface.
 * With this interface, you will be able to create a new scheduler without connecting yourself,
 * or create it with your admin properties, groupfile and loginFile in order to give the scheduler
 * the right to only accept predefined user.
 * A resources manager may have been launched before creating a new scheduler.
 * This class provides methods to managed jobs as an administrator.
 *
 *
 * @author jlscheef - ProActiveTeam
 * @version 1.0, Jun 28, 2007
 * @since ProActive 3.2
 */
public class AdminScheduler extends UserScheduler
    implements AdminSchedulerInterface {

    /** serial version UID */
    private static final long serialVersionUID = -8799427055681878266L;

    /** Logger to be used for all messages related to the scheduler */
    public static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);

    /**
     * Create a new scheduler at the specified URL plugged on the given resource manager.
     * This will provide a connection interface to allow the access to a restricted number of user.
     * It will return an admin scheduler able to managed the scheduler.
     *
     * @param loginFile the path where are stored the allowed login//password.
     * @param groupFile the path where to check the membership of a user.
     * @param imp the resource manager to plug on the scheduler.
     * @param policyFullClassName the full policy class name for the scheduler.
     */
    public static void createScheduler(String loginFile, String groupFile,
        InfrastructureManagerProxy imp, String policyFullClassName)
        throws AdminSchedulerException {
        logger.info(
            "********************* STARTING NEW SCHEDULER *******************");
        //verifying arguments...
        if (imp == null) {
            throw new AdminSchedulerException(
                "The Entity manager must be set !");
        }

        //verifying that the scheduler is an active object
        try {
            ProActiveObject.getActiveObjectNodeUrl(imp);
        } catch (ProActiveRuntimeException e) {
            logger.warn(
                "The infrastructure manager is not an active object, this will decrease the scheduler performance.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new AdminSchedulerException(
                "An error has occured trying to access the entity manager " +
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
            schedulerFrontend = (SchedulerFrontend) ProActiveObject.newActive(SchedulerFrontend.class.getName(),
                    new Object[] { imp, policyFullClassName });
            // creating the scheduler authentication interface.
            // if this fails then it will not continue.
            logger.info("Creating scheduler authentication interface...");
            schedulerAuth = (SchedulerAuthentication) ProActiveObject.newActive(SchedulerAuthentication.class.getName(),
                    new Object[] { loginFile, groupFile, schedulerFrontend });
            // adding NFE listener to managed non functional exceptions
            // that occurs in Proactive Core
            //ProActive.addNFEListenerOnAO(schedulerFrontend,
            //    new NFEHandler("Scheduler Front-end"));
            //ProActive.addNFEListenerOnAO(schedulerAuth,
            //    new NFEHandler("Scheduler authentication"));
            // registering the scheduler proxy at the given URL
            logger.info("Registering scheduler...");
            String schedulerUrl = "//localhost/" +
                SchedulerConnection.SCHEDULER_DEFAULT_NAME;
            ProActiveObject.register(schedulerAuth, schedulerUrl);
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
     * Create a new scheduler at the specified URL plugged on the given resource manager.
     * This constructor also requires the username//password of the admin to connect.
     * This will provide a connection interface to allow the access to a restricted number of user.
     * It will return an admin scheduler able to managed the scheduler.
     * WARNING this method provides a way to connect the scheduler after its creation,
     * BUT if the scheduler is restarting after failure, this method will create the scheduler
     * but will throw a SchedulerException due to the failure of admin connection.
     *
     * @param loginFile the path where are stored the allowed login//password.
     * @param groupFile the path where to check the membership of a user.
     * @param login the admin login.
     * @param password the admin password.
     * @param imp the resource manager to plug on the scheduler.
     * @param policyFullClassName the full policy class name for the scheduler.
     * @return an admin scheduler interface to manage the scheduler.
     * @throws SchedulerException if the scheduler cannot be created.
     * @throws AdminSchedulerException if an admin connection exception occurs.
     * @throws LoginException if a user login/password exception occurs.
     */
    public static AdminSchedulerInterface createScheduler(String loginFile,
        String groupFile, String login, String password,
        InfrastructureManagerProxy imp, String policyFullClassName)
        throws AdminSchedulerException, SchedulerException, LoginException {
        createScheduler(loginFile, groupFile, imp, policyFullClassName);
        SchedulerAuthenticationInterface auth = SchedulerConnection.join(null);
        return auth.logAsAdmin(login, password);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.AdminSchedulerInterface#start()
     */
    public BooleanWrapper start() throws SchedulerException {
        return schedulerFrontend.coreStart();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.AdminSchedulerInterface#stop()
     */
    public BooleanWrapper stop() throws SchedulerException {
        return schedulerFrontend.coreStop();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.AdminSchedulerInterface#pause()
     */
    public BooleanWrapper pause() throws SchedulerException {
        return schedulerFrontend.corePause();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.AdminSchedulerInterface#pauseImmediate()
     */
    public BooleanWrapper pauseImmediate() throws SchedulerException {
        return schedulerFrontend.coreImmediatePause();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.AdminSchedulerInterface#resume()
     */
    public BooleanWrapper resume() throws SchedulerException {
        return schedulerFrontend.coreResume();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.AdminSchedulerInterface#shutdown()
     */
    public BooleanWrapper shutdown() throws SchedulerException {
        return schedulerFrontend.coreShutdown();
    }

    public BooleanWrapper kill() throws SchedulerException {
        return schedulerFrontend.coreKill();
    }
}
