/*
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.scheduler;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.scheduler.exception.AdminException;
import org.objectweb.proactive.extra.scheduler.resourcemanager.GenericResourceManager;


/**
 * This class does administrative tasks regarding the scheduler
 *
 * @author walzouab
 *
 */
public class AdminScheduler {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);
    private Scheduler scheduler;
    private UserScheduler userScheduler;

    /**
     * rEguar constructor
     * @param scheduler
     * @param userScheduler
     */
    public AdminScheduler(Scheduler scheduler, UserScheduler userScheduler) {
        this.scheduler = scheduler;
        this.userScheduler = userScheduler;
    }

    /**
    * Proactive NoArg constructor.
    *
    */
    public AdminScheduler() {
    }

    /**
         * stops the user from  submitting commands
         * @return true if it is  stopped, return false if it is already stopped
         */
    public BooleanWrapper stop() {
        BooleanWrapper s = userScheduler.stop();
        ProActive.waitFor(s);
        return s;
    }

    /**
     * Allows the user to start submitting commands
     * @return true if it is started, return false if it is already started
     */
    public BooleanWrapper start() {
        return userScheduler.start();
    }

    public void killAllRunning() {
        scheduler.killAllRunning();
    }

    public void flushqueue() {
        scheduler.flushqueue();
    }

    /**
     * Provides a way to connect to an already existing adminstator API
     *
     * @param schedulerURL
     * @return refernce to Admin API
     * @throws AdminException
     */
    public static AdminScheduler connectTo(String schedulerURL)
        throws AdminException {
        Object[] ao;
        try {
            Node node = NodeFactory.getNode(schedulerURL);

            ao = node.getActiveObjects(AdminScheduler.class.getName());
        } catch (Exception e) {
            throw new AdminException("Couldn't Connect to the scheduler");
        }

        if (ao.length == 1) {
            return ((AdminScheduler) ao[0]);
        }

        throw new AdminException(
            "Scheduler object doesnt Exist please make sure its running");
    }

    /**
     *  shuts down the scheduler
     * @param true means killing running processes flushing queues and terminating, false means waiting for current processes to finish
     */
    public void shutdown(BooleanWrapper immediate) {
        this.stop();
        scheduler.shutdown(immediate);

        BooleanWrapper schedulerterminated = scheduler.terminateScheduler();
        ProActive.waitFor(schedulerterminated);
        userScheduler.shutdown();
        try {
            //FIXME :must make sure that futures have been propagated using automatic continuation
            logger.warn(
                "FIX ME:Bugs #945 in proacive forge Will sleep in adminscheduler to allow for automatic continuation to propagate");
            Thread.sleep(1000);

            ProActive.getBodyOnThis().terminate();
        } catch (Exception e) {
            logger.info("error terminating adminscheulder" + e.getMessage());
        }
    }

    /**
     * Creates a scheduler on the local machine with a Specific or Random node name using the specifed the resoruce manager and a defaultpolicy
     *
     * @param RM- a refernce to the resoruce manager
     * @param nodeName- the required name to be used. Use null to create using a random name
     *
     *
     * @return Reference to the AdminInterface
     * @throws AdminException
     */
    public static AdminScheduler createLocalScheduler(
        GenericResourceManager RM, String nodeName) throws AdminException {
        String defaultPolicy;
        String defaultFactory;
        try {
            defaultPolicy = System.getProperty("extra.scheduler.default_policy");
            defaultFactory = System.getProperty(
                    "extra.scheduler.default_policy_factory");
        } catch (Exception e) {
            logger.error(
                "Must define the property defaultpolicy and default factory in proactive configuration");
            throw new AdminException(e.getLocalizedMessage());
        }

        return AdminScheduler.createLocalScheduler(RM, nodeName, defaultPolicy,
            defaultFactory);
    }

    /**
     * Creates a scheduler on the local machine  with a Specific or random name using the specifed the resoruce manager and a default policy
     *
     *
     * @param RM- The URL of the resource manager
     * @param nodeName- the required name to be used. Use null to create using a random name
     * @return Reference to the AdminInterface
     * @throws AdminException
     */
    public static AdminScheduler createLocalScheduler(String RMURL,
        String nodeName) throws AdminException {
        //get properties from the proactive confuguration file
        String defaultPolicy;
        String defaultFactory;
        try {
            defaultPolicy = System.getProperty("extra.scheduler.default_policy");
            defaultFactory = System.getProperty(
                    "extra.scheduler.default_policy_factory");
        } catch (Exception e) {
            logger.error(
                "Must define the property defaultpolicy and default factory in proactive configuration");
            throw new AdminException(e.getLocalizedMessage());
        }

        return AdminScheduler.createLocalScheduler(RMURL, nodeName,
            defaultPolicy, defaultFactory);
    }

    /**
     * Creates a scheduler on the local machine with a Specific or random name using the specifed the resoruce manager and policy
     *
     *
     * @param RM- The URL of the resource manager
     * @param nodeName- the required name to be used. Use null to create using a random name
     * @param  Policy- A string that referes to the class's formal name and package
     * @param policyFactory- A string that refers to a static Policy Factory that  takes a resource manager and returns a generic policy
     * @return Reference to the AdminInterface
     * @throws AdminException
     */
    public static AdminScheduler createLocalScheduler(String RMURL,
        String nodeName, String Policy, String policyFactory)
        throws AdminException {
        GenericResourceManager rm;
        try {
            rm = (GenericResourceManager) ProActive.lookupActive(GenericResourceManager.class.getName(),
                    RMURL);
        } catch (Exception e) {
            throw new AdminException(
                "Resource Manager doesn't exist in the specified URL");
        }

        return AdminScheduler.createLocalScheduler(rm, nodeName, Policy,
            policyFactory);
    }

    /**
     * Creates a scheduler on the local machine with a Specific or random name using the specifed the resoruce manager and policy
     * @param nodeName- the required name to be used. Use null to the default node(random name)
     * @param RM- a refernce to the resoruce manager
     * @param Policy- A string that referes to the class's formal name and package
     * @param policyFactory- A string that refers to a static Policy Factory that  takes a resource manager and returns a generic policy
     *
     * @return Reference to the AdminInterface
     * @throws AdminException
     */
    public static AdminScheduler createLocalScheduler(
        GenericResourceManager RM, String nodeName, String Policy,
        String policyFactory) throws AdminException {
        String localNode;

        try {
            if (nodeName == null) {
                localNode = NodeFactory.getDefaultNode().getNodeInformation()
                                       .getURL();
            } else {
                localNode = NodeFactory.createNode(nodeName).getNodeInformation()
                                       .getURL();
            }
        }
        //here is the case where a node already exists with the specified ame
        catch (java.rmi.AlreadyBoundException e) {
            throw new AdminException("Cannot create a new node becaue the node" +
                nodeName + "already Exists!!" + e.getMessage());
        } catch (Exception e) {
            throw new AdminException("Cannot create Scheduler locally " +
                e.getMessage());
        }
        return AdminScheduler.createScheduler(RM, localNode, Policy,
            policyFactory);
    }

    /**
     * Creates a scheduler on the specified node using the specifed the resoruce manager using a defualt policy
     * @param RM- a refernce to the resoruce manager
     * @param schedulerURL- the node where the scheduler is to be created
     *
     *
     * @return Reference to the AdminInterface
     * @throws AdminException
     */
    public static AdminScheduler createScheduler(GenericResourceManager RM,
        String schedulerURL) throws AdminException {
        String defaultPolicy;
        String defaultFactory;
        try {
            defaultPolicy = System.getProperty("extra.scheduler.default_policy");
            defaultFactory = System.getProperty(
                    "extra.scheduler.default_policy_factory");
        } catch (Exception e) {
            logger.error(
                "Must define the property defaultpolicy and default factory in proactive configuration");
            throw new AdminException(e.getLocalizedMessage());
        }

        return AdminScheduler.createScheduler(RM, schedulerURL, defaultPolicy,
            defaultFactory);
    }

    /**
     * Creates a scheduler on the specified node using the specifed the resoruce manager using a default policy
     *
     *
     * @param RM- The URL of the resource manager
     * @param schedulerURL- the node where the scheduler is to be created
     * @return Reference to the AdminInterface
     * @throws AdminException
     */
    public static AdminScheduler createScheduler(String RMURL,
        String schedulerURL) throws AdminException {
        String defaultPolicy;
        String defaultFactory;
        try {
            defaultPolicy = System.getProperty("extra.scheduler.default_policy");
            defaultFactory = System.getProperty(
                    "extra.scheduler.default_policy_factory");
        } catch (Exception e) {
            logger.error(
                "Must define the property defaultpolicy and default factory in proactive configuration");
            throw new AdminException(e.getLocalizedMessage());
        }
        return AdminScheduler.createScheduler(RMURL, schedulerURL,
            defaultPolicy, defaultFactory);
    }

    /**
     * Creates a scheduler on the specified node using the specifed the resoruce manager and policy
     *
     * @param RM- The URL of the resource manager
     * @param schedulerURL- the node where the scheduler is to be created
     * @param Policy- A string that referes to the class's formal name and package
     * @param policyFactory- A string that refers to a static Policy Factory that  takes a resource manager and returns a generic policy
     *
     * @return Reference to the AdminInterface
     * @throws AdminException
     */
    public static AdminScheduler createScheduler(String RMURL,
        String schedulerURL, String Policy, String policyFactory)
        throws AdminException {
        GenericResourceManager rm;
        try {
            rm = (GenericResourceManager) ProActive.lookupActive(GenericResourceManager.class.getName(),
                    RMURL);
        } catch (Exception e) {
            throw new AdminException(
                "Resource Manager doesn't exist in the specified URL");
        }

        return AdminScheduler.createScheduler(rm, schedulerURL, Policy,
            policyFactory);
    }

    /**
     * Creates a scheduler on the specified node using the specifed the resoruce manager and policy
     * @param RM- a refernce to the resoruce manager
     * @param schedulerURL- the node where the scheduler is to be created
     * @param Policy- A string that referes to the class's formal name and package
     * @param policyFactory- A string that refers to a static Policy Factory that  takes a resource manager and returns a generic policy
     *
     * @return Reference to the AdminInterface
     * @throws AdminException
     */
    public static AdminScheduler createScheduler(GenericResourceManager RM,
        String schedulerURL, String Policy, String policyFactory)
        throws AdminException {
        if (RM == null) {
            throw new AdminException("The Resource manager is set to null");
        }
        try {
            String temp = ProActive.getActiveObjectNodeUrl(RM);
        } catch (ProActiveRuntimeException e) {
            logger.warn(
                "The resource manager is not an active object, this will decrease the scheduler performance.");
        } catch (Exception e) {
            throw new AdminException(
                "An error has occured trying to access the resource manager " +
                e.getMessage());
        }
        Scheduler scheduler;
        UserScheduler userScheduler;
        AdminScheduler adminScheduler;
        try {
            //first thing to do is create the scheduler. if this fails then it will not continute, this clean up is not needed
            scheduler = (Scheduler) ProActive.newActive(Scheduler.class.getName(),
                    new Object[] { Policy, RM, policyFactory }, schedulerURL);
            userScheduler = (UserScheduler) ProActive.newActive(UserScheduler.class.getName(),
                    new Object[] { scheduler }, schedulerURL);
            adminScheduler = (AdminScheduler) ProActive.newActive(AdminScheduler.class.getName(),
                    new Object[] { scheduler, userScheduler }, schedulerURL);
            logger.info("Scheduler Created on " + schedulerURL);
        } catch (Exception e) {
          
        	throw new AdminException(e.getMessage());
        }

        return adminScheduler;
    }

    /**
     * gets the IDS of queued tasks
     * @return
     */
    public Vector<String> getQueuedID() {
        return scheduler.getQueuedID();
    }

    /**
     * gets the IDS of failed tasks
     * @return
     */
    public Vector<String> getFailedID() {
        return scheduler.getFailedID();
    }

    /**
     * returns the IDS of running tasks
     * @return
     */
    public Vector<String> getRunningID() {
        return scheduler.getRunningID();
    }

    /**
     * Retuns the status of a specific task
     * @param taskID
     * @return
     */
    public Status status(String taskID) {
        return scheduler.info(taskID).getObject().getStatus();
    }

    /**
     * get all killed tasks
     * @return
     */
    public Vector<String> getKilledID() {
        return scheduler.getKilledID();
    }

    /**
     * get all finished tasks that havent been collect by the user
     * @return
     */
    public Vector<String> getFinishedID() {
        return scheduler.getFinishedID();
    }

    public BooleanWrapper del(String tID) {
        return scheduler.del(tID, "admin");
    }

    public Vector<Info> info_all() {
        return scheduler.info_all();
    }
}
