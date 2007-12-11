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
package org.objectweb.proactive.examples.migration;

import java.io.Serializable;
import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Our 'hello' server without IHM
 * This Active Object can migrate
 * There is a lot of "trace" to locate the object
 *
 * @author ProActive trainee Team
 * @version ProActive 1.0 (March 2002)
 */
public class SimpleObjectMigration implements Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private static final int SLEEP_TIME = 9000;
    private String name; // The name of the instance
    private String hi = " say hello from "; // The 'hello' sentence

    /**
     * Creates a new <code>SimpleObjectMigration</code> instance.
     *
     */
    public SimpleObjectMigration() {
        logger.info("SimpleObjectMigration> Empty constructor");
    }

    /**
         * Creates a new <code>SimpleObjectMigration</code> instance.
         *
         * @param name
         *            a <code>String</code> value who represents the name of the
         *            instance
         */
    public SimpleObjectMigration(String name) {
        logger.info("SimpleObjectMigration> Constructor with a parameter : " +
            name);
        this.name = name;
    }

    /**
     * Describe <code>sayHello</code> method here.
     *
     * @return a <code>String</code> value who is the 'hello' sentence
     */
    public String sayHello() {
        logger.info("SimpleObjectMigration> sayHello()");
        String localhost = null;
        try {
            localhost = ProActiveInet.getInstance().getInetAddress().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sentence = name + hi + localhost;
        logger.info("SimpleObjectMigration> sayHello() --> " + sentence);
        return sentence;
    }

    /**
     * Describe <code>moveTo</code> method here.
     * This methods is used to migrate the instance
     *
     * @param t The url of the destination node
     */
    public void moveTo(String t) {
        try {
            logger.info("SimpleObjectMigration> moveTo(" + t + ") " +
                "% start migration");
            PAMobileAgent.migrateTo(t);
            logger.info("SimpleObjectMigration> moveTo(" + t + ") " +
                "% stop migration");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
         * Describe <code>main</code> method here. It migrate a simple object from
         * node1 to node2
         *
         * <ul>
         * <li>param1 : The source node url (like
         * <code>http://host1/HTTPNode1</code>)
         * <li>
         * <li>param2 : The destination node url (like
         * <code>rmi://host2/RmiNode2</code>)
         * <li>
         * </ul>
         *
         * @param args
         *            the 2 parameters in an array
         */
    public static void main(String[] args) {
        // The source node
        String urlSourceNode = "";
        Node sourceNode = null;

        // The destination node
        String urlDestinationNode = "";
        Node destinationNode = null;

        // we need 2 args to migrate
        if (args.length == 2) {
            urlSourceNode = args[0];
            urlDestinationNode = args[1];
        } else {
            logger.info("USAGE   : java SimpleObjectMigration " +
                "urlSourceNode        urlDestinationNode ");
            logger.info("Example : java SimpleObjectMigration " +
                "rmi://host1/Mynode1  http://host2/MyNode2 ");
            System.exit(1);
        }

        System.out.println("SimpleObjectMigration> main() > " +
            "We are going to try to migrate a simple object from " +
            urlSourceNode + " to " + urlDestinationNode);

        try {
            logger.info("SimpleObjectMigration> main() > " +
                "we try to get the source node : " + urlSourceNode);

            sourceNode = NodeFactory.getNode(urlSourceNode);

            logger.info("SimpleObjectMigration> main() > " +
                "we obtain the source node : " + urlSourceNode);
        } catch (NodeException e) {
            logger.info("SimpleObjectMigration> main() > " +
                "Exception during the getting of " + " the source node " +
                urlSourceNode + " (" + e.getMessage() + ")");
            e.printStackTrace();
        }

        try {
            logger.info("SimpleObjectMigration> main() > " +
                "we try to get the destination node : " + urlDestinationNode);

            destinationNode = NodeFactory.getNode(urlDestinationNode);

            logger.info("SimpleObjectMigration> main() > " +
                "we obtain the destination node : " + urlDestinationNode);
        } catch (NodeException e) {
            System.out.println("SimpleObjectMigration> main() > " +
                "Exception during the getting of " + " the destination node " +
                urlDestinationNode + " (" + e.getMessage() + ")");
            e.printStackTrace();
        }

        logger.info("SimpleObjectMigration> main() > " +
            "We shows the state before" + " to create the Active Object");
        // We show the two nodes states
        showIds(urlSourceNode, sourceNode, urlDestinationNode, destinationNode);

        logger.info("SimpleObjectMigration> main() > " +
            "We try to create an simple active object");

        // The active obect
        SimpleObjectMigration activeHello = null;
        try {
            String className = SimpleObjectMigration.class.getName();
            Object[] params = new Object[] {
                    "Created by " +
                    ProActiveInet.getInstance().getInetAddress().toString()
                };

            activeHello = (SimpleObjectMigration) PAActiveObject.newActive(className,
                    params, sourceNode);
            logger.info("SimpleObjectMigration> main() > " +
                "We created an simple active object");

            logger.info("SimpleObjectMigration> main() > " +
                "The simple active object want to say hello ;)");

            String helloAnswer = activeHello.sayHello();

            logger.info("SimpleObjectMigration> main() > " +
                "The simple active object said '" + helloAnswer + "'");
        } catch (ActiveObjectCreationException e) {
            logger.info("SimpleObjectMigration> main() > " +
                "Exception during the creation of the active object" + " (" +
                e.getMessage() + ")");
            e.printStackTrace();
        } catch (NodeException e) {
            logger.info("SimpleObjectMigration> main() > " +
                "Exception during the creation of the active object" + " (" +
                e.getMessage() + ")");
            e.printStackTrace();
        }

        logger.info("SimpleObjectMigration> main() > " +
            "We show the state before" + " the migration of the Active Object");
        // We show the two nodes states
        showIds(urlSourceNode, sourceNode, urlDestinationNode, destinationNode);

        try {
            logger.info("SimpleObjectMigration> main() > " + "begin sleep " +
                SLEEP_TIME + " ...");
            Thread.sleep(SLEEP_TIME);
            logger.info("SimpleObjectMigration> main() > " +
                "... end of sleep " + SLEEP_TIME);
        } catch (InterruptedException e2) {
        }

        logger.info("SimpleObjectMigration> main() > " +
            "migrate active object to " + urlDestinationNode);

        logger.info("SimpleObjectMigration> main() > " +
            "We show the state after" + " the migration of the Active Object");

        logger.info("");

        // We migrate the Active Object
        activeHello.moveTo(urlDestinationNode);

        logger.info("");

        try {
            logger.info("SimpleObjectMigration> main() > " + "begin sleep " +
                SLEEP_TIME + " ...");
            Thread.sleep(SLEEP_TIME);
            logger.info("SimpleObjectMigration> main() > " +
                "... end of sleep " + SLEEP_TIME);
        } catch (InterruptedException e2) {
        }

        // We show the two nodes states
        showIds(urlSourceNode, sourceNode, urlDestinationNode, destinationNode);

        logger.info("SimpleObjectMigration> main() > " +
            "The simple active object want to say hello ;)");

        String helloAnswer = activeHello.sayHello();

        logger.info("SimpleObjectMigration> main() > " +
            "The simple active object said '" + helloAnswer + "'");

        logger.info("SimpleObjectMigration> main() > end of test");
    }

    protected static void showIds(String urlSourceNode, Node sourceNode,
        String urlDestinationNode, Node destinationNode) {
        try {
            logger.info("");
            logger.info("SimpleObjectMigration> showIds() > ");
            logger.info("-------- Ids on " + urlSourceNode + " ------");

            /*
               UniqueID[] ids = sourceNode.getActiveObjectIDs();
               for (int j = 0; j < ids.length; j++) {
                 System.out.println("\t id" + j + " = " + ids[j]);
               }
               System.out.println("-----------------------------------------------");
               System.out.println("");
               System.out.println("-------- Ids on " + urlDestinationNode + " ------");
               ids = destinationNode.getActiveObjectIDs();
               for (int j = 0; j < ids.length; j++) {
                 System.out.println("\t id" + j + " = " + ids[j]);
               }
               System.out.println("-----------------------------------------------");
             */
        } catch (Exception e) {
            logger.info("SimpleObjectMigration> showIds() > " +
                "Exception during the of the node's state" + " (" +
                e.getMessage() + ")");
            e.printStackTrace();
        }
    }
}
