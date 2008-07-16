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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;
import org.ow2.proactive.scheduler.util.SchedulerLoggers;
import org.objectweb.proactive.extensions.security.loginmodule.FileLogin;
import org.objectweb.proactive.extensions.security.loginmodule.Login;


/**
 * This is the authentication class of the scheduler.
 * To get an instance of the scheduler you must ident yourself with this class.
 * Once authenticate, the <code>login</code> method returns a user/admin interface
 * in order to managed the scheduler.
 *
 * @author The ProActive Team
 * @since ProActive 3.9
 *
 */
public class SchedulerAuthentication implements InitActive, SchedulerAuthenticationInterface {

    /** Scheduler logger */
    private static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CORE);

    /** The file where to store the allowed user//password */
    private String loginFile;

    /** The file where to store group management */
    private String groupFile;

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
     * @param loginFile the file path where to check if a username//password is correct.
     * @param groupFile the file path where to check the membership of a user.
     * @param scheduler the scheduler front-end on which to connect the user after authentication success.
     */
    public SchedulerAuthentication(String loginFile, String groupFile, SchedulerFrontend scheduler) {
        URL jaasConfig = Login.class.getResource("jaas.config");

        if (jaasConfig == null) {
            throw new RuntimeException(
                "The file 'jaas.config' has not been found and have to be at the following directory :\n"
                    + "\tclasses/Extensions/org.objectweb.proactive.extensions.security.loginmodule/");
        }

        System.setProperty("java.security.auth.login.config", jaasConfig.toString());
        this.loginFile = loginFile;
        this.groupFile = groupFile;
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
        // Verify that this user//password can connect to this existing scheduler
        logger.info(user + " is trying to connect...");
        logger.info("Checking user name and password...");

        Map<String, Object> params = new HashMap<String, Object>(6);
        params.put("username", user);
        params.put("pw", password);
        params.put("path", loginFile);
        params.put("group", "user");
        params.put("groupsFilePath", groupFile);
        params.put("groupsHierarchy", new String[] { "user", "admin" });
        FileLogin.login(params);
        logger.info("Logging successfull for user : " + user);
        // create user scheduler interface
        logger.info("Connecting to the scheduler...");

        UserScheduler us = new UserScheduler();
        us.schedulerFrontend = scheduler;
        //add this user to the scheduler front-end
        UserIdentificationImpl ident = new UserIdentificationImpl(user);
        ident.setHostName(getSenderHostName());
        scheduler.connect(PAActiveObject.getContext().getCurrentRequest().getSourceBodyID(), ident);

        // return the created interface
        return us;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerAuthenticationInterface#logAsAdmin(java.lang.String, java.lang.String)
     */
    public AdminSchedulerInterface logAsAdmin(String user, String password) throws LoginException,
            SchedulerException {
        isStarted();
        // Verify that this user//password can connect (as admin) to this existing scheduler.
        logger.info("Checking admin name and password...");

        Map<String, Object> params = new HashMap<String, Object>(6);
        params.put("username", user);
        params.put("pw", password);
        params.put("path", loginFile);
        params.put("group", "admin");
        params.put("groupsFilePath", groupFile);
        params.put("groupsHierarchy", new String[] { "admin" });
        FileLogin.login(params);
        logger.info("Logging successfull for user : " + user);
        // create admin scheduler interface
        logger.info("Connecting to the scheduler...");

        AdminScheduler as = new AdminScheduler();
        as.schedulerFrontend = scheduler;
        //add this user to the scheduler front-end
        UserIdentificationImpl ident = new UserIdentificationImpl(user, true);
        ident.setHostName(getSenderHostName());
        scheduler.connect(PAActiveObject.getContext().getCurrentRequest().getSourceBodyID(), ident);

        // return the created interface
        return as;
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
