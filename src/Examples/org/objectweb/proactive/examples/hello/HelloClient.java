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
package org.objectweb.proactive.examples.hello;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This shows an example of how to access another Active Object,
 * which may have been created by another application.
 */
public class HelloClient {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    /** Looks for a Hello Active Object bepending on args, and calls a method on it */
    public static void main(String[] args) {
        Hello myServer;
        String message;
        try {
            // checks for the server's URL
            if (args.length == 0) {
                // There is no url to the server, so create an active server within this VM
                myServer = (Hello) org.objectweb.proactive.api.ProActiveObject.newActive(Hello.class.getName(),
                        new Object[] { "local" });
            } else {
                // Lookups the server object
                logger.info("Using server located on " + args[0]);
                myServer = (Hello) org.objectweb.proactive.api.ProActiveObject.lookupActive(Hello.class.getName(),
                        args[0]);
            }

            // Invokes a remote method on this object to get the message
            message = myServer.sayHello().toString();
            // Prints out the message
            logger.info("The message is : " + message);
        } catch (Exception e) {
            logger.error("Could not reach/create server object");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
