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
package scalabilityTests.fixtures;

import java.util.LinkedList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.JobId;

import scalabilityTests.framework.AbstractSchedulerUser;
import scalabilityTests.framework.JobSubmissionAction;
import scalabilityTests.framework.listeners.ListenerInfo;


/**
 * This fixture offers methods for:
 * 	- deploying {@link AbstractSchedulerUser}s on the GCM infrastructure
 *  - connecting to a Scheduler from each of the nodes
 *  - launching jobs simultaneously by each of the {@link AbstractSchedulerUser}s
 *  
 * @author fabratu
 *
 */
public class SchedulerFixture extends ActiveFixture {

    private static final Logger logger = Logger.getLogger(SchedulerFixture.class);

    private final ListenerInfo listenerInfo;

    public SchedulerFixture(String gcmDeploymentPath, String vnName) throws IllegalArgumentException {
        this(gcmDeploymentPath, vnName, null);
    }

    public SchedulerFixture(String gcmDeploymentPath, String vnName, ListenerInfo listenerInfo)
            throws IllegalArgumentException {
        super(gcmDeploymentPath, vnName);
        this.listenerInfo = listenerInfo;
    }

    /**
     * One-shot method for:
     *  - deploying {@link AbstractSchedulerUser}s
     *  - connecting them to the Scheduler, using the same Credentials for login
     *  
     * Here we take advantage of the fact(not a bug, but a feature? :P)
     *  that we can login using the same Credentials from different machines
     * @throws NodeException ProActive deployment exception
     * @throws ActiveObjectCreationException ProActive deployment exception 
     * @throws SchedulerException Scheduler user login exception
     * @throws LoginException Scheduler user login exception
     * @throws CannotRegisterListenerException 
     */
    public <V> List<AbstractSchedulerUser<V>> deployConnectedUsers(
            Class<? extends AbstractSchedulerUser<V>> schedulerUserClass, String schedulerUrl,
            Credentials userCreds) throws ActiveObjectCreationException, NodeException, LoginException,
            SchedulerException, CannotRegisterListenerException {
        // we cheat. 
        return deployConnectedUsers(schedulerUserClass, schedulerUrl, new Credentials[] { userCreds });
    }

    /**
     * Same as previous method; the difference is that 
     * now we will login using different Credentials for each Actor.
     * 
     * The number of the user credentials does not necessarily have to be equal to
     * 	the number of available Nodes. If the number is less, only the first Credentials will be used.
     *  If the number is greater, then the Credentials will be used in cycle for login
     *
     * @param schedulerUserClass - the Class of the deployed Scheduler users
     * @param schedulerUrl the URL of the Scheduler to login to
     * @param userCreds an array containing the Credentials to be used to login to the Scheduler
     * @throws CannotRegisterListenerException 
     */
    public <V> List<AbstractSchedulerUser<V>> deployConnectedUsers(
            Class<? extends AbstractSchedulerUser<V>> schedulerUserClass, String schedulerUrl,
            Credentials[] userCreds) throws LoginException, SchedulerException,
            ActiveObjectCreationException, NodeException, CannotRegisterListenerException {

        if (this.nodes == null)
            throw new IllegalStateException(
                "Invalid usage of this object; loadInfrastructure() needs to be called first");

        logger.trace("# of available nodes: " + this.nodes.size());
        logger.trace("Deploying the scheduler users...");
        List<AbstractSchedulerUser<V>> connectedUsers = new LinkedList<AbstractSchedulerUser<V>>();
        int userCredsIndex = 0;
        for (Node node : nodes) {
            AbstractSchedulerUser<V> schedulerUser = PAActiveObject.newActive(schedulerUserClass,
                    new Object[] { schedulerUrl, userCreds[userCredsIndex] }, node);
            connectedUsers.add(schedulerUser);
            userCredsIndex++;
            if (userCredsIndex == userCreds.length)
                userCredsIndex = 0;
        }
        this.knownActors.addAll(connectedUsers);
        logger.trace("Done.");

        // now it is time to connect
        logger.trace("Connecting the users to the Scheduler...");
        if (this.listenerInfo != null) {
            logger.trace("Scheduler listeners will also be registered for each user.");
        }
        for (AbstractSchedulerUser<V> schedulerUser : connectedUsers) {
            schedulerUser.connectToScheduler();
            if (this.listenerInfo != null) {
                try {
                    schedulerUser.registerListener(listenerInfo.downloadInitialState(), listenerInfo
                            .getMyEventsOnly(), listenerInfo.getListenerClazzName());
                } catch (ProActiveException e) {
                    throw new CannotRegisterListenerException(e);
                } catch (SchedulerException e) {
                    throw new CannotRegisterListenerException(e);
                } catch (IllegalArgumentException e) {
                    throw new CannotRegisterListenerException(e);
                }
            }
        }

        return connectedUsers;
    }

    public class CannotRegisterListenerException extends Exception {

    private static final long serialVersionUID = 32L;

        public CannotRegisterListenerException(String msg) {
            super(msg);
        }

        public CannotRegisterListenerException() {
            super();
        }

        public CannotRegisterListenerException(String msg, Throwable cause) {
            super(msg, cause);
        }

        public CannotRegisterListenerException(Throwable cause) {
            super(cause);
        }
    }

    public void launchSameJob(List<AbstractSchedulerUser<JobId>> schedulerUsers, JobSubmissionAction action) {
        logger.trace("Submitting the same Job...");
        for (AbstractSchedulerUser<JobId> schedulerUser : schedulerUsers) {
            schedulerUser.doAction(action);
        }
        logger.trace("Done!");
    }

}
