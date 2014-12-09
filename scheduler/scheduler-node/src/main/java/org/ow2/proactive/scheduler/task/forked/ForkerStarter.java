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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.task.forked;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.rm.util.process.EnvironmentCookieBasedChildProcessKiller;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.apache.log4j.Logger;

import static org.ow2.proactive.utils.ClasspathUtils.findSchedulerHome;


/**
 * ForkerStarter...
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.0
 */
public class ForkerStarter {

    private static final Logger logger = Logger.getLogger(ForkerStarter.class);

    private static final int PING_NODE_EVERY_MINUTE = 60 * 1000;
    private static final int NB_OF_NODE_PING_FAILURES_BEFORE_EXIT = 10;

    //args[0]=callbackURL,args[1]=nodeName
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println(ForkerStarter.class.getSimpleName() +
                " does not have the appropriate number of arguments.");
            System.err.println("Arguments must be : callbackURL nodeName");
            System.exit(2);
        }
        configureRMAndProActiveHomes();
        EnvironmentCookieBasedChildProcessKiller.registerKillChildProcessesOnShutdown("forked");
        ForkerStarter fs = new ForkerStarter();
        Node n = fs.createLocalNode(args[1]);
        fs.callBack(args[0], n);
    }

    /**
     * callback method to starter runtime at given URL
     * Node reference will be sent through the callback method
     * 
     * @param url the url of the runtime to join
     * @param n the node to return
     */
    private void callBack(String url, Node n) {
        try {
            final ForkerStarterCallback oa = PAActiveObject.lookupActive(ForkerStarterCallback.class, url);
            pingNodeInCaseItDies(oa);
            oa.callback(n);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
            System.exit(3);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(4);
        }
    }

    private void pingNodeInCaseItDies(final ForkerStarterCallback oa) {
        new Timer("PingNode").schedule(new TimerTask() {
            private int nbOfFailedPing = 0;

            @Override
            public void run() {
                boolean pong = PAActiveObject.pingActiveObject(oa);
                if (pong) {
                    nbOfFailedPing = 0;
                } else {
                    nbOfFailedPing++;
                }

                if (nbOfFailedPing > NB_OF_NODE_PING_FAILURES_BEFORE_EXIT) {
                    logger.warn("Node seems dead, Forker task exiting on its own");
                    System.exit(7);
                }
            }
        }, 0, PING_NODE_EVERY_MINUTE);
    }

    private static void configureRMAndProActiveHomes() {
        if (System.getProperty(PAResourceManagerProperties.RM_HOME.getKey()) == null) {
            System.setProperty(PAResourceManagerProperties.RM_HOME.getKey(), findSchedulerHome());
        }
        if (System.getProperty(CentralPAPropertyRepository.PA_HOME.getName()) == null) {
            System.setProperty(CentralPAPropertyRepository.PA_HOME.getName(), System
                    .getProperty(PAResourceManagerProperties.RM_HOME.getKey()));
        }

        if (System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey()) == null) {
            System.setProperty(PASchedulerProperties.SCHEDULER_HOME.getKey(), System
                    .getProperty(PAResourceManagerProperties.RM_HOME.getKey()));
        }
    }

    /**
     * Creates the node with the name given as parameter and returns it.
     * 
     * @param nodeName The expected name of the node
     * @return the newly created node.
     */
    protected Node createLocalNode(String nodeName) {
        Node localNode = null;
        try {
            localNode = NodeFactory.createLocalNode(nodeName, false, null, null);
            if (localNode == null) {
                System.err.println("Cannot create local node !");
                System.exit(5);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(6);
        }
        return localNode;
    }

}
