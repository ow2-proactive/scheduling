/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.authentication;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface;
import org.ow2.proactive.scheduler.core.AdminScheduler;
import org.ow2.proactive.scheduler.core.SchedulerFrontend;
import org.ow2.proactive.scheduler.core.UserScheduler;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;
import org.ow2.proactive.scheduler.util.SchedulerLoggers;


/**
 * This is the authentication class of the scheduler.
 * To get an instance of the scheduler you must ident yourself with this class.
 * Once authenticate, the <code>login</code> method returns a user/admin interface
 * in order to managed the scheduler.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
public class SchedulerAuthentication implements InitActive, SchedulerAuthenticationInterface {

    /** Scheduler logger */
    private static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CONNECTION);

    /** Jaas config file path used to determine authentication method*/
    private String jaasConfigFilePath = PASchedulerProperties.JAAS_CONFIG_FILE_PATH.getValueAsString();

    /** The scheduler front-end connected to this authentication interface */
    private SchedulerFrontend scheduler;

    /** Active state of the authentication interface :
     * If false, user can not access the scheduler,
     * If true user can connect to the scheduler.*/
    private boolean activated = false;

    /**
     * ProActive empty constructor.
     */
    public SchedulerAuthentication() {
    }

    /**
     * Get a new instance of SchedulerAuthentication according to the given logins file.
     * This will also set java.security.auth.login.config property.
     *
     * @param scheduler the scheduler front-end on which to connect the user after authentication success.
     */
    public SchedulerAuthentication(SchedulerFrontend scheduler) {

        URL jaasConfig = SchedulerAuthentication.class.getResource("jaas.config");

        //test that gcmApplicationFile is an absolute path or not
        if (!(new File(this.jaasConfigFilePath).isAbsolute())) {
            //file path is relative, so we complete the path with the prefix RM_Home constant
            this.jaasConfigFilePath = PASchedulerProperties.SCHEDULER_HOME.getValueAsString() +
                File.separator + this.jaasConfigFilePath;
        }

        if (!(new File(jaasConfigFilePath).exists())) {
            throw new RuntimeException("Error The file " + jaasConfigFilePath + " has not been found \n" +
                "Scheduler is unable to load any authentication Method");
        }
        System.setProperty("java.security.auth.login.config", jaasConfigFilePath);
        this.scheduler = scheduler;
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        scheduler.connect();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerAuthenticationInterface#logAsUser(java.lang.String, java.lang.String)
     */
    public UserSchedulerInterface logAsUser(String user, String password) throws LoginException,
            SchedulerException {
        isStarted();

        try {
            // Verify that this user//password can connect to this existing scheduler
            logger.info(user + " is trying to connect...");

            Map<String, Object> params = new HashMap<String, Object>(4);
            //user name to check
            params.put("username", user);
            //password to check
            params.put("pw", password);
            //minimal group membership : user must belong to group user or a group above  
            params.put("group", "user");
            //group hierarchy defined for this authentication/permission ( from lowest, 
            params.put("groupsHierarchy", new String[] { "user", "admin" });

            //Load LoginContext according to login method defined in jaas.config
            LoginContext lc = new LoginContext("SchedulerLoginMethod", new NoCallbackHandler(params));

            lc.login();
            logger.info("Logging successfull for user : " + user);
            // create user scheduler interface
            logger.debug("Connecting to the scheduler...");

            UserScheduler us = new UserScheduler();
            us.schedulerFrontend = scheduler;
            //add this user to the scheduler front-end
            UserIdentificationImpl ident = new UserIdentificationImpl(user);
            ident.setHostName(getSenderHostName());
            scheduler.connect(PAActiveObject.getContext().getCurrentRequest().getSourceBodyID(), ident);

            // return the created interface
            return us;
        } catch (LoginException e) {
            logger.info(e.getMessage());
            //Nature of exception is hidden for user, we don't want to inform
            //user about the reason of non authentication
            throw new LoginException("authentication Failed");
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerAuthenticationInterface#logAsAdmin(java.lang.String, java.lang.String)
     */
    public AdminSchedulerInterface logAsAdmin(String user, String password) throws LoginException,
            SchedulerException {

        isStarted();
        try {
            // Verify that this user//password can connect to this existing scheduler
            logger.info(user + " is trying to connect as admin...");
            logger.debug("Checking user name and password...");

            Map<String, Object> params = new HashMap<String, Object>(4);
            //user name to check
            params.put("username", user);
            //password to check
            params.put("pw", password);
            //minimal group membership : user must belong to group user or a group above  
            params.put("group", "admin");
            //group hierarchy defined for this authentication/permission
            params.put("groupsHierarchy", new String[] { "admin" });

            //Load LoginContext according to login method defined in jaas.config
            LoginContext lc = new LoginContext("SchedulerLoginMethod", new NoCallbackHandler(params));

            lc.login();
            logger.info("Logging successfull for admin : " + user);
            // create user scheduler interface
            logger.debug("Connecting to the scheduler...");

            AdminScheduler as = new AdminScheduler();
            as.schedulerFrontend = scheduler;
            //add this user to the scheduler front-end
            UserIdentificationImpl ident = new UserIdentificationImpl(user, true);
            ident.setHostName(getSenderHostName());
            scheduler.connect(PAActiveObject.getContext().getCurrentRequest().getSourceBodyID(), ident);

            // return the created interface
            return as;

        } catch (LoginException e) {
            logger.info(e.getMessage());
            //Nature of exception is hidden for user, we don't want to inform
            //user about the reason of non authentication
            throw new LoginException("authentication Failed");
        }
    }

    private void isStarted() throws SchedulerException {
        if (!activated) {
            throw new SchedulerException("Scheduler is starting, please try to connect it later !");
        }
    }

    //get the host name of the sender.
    private String getSenderHostName() {
        String senderURL = PAActiveObject.getContext().getCurrentRequest().getSender().getNodeURL();
        senderURL = senderURL.replaceFirst(".*//", "").replaceFirst("/.*", "");
        return senderURL;
    }

    /**
     * Active the scheduler authentication interface.
     * This method allow user to access the scheduler.
     */
    public void activate() {
        activated = true;
    }

    /**
     * Terminate the SchedulerAuthentication active object.
     * @return always true;
     */
    public boolean terminate() {
        PAActiveObject.terminateActiveObject(false);
        logger.info("Scheduler authentication is now shutdown !");

        return true;
    }
}
